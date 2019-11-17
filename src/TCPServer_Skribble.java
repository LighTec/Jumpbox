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
import java.io.IOException;
import java.nio.channels.Selector;
import java.util.*;

public class TCPServer_Skribble extends TCPServer_Base {

    private final int DRAWCHOICEAMT = 3;
    private final String[] DRAWCHOICES = {"Apple", "Banana", "Coconut", "Durian", "Grapes", "Kiwi", "Lime", "Mango",
            "Orange", "Starfruit", "Tomato"};
    private final int SKRIBBLEPORT = 9001;
    private final int ROUNDTIME = 90;
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
    private HashMap<String, Integer> scoreMap = new HashMap<String, Integer>();

    private LinkedList<Integer> playerRotations = new LinkedList<>();
    private Random ranGen = new Random();
    private int currentRound = 1;
    private long roundEndTime;

    /**
     * // TODO write this javadoc
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
                        this.chosenDraw = chosen;
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
                    if(msg.equals(this.chosenDraw) && this.matchStatus.equals(INMATCH)){
                        this.cplayer.setScore(this.cplayer.getScore() + this.scoreHeuristic());
                        this.sendToPlayerName(key,24,null);
                    String[] msgArray = msg.split(",");
                    int timeStamp = Integer.parseInt(msgArray[0]);
                    String userName = msgArray[1];
                    String msgBody =  msgArray[1];
                    if(msgBody.equals(this.chosenDraw) && this.matchStatus.equals(INMATCH)){
                        scoreMap.put(userName, timeStamp);
                        //this.cplayer.setScore(this.cplayer.getScore() + this.scoreHeuristic());
                    }else{
                        String chatMsg = "[" + this.cplayer.getUsername() + "]: " + msg;
                        if(DEBUG){
                            System.out.println(chatMsg);
                        }
                        String chatMsg = "[" + this.cplayer.getUsername() + "]: " + msgBody;
                        this.sendUpdates(key, 43, this.stringToByteArr(chatMsg), false);
                        this.chatHistory.add(chatMsg);
                    }
                    break;
                case 43:
                case 50:
                    this.sendInvalidCommand();
                    break;
                case 51:
                    if(cplayer.getUsername().equals(this.chosenDraw)) {
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
            System.out.println("initializing skribble...");
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
            Iterator<Integer> rotiter = this.playerRotations.listIterator();
            while(rotiter.hasNext()){
                boolean found = false;
                Integer num = rotiter.next();
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
                    System.out.println("Initializing round with draw leader ");
                }

                // send to all the new drawer
                this.sendUpdates(null, 23, this.stringToByteArr(this.drawLeader), false);
                // get draw choices
                this.currentDrawChoices = this.getDrawChoices();
                String drawStr = "";
                // format and send draw choices
                for(String choice : this.currentDrawChoices){
                    drawStr += choice + '\n';
                }
                this.sendToPlayerName(this.drawLeader, 21, this.stringToByteArr(drawStr));

                this.matchStatus = DRAWPICK;
                break;
            case DRAWPICK:
                // do nothing, the command handler manages this state
                break;
            case INMATCH:
                if(System.currentTimeMillis() > this.roundEndTime){
                    this.matchStatus = ENDWAIT;
                    this.roundEndTime = System.currentTimeMillis() + 1000;
                }

                break;
            case ENDWAIT:
                if(System.currentTimeMillis() > this.roundEndTime){
                    this.matchStatus = ROUNDINIT;
                }
                break;
            case GAMEOVER:
                terminated = true; // terminate the server
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
        List<Map.Entry<String, Integer>> list = new ArrayList<>(scoreMap.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Map<String, Integer> sortedScoreMap = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : list) {
            sortedScoreMap.put(entry.getKey(), entry.getValue());
        }


        for (Map.Entry<String, Integer> entry : sortedScoreMap.entrySet()) {
            String playerName = entry.getKey();
            int timeStamp = entry.getValue();

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
                randomStrings[i] = DRAWCHOICES[i];
                i++;
            }
        }
        return randomStrings;
    }
}