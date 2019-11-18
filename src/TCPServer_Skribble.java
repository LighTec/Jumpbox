/*
 * A TCP server for the game project 'JumpBox"
 * By CPSC 441 Fall 2019 Group 6
 * Writers:
 * -  Kell Larson
 * - {Please add your name here if you edit code, ty!}
 */
/*
TODO:
2. game loop: get player (how?) -> linkedList, a round would be a single iteration through the list, and a match a single next()
    set a player as a drawer, send to all their name (same as lobby)
    drawer is given choices from dictionary (partially complete, must send choices to user. Choice generator complete
    drawer returns chosen choice
    timer begins for all
    drawer draws frames
        all other users must gets the frames pushed to them (complete)
    users guess
        check is guess is correct, update score (and send updated score to all others) if right (partially complete, need score heuristic)
    round ends
    select new drawer via linkedlist(?)
    keep looping until roundsLeft = 0
    if roundsLeft == 0, terminate self
 */
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.*;

public class TCPServer_Skribble extends TCPServer_Base {

    private final int DRAWCHOICEAMT = 3;
    private final String[] DRAWCHOICES = {"Apple", "Banana", "Coconut", "Durian", "Grapes", "Kiwi", "Lime", "Mango",
            "Orange", "Starfruit", "Tomato"};
    private final int SKRIBBLEPORT = 9001;
    private final int ROUNDTIME = 20;
    private final int TOTALROUNDS = 3;
    private final String DRAWPICK = "DRAWPICK";
    private final String INMATCH = "INMATCH";
    private final String ENDWAIT = "ENDWAIT";
    private final String GAMEOVER = "GAMEOVER";
    private final String ROUNDINIT = "ROUNDINIT";
    /*
match status options: DRAWPICK, INMATCH, ENDWAIT, GAMEOVER
DRAWPICK: the drawer is currently picking something to draw, and the server is waiting for them to choose
INMATCH: the drawer is currently allowed to draw, and everyone else is allowed to guess.
ENDWAIT: after a match, we wait for a small amount of time as a cooldown period.
GAMEOVER: all matches are complete, and the server will terminate on the next cycle.
 */
    private String matchStatus = ENDWAIT;
    private String drawLeader = "";
    private String[] currentDrawChoices;
    private String chosenDraw = "";
    private LinkedList<String> chatHistory = new LinkedList<>();
    private int countCorrectGuess = 0;
    private HashMap<String, Long> scoreMap = new HashMap<String, Long>();

    private LinkedList<Integer> playerRotations = new LinkedList<>();
    private Random ranGen = new Random();
    private int currentRound = 1;
    private long roundEndTime;
    private long lastTimeTimeSent = -1;

    /**
     * By passing all critical values, we do not have to open a new connection for the skirbble server.
     * @param players
     * @param selec
     * @param playerList
     * @param dcPlayers
     * @param mik
     */
    public TCPServer_Skribble(ArrayList<Player> players, Selector selec, HashMap<Integer, Player> playerList, ArrayList<Player> dcPlayers, int mik){
        super(false);
        this.selector = selec;
        this.disconnectedPlayers = players;
        this.playerNetHash = playerList;
        this.disconnectedPlayers = dcPlayers;
        this.maxIntKey = mik;
        initSkribbleGame();
    }

    @Override
    int getPort() {
        return SKRIBBLEPORT;
    }

    @Override
    void handleSpecializedCommand(int cmd, byte[] pktBytes) {
        System.out.println("inside Skribble");
        try{
            int z = 0;
            switch(cmd){
                case 1:
                    cplayer.setUsername(byteArrToString(pktBytes)); // update player name
                    this.playerNetHash.replace(intkey, cplayer); // update hashmap player
                    this.playerRotations.add(intkey); // add player to end of player draw list
                    break;
                case 20:
                case 21:
                    this.sendInvalidCommand();
                    break;
                case 22:
                    String chosen = this.byteArrToString(pktBytes);
                    boolean validOption = false;
                    for(String cho : this.currentDrawChoices){
                        if(cho.equals(chosen)){
                            validOption = true;
                        }
                    }
                    if(this.matchStatus.equals(DRAWPICK) && this.cplayer.getUsername().equals(this.drawLeader) && validOption){
                        if(DEBUG){
                            System.out.println("Received valid draw option: " + chosen);
                        }
                        this.chosenDraw = chosen;
                        this.roundEndTime = System.currentTimeMillis() + (ROUNDTIME * 1000);
                        this.matchStatus = INMATCH;
                    }else{
                        inBuffer.putInt(4);
                        inBuffer.putInt(5);
                        this.inBuffer.flip();
                        z = cchannel.write(inBuffer); // write cannot pick draw choice
                        this.inBuffer.flip();
                    }
                    break;
                case 23:
                case 24:
                case 25:
                case 26:
                    this.sendInvalidCommand();
                    break;
                case 40:
                    String chatStr = "";
                    ListIterator<String> chatIter = this.chatHistory.listIterator();
                    while(chatIter.hasNext()){
                        chatStr = chatIter.next() + '\n';
                    }

                    break;
                case 41:
                    this.sendInvalidCommand();
                    break;
                case 42:
                    String msg = this.byteArrToString(pktBytes);
                    String[] msgArray = msg.split(",");
                    Long timeStamp =Long.parseLong(msgArray[0]);
                    //int timeStamp = (int) i;
                    String userName = msgArray[1];
                    String msgBody = msgArray[1];
                    if(DEBUG) {
                        System.out.println("timeStamp: " + msgArray[0]);
                        System.out.println("userName: " + userName);
                        System.out.println("message: " + msgBody);
                    }
                    // if the guess is correct, and we are in a match, and this person is not the draw leader, do stuff
                    if(msgArray[2].toLowerCase().equals(this.chosenDraw.toLowerCase()) && this.matchStatus.equals(INMATCH) && !this.playerNetHash.get((Integer)key.attachment()).getUsername().equals(this.drawLeader)){
                        if(DEBUG){
                            System.out.println("correct Guess by user" + this.playerNetHash.get((Integer)key.attachment()).getUsername());
                        }
                        //this.sendToPlayerName(key,24,null);
                        scoreMap.put(userName, timeStamp);
                    }else{
                        if(DEBUG){
                            System.out.println("incorrect Guess by user" + this.playerNetHash.get((Integer)key.attachment()).getUsername());
                        }
                        String chatMsg =  this.cplayer.getUsername() + "," + msgBody;
                        if(DEBUG){
                            System.out.println(chatMsg);
                        }
                        this.sendUpdates(key, 43, this.stringToByteArr(chatMsg), true);
                        this.chatHistory.add(chatMsg);
                    }
                    break;
                case 43:
                case 50:
                    this.sendInvalidCommand();
                    break;
                case 51:
                    if(cplayer.getUsername().equals(this.chosenDraw)) {
                        if(DEBUG){
                            System.out.println("Propagating update canvas frame.");
                        }
                        this.sendUpdates(key, 53, pktBytes, true);
                    }else{
                        inBuffer.putInt(4);
                        inBuffer.putInt(2);
                        this.inBuffer.flip();
                        z = cchannel.write(inBuffer); // write cannot draw
                        this.inBuffer.flip();
                    }
                    break;
                case 52:
                case 53:
                case 54:
                    this.sendInvalidCommand();
                    break;
                default:
                    inBuffer.putInt(4);
                    inBuffer.putInt(99);
                    this.inBuffer.flip();
                    z = cchannel.write(inBuffer); // write unknown error
                    this.inBuffer.flip();
                    break;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void initSkribbleGame(){
        if(DEBUG){
            System.out.println("initializing skribble object...");
        }
        this.playerRotations.add(-1); // -1 denotes that we've made a full loop
        for(Integer key : this.playerNetHash.keySet()){
            this.playerRotations.add(key);
        }
    }

    @Override
    void customRun() {
        // deletes old chat history
        while(this.chatHistory.size() > 100){
            this.chatHistory.removeFirst();
        }

        // handle player disconnects
        if(this.playerNetHash.size() != this.playerRotations.size()){
            if(DEBUG){
                System.out.println("Player disconnection or connection, iterating over linkedlist to remove player from list if disconnected.");
            }
            for(int i = 0; i < this.playerRotations.size(); i++){
                boolean found = false;
                Integer num = this.playerRotations.get(i);
                for(Integer key : this.playerNetHash.keySet()){
                    if(num == key){
                        found = true;
                    }
                }
                if(!found){
                    this.playerRotations.remove(num);
                }
            }
        }

        //game logic
        switch(this.matchStatus){
            case ROUNDINIT:
                this.lastTimeTimeSent = -1;
                int drawLeaderNum = this.playerRotations.removeFirst();
                if(drawLeaderNum == -1){
                    this.currentRound++;
                    if(this.currentRound >= TOTALROUNDS){
                        this.matchStatus = GAMEOVER;
                    }
                    this.playerRotations.addLast(-1);
                    drawLeaderNum = this.playerRotations.removeFirst();
                }
                this.playerRotations.addLast(drawLeaderNum);

                // select drawer
                this.drawLeader = this.playerNetHash.get(drawLeaderNum).getUsername();
                if(DEBUG){
                    System.out.println("Initializing round with draw leader " + this.drawLeader);
                }
                // send new round
                this.sendUpdates(null,25,new byte[0],false);
                // send to all the new drawer
                this.sendUpdates(null, 23, this.stringToByteArr(this.drawLeader), false);
                // get draw choices
                this.currentDrawChoices = this.getDrawChoices();
                String drawStr = "";
                // format and send draw choices
                for(String choice : this.currentDrawChoices){
                    drawStr += choice + ',';
                }
                drawStr = drawStr.substring(0, drawStr.length() - 1); // remove the last comma
                this.sendToPlayer(this.drawLeader, 21, this.stringToByteArr(drawStr));

                this.matchStatus = DRAWPICK;
                break;
            case DRAWPICK:
                if(DEBUG){
                  //  System.out.println("Currently waiting for the drawer to pick an option.");
                }
                // do nothing, the command handler manages this state
                break;
            case INMATCH:
                if(System.currentTimeMillis() - 1000 > this.lastTimeTimeSent){
                    this.lastTimeTimeSent = System.currentTimeMillis();
                    int timeLeft = (int)((this.roundEndTime - System.currentTimeMillis())/1000);
                    byte[] timebytes = ByteBuffer.allocate(4).putInt(timeLeft).array();
                    this.sendUpdates(null, 20, timebytes, false);
                }
                if(System.currentTimeMillis() > this.roundEndTime){
                    this.matchStatus = ENDWAIT;
                    this.roundEndTime = System.currentTimeMillis() + 3000;
                    if(DEBUG){
                        System.out.println("Round over, moving to ENDWAIT.");
                    }
                }else{
                 if(DEBUG){
                    /// System.out.println("Currently in match, seconds remaining: ");
                 }
                }
                break;
            case ENDWAIT:
                if(System.currentTimeMillis() > this.roundEndTime){
                    this.matchStatus = ROUNDINIT;
                    if(DEBUG){
                        System.out.println("ENDWAIT over, moving to ROUNDINIT.");
                    }
                }
                break;
            case GAMEOVER:
                if(DEBUG){
                    System.out.println("Game over! returning to lobby.");
                }
                this.terminate();
                break;
        }

    }

    /**
     * Called when a player correctly guesses the draw choice. Updates the players score.
     */
    private void scoreHeuristic(){
        int score = 0;
        // TODO score calculation
        /*Stream<Map.Entry<String,Integer>> sortedScoreMap =
                scoreMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue());*/

        //sort players by their score
        List<Map.Entry<String, Long>> list = new ArrayList<>(scoreMap.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Map<String, Long> sortedScoreMap = new LinkedHashMap<>();

        for (Map.Entry<String, Long> entry : list) {
            sortedScoreMap.put(entry.getKey(), Long.valueOf(entry.getValue()));
        }


        for (Map.Entry<String, Long> entry : sortedScoreMap.entrySet()) {
            String playerName = entry.getKey();
            Long timeStamp = entry.getValue();

            for (Player player: this.playerNetHash.values()) {
                if (player.getUsername().equals(playerName)) {
                    score = (8-countCorrectGuess) * 100;
                    player.setScore(player.getScore() + score);
                }
            }
            countCorrectGuess++;
        }
        //return score;
    }

    /**
     * Returns a round's worth of draw choices for the drawer. Guaranteed to not contain duplicates.
     * @return a random subset of the draw choices
     */
    private String[] getDrawChoices(){
        int[] randomNums = new int[DRAWCHOICEAMT];
        Arrays.fill(randomNums, -1);
        String[] randomStrings = new String[DRAWCHOICEAMT];
        int i = 0;
        while(i < DRAWCHOICEAMT){
            int rann = (int) Math.round((this.ranGen.nextDouble() * DRAWCHOICES.length));
            boolean unique = true;
            for(int j = 0; j < randomNums.length; j++){
                if(rann == randomNums[j]){
                    unique = false;
                }
            }
            if(unique){
                randomNums[i] = rann;
                randomStrings[i] = DRAWCHOICES[rann];
                i++;
            }
        }
        return randomStrings;
    }
}