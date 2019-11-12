/*
 * A TCP server for the game project 'JumpBox"
 * By CPSC 441 Fall 2019 Group 6
 * Writers:
 * -  Kell Larson
 * - {Please add your name here if you edit code, ty!}
 */
/*
TODO:
1. create global vars: roundsLeft, currentDrawer, roundTimeLeft, roundEnded, drawChoiceAmt
2. game loop: get player (how?)
    set as drawer
    drawer is given choices from dictionary
    drawer returns chosen choice
    timer begins for all
    drawer draws frames
        all other users must gets the frames pushed to them
    users guess
        check is guess is correct, update score (and send updated score to all others) if right
    round ends
    let drawer know they can no longer draw
    select new drawer
    keep looping until roundsLeft = 0
    tell users to go back to lobby
    wait 3 sec
    terminate self
 */

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.*;

public class TCPServer_Skribble extends TCPServer_Base{

    private static int DRAWCHOICEAMT = 3;
    private static String[] DRAWCHOICES = {"Apple", "Banana", "Coconut", "Durian", "Grapes", "Kiwi", "Lime", "Mango",
            "Orange", "Starfruit", "Tomato"};
    private static int SKRIBBLEPORT = 9001;
    private static int ROUNDTIME = 90;
    private static int TOTALROUNDS = 3;
    private static String DRAWPICK = "DRAWPICK";
    private static String INMATCH = "INMATCH";
    private static String ENDWAIT = "ENDWAIT";
    private static String GAMEOVER = "GAMEOVER";

    /*
match status options: DRAWPICK, INMATCH, ENDWAIT, GAMEOVER
DRAWPICK: the drawer is currently picking something to draw, and the server is waiting for them to choose
INMATCH: the drawer is currently allowed to draw, and everyone else is allowed to guess.
ENDWAIT: after a match, we wait for a small amount of time as a cooldown period.
GAMEOVER: all matches are complete, and the server will terminate on the next cycle.
 */
    private String matchStatus = "";
    private String drawLeader = "";
    private String[] currentDrawChoices;
    private String chosenDraw = "";
    private LinkedList<String> chatHistory = new LinkedList<>();

    private Random ranGen = new Random();

    /**
     * // TODO write this javadoc
     * @param players
     * @param selec
     * @param playerList
     * @param dcPlayers
     * @param mik
     */
    public TCPServer_Skribble(ArrayList<Player> players, Selector selec, HashMap<Integer,Player> playerList, ArrayList<Player> dcPlayers, int mik){
        super(false);
        this.selector = selec;
        this.disconnectedPlayers = players;
        this.playerNetHash = playerList;
        this.disconnectedPlayers = dcPlayers;
        this.maxIntKey = mik;
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
                        inBuffer.flip();
                        z = cchannel.write(inBuffer); // write cannot pick draw choice
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
                    }else{
                        String chatMsg = "[" + this.cplayer.getUsername() + "]: " + msg;
                        this.sendUpdates(selector.keys(), key, 43, this.stringToByteArr(chatMsg), false);
                        this.chatHistory.add(chatMsg);
                    }
                    break;
                case 43:
                case 50:
                    this.sendInvalidCommand();
                    break;
                case 51:
                    if(cplayer.getUsername().equals(this.chosenDraw)) {
                        this.sendUpdates(selector.keys(), key, 53, pktBytes, true);
                    }else{
                        inBuffer.putInt(4);
                        inBuffer.putInt(2);
                        inBuffer.flip();
                        z = cchannel.write(inBuffer); // write cannot draw
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
                    inBuffer.flip();
                    z = cchannel.write(inBuffer); // write unknown error
                    break;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    void customRun() {
        // deletes old chat history
        while(this.chatHistory.size() > 100){
            String old = this.chatHistory.removeFirst();
        }
    }

    /**
     * Called when a player correctly guesses the draw choice. Updates the players score.
     */
    private int scoreHeuristic(){
        int score = 0;
        // TODO score calculation
        return score;
    }

    /**
     * Returns a round's worth of draw choices for the drawer. GUaranteed to not contain duplicates.
     * @return a random subset of the draw choices
     */
    private String[] getDrawChoices(){
        int[] randomNums = new int[DRAWCHOICEAMT];
        Arrays.fill(randomNums, -1);
        String[] randomStrings = new String[DRAWCHOICEAMT];
        int i = 0;
        while(i < DRAWCHOICEAMT){
            int rann = (int)Math.round((this.ranGen.nextDouble() * DRAWCHOICES.length));
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