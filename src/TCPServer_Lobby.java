import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Set;

public class TCPServer_Lobby extends TCPServer_Base {

    private final int LOBBYPORT = 9000;

    private String selectedGame = ""; // the name of which game has been selected
    private boolean gameStarted = false; // whether a game has started
    private String leaderName = ""; // the lobby leader, who picks the game to play

    public TCPServer_Lobby(){
        super(true);
    }

    @Override
    int getPort() {
        return LOBBYPORT;
    }

    @Override
    void handleSpecializedCommand(int cmd, byte[] pktBytes) {
        try{
        int z = 0;
        switch (cmd) {
            case 1:
                cplayer.setUsername(byteArrToString(pktBytes).trim()); // update player name
                if (cplayer.isFirstPlayer()) {
                    this.leaderName = cplayer.getUsername();
                }
                this.playerNetHash.replace(intkey, cplayer); // update hashmap player
                this.inBuffer.putInt(12);
                this.inBuffer.putInt(this.leaderName.length());
                this.inBuffer.put(this.stringToByteArr(this.leaderName));
                if(DEBUG){
                    //System.out.println("===========================================================");
                    //System.out.println("Leader length: " + this.leaderName.length());
                    System.out.println("Lobby leader name: " + this.leaderName);
                    //System.out.println("===========================================================");
                }
                this.inBuffer.flip();
                z = cchannel.write(inBuffer);
                this.inBuffer.flip();
                // send player list to the newly connected player
                if(DEBUG){
                    System.out.println("Bytes sent: " + z);
                }
                inBuffer.clear(); // new command
                Set<Integer> keyset1 = this.playerNetHash.keySet();
                //System.out.println("keyset1: " + keyset1);
                String toSend1 = this.playersToSendList(keyset1);
                //inBuffer.putInt(31);
                //inBuffer.putInt(toSend1.length());
                // creates a string in the form of username,score\n for all players, then turns it into a byte array
                //inBuffer.put(this.stringToByteArr(toSend1));
                //this.inBuffer.flip();
                //z = cchannel.write(inBuffer);

                //this.inBuffer.flip();
                this.sendUpdates(key, 31, this.stringToByteArr(toSend1), true);
                //String updateMsg = cplayer.getUsername() + ',' + cplayer.getScore() + '\n';
                //this.sendUpdates(key, 34, this.stringToByteArr(updateMsg), false);

                break;
            case 10:
                inBuffer.putInt(11); // return command number
                int lengthGameTypes = 0;
                String gameStr = "";
                for (String gt : GAMETYPES) {
                    lengthGameTypes += gt.length() + 1;
                    gameStr += gt + '\n';
                }
                cBuffer = CharBuffer.allocate(lengthGameTypes);
                cBuffer.clear();
                cBuffer.put(gameStr);
                cBuffer.flip();
                inBuffer.putInt(lengthGameTypes);
                inBuffer.put(encoder.encode(cBuffer));
                cBuffer.flip();
                this.inBuffer.flip();
                z = cchannel.write(inBuffer); // write the game types, delimited by '\n'
                this.inBuffer.flip();
                break;
            case 11:
            case 12:
                this.sendInvalidCommand();
                break;
            case 13:
                if (cplayer.isFirstPlayer()) {
                    String gameNameStr = byteArrToString(pktBytes);
                    if(DEBUG){
                        System.out.println("Game leader has sent a game to play.");
                    }
                    boolean validGame = false;
                    for (String gam : this.GAMETYPES) {
                        if (gam.equals(gameNameStr)) {
                            validGame = true;
                            break;
                        }
                    }
                    if (validGame) {
                        if(DEBUG){
                            System.out.println("Game is valid. Setting selected game to " + gameNameStr);
                        }
                        this.selectedGame = gameNameStr;
                    } else {
                        inBuffer.putInt(4);
                        inBuffer.putInt(4);
                        this.inBuffer.flip();
                        z = cchannel.write(inBuffer); // write invalid command error, did not return a game name
                        this.inBuffer.flip();
                    }
                } else {
                    inBuffer.putInt(4);
                    inBuffer.putInt(3);
                    this.inBuffer.flip();
                    z = cchannel.write(inBuffer); // write invalid command error, only "leader" can select game
                    this.inBuffer.flip();
                }
                break;
            case 14:
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

    @Override
    void customRun() {
        if(!this.selectedGame.isEmpty()){
            if(DEBUG){
                System.out.println("Game selected, attempting to go into a game...");
            }
            switch(this.selectedGame){
                case "skribble":
                    this.sendUpdates(null,14, new byte[0], false); // send to all to move to skribble code
                    TCPServer_Skribble skribServ = new TCPServer_Skribble(this.selector, this.playerNetHash, this.disconnectedPlayers, this.maxIntKey);
                    skribServ.runServer();
                    // wait for server to terminate
                    // update local resources
                    this.selector = skribServ.getSelector();
                    this.playerNetHash = skribServ.getPlayerNetHash();
                    this.disconnectedPlayers = skribServ.getDisconnectedPlayers();
                    this.maxIntKey = skribServ.getMaxIntKey();
                    break;
                default:
                    System.err.println("Game start error! The selected game string is invalid, resetting game string...");
                    this.selectedGame = "";
                    break;
            }
            this.resetLobby(); // reset all lobby vars
        }
    }

    /**
     * Resets lobby and player variables to a clean state for the next game
     */
    private void resetLobby(){
        Set<Integer> keys = this.playerNetHash.keySet();
        for(Integer k : keys){
            Player p = this.playerNetHash.get(k);
            p.setScore(0);
            this.playerNetHash.replace(k,p);
        }
        this.selectedGame = "";
    }




}