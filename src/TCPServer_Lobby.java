/*
 * A TCP server for the game project 'JumpBox"
 * By CPSC 441 Fall 2019 Group 6
 * Writers:
 * -  Kell Larson
 * - {Please add your name here if you edit code, ty!}
 */

import java.io.*;
import java.nio.*;
import java.util.*;

public class TCPServer_Lobby extends TCPServer_Base {

    private static int LOBBYPORT = 9000;

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
                cplayer.setUsername(byteArrToString(pktBytes)); // update player name
                if (cplayer.isFirstPlayer()) {
                    this.leaderName = cplayer.getUsername();
                }
                this.playerNetHash.replace(intkey, cplayer); // update hashmap player
                inBuffer.putInt(12);
                inBuffer.putInt(this.leaderName.length());
                inBuffer.put(this.stringToByteArr(this.leaderName));
                if(DEBUG){
                    System.out.println("===========================================================");
                    System.out.println("Leader length: " + this.leaderName.length());
                    System.out.println("Leader name: " + this.leaderName);
                    System.out.println("===========================================================");
                }
                z = cchannel.write(inBuffer);
                // send player list to the newly connected player
                inBuffer.clear(); // new command
                Set<Integer> keyset1 = this.playerNetHash.keySet();
                String toSend1 = this.playersToSendList(keyset1);
                inBuffer.putInt(31);
                inBuffer.putInt(toSend1.length());
                // creates a string in the form of username,score\n for all players, then turns it into a byte array
                inBuffer.put(this.stringToByteArr(toSend1));
                z = cchannel.write(inBuffer);

                String updateMsg = cplayer.getUsername() + ',' + cplayer.getScore() + '\n';
                this.sendUpdates(key, 34, this.stringToByteArr(updateMsg), false);

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
                z = cchannel.write(inBuffer); // write the game types, delimited by '\n'
                break;
            case 11:
            case 12:
                this.sendInvalidCommand();
                break;
            case 13:
                if (cplayer.isFirstPlayer()) {
                    String gameNameStr = byteArrToString(pktBytes);
                    boolean validGame = false;
                    for (String gam : this.GAMETYPES) {
                        if (gam.equals(gameNameStr)) {
                            validGame = true;
                            break;
                        }
                    }
                    if (validGame) {
                        this.selectedGame = gameNameStr;
                    } else {
                        inBuffer.putInt(4);
                        inBuffer.putInt(4);
                        inBuffer.flip();
                        z = cchannel.write(inBuffer); // write invalid command error, did not return a game name
                    }
                } else {
                    inBuffer.putInt(4);
                    inBuffer.putInt(3);
                    inBuffer.flip();
                    z = cchannel.write(inBuffer); // write invalid command error, only "leader" can select game
                }
                break;
            case 14:
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
        if(!this.selectedGame.isEmpty()){
            ArrayList<Player> totalPlayerList = new ArrayList<>();
            Set<Integer> keysetsel = this.playerNetHash.keySet();
            for(Integer k : keysetsel){
                totalPlayerList.add(this.playerNetHash.get(k));
            }
            switch(this.selectedGame){
                case "skribble":
                    TCPServer_Skribble skribServ = new TCPServer_Skribble(totalPlayerList, this.readSelector, this.playerNetHash, this.disconnectedPlayers, this.maxIntKey);
                    skribServ.runServer();
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