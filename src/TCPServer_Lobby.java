/*
 * A TCP server for the game project 'JumpBox"
 * By CPSC 441 Fall 2019 Group 6
 * Writers:
 * -  Kell Larson
 * - {Please add your name here if you edit code, ty!}
 */

/*
IMPORTANT STUFF FOR WHEN WRITING CLIENT:
- buffer size of 64000
- commands are integers
 */

/*
TODO:
- lobby loop
- run the skribble server
- debugging everything
 */

/* ####### How to run: ##########
        TCPServer_Lobby lobby = new TCPServer_Lobby(9996); // pick some port
        lobby.runServer(); // starts the server.
 */


import com.sun.org.apache.bcel.internal.generic.Select;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class TCPServer_Lobby {

    private static int BUFFERSIZE = 64000; // 64k buffer, enough for a 140*140 uncompressed image max
    private static int PLAYERMAX = 16;
    private static boolean DEBUG = true; // debug print statements print if this is true
    private static String[] GAMETYPES = {"skribble"}; // game types available
    private static int MAXNAMELEN = 256;

    private int port;
    private int[] cmdLen;
    private ArrayList<Player> playerList;
    private boolean terminated = false;
    private HashMap<SelectionKey, Player> playerNetHash = new HashMap<>(PLAYERMAX); // must be allocated size, so max 16 currently connected players
    private ByteBuffer inBuffer = null;
    private CharBuffer cBuffer = null;
    private String selectedGame = "";
    private boolean gameStarted = false;

    public TCPServer_Lobby(int port){
        this.port = port;
        this.playerList = new ArrayList<>();
        this.initCmdLen();
    }

    public TCPServer_Lobby(int port, ArrayList<Player> players){
        this.port = port;
        this.playerList = players;
        this.initCmdLen();
    }

    /**
     * Resets lobby and player variables to a clean state for the next game
     */
    private void resetLobby(){
        Set<SelectionKey> keys = this.playerNetHash.keySet();
        for(SelectionKey k : keys){
            Player p = this.playerNetHash.get(k);
            p.setScore(0);
            this.playerNetHash.replace(k,p);
        }
        this.selectedGame = "";
    }

    /**
     * Runs a lobby instance. A more in-depth explanation to come later. on implementation details.
     */
    public void runServer() {
        // Try to open a server socket on the given port
        // Note that we can't choose a port less than 1023 if we are not
        // privileged users (root)

        // set up encoder and decoder for string translation
        Charset charset = StandardCharsets.US_ASCII;
        CharsetEncoder encoder = charset.newEncoder();
        CharsetDecoder decoder = charset.newDecoder();

        // Initialize the selector
        Selector selector = null;
        try {
            selector = Selector.open();

        // Create a server channel and make it non-blocking
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);

        // Get the port number and bind the socket
        InetSocketAddress isa = new InetSocketAddress(this.port);
        channel.socket().bind(isa);

        // Register that the server selector is interested in connection requests
        channel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
        // Wait for something happen among all registered sockets
        try {
            while (!terminated)
            {
                // ########## LOBBY CODE BEGIN

                if(!this.selectedGame.isEmpty()){
                    // TODO init game lobby
                    switch(this.selectedGame){
                        case "skribble":
                            break;
                        default:
                            System.err.println("Game start error!");
                            break;
                    }
                    this.resetLobby(); // reset all lobby vars
                }

                // ########## LOBBY CODE END
                // ########## NETWORK MANAGEMENT CODE BEGIN

                if (selector.select(500) < 0)
                {
                    System.out.println("select() failed");
                    System.exit(1);
                }

                // Get set of ready sockets
                Set readyKeys = selector.selectedKeys();
                Iterator readyItor = readyKeys.iterator();

                // Walk through the ready set
                while (readyItor.hasNext()) {
                    // Get key from set
                    SelectionKey key = (SelectionKey)readyItor.next();

                    // Remove current entry
                    readyItor.remove();

                    // Accept new connections, if any
                    if (key.isAcceptable()){
                        SocketChannel cchannel = ((ServerSocketChannel)key.channel()).accept();
                        cchannel.configureBlocking(false);
                        System.out.println("Accepted a connection from " + cchannel.socket().getInetAddress() + ":" + cchannel.socket().getPort());

                        // Register the new connection for read operation
                        cchannel.register(selector, SelectionKey.OP_READ);


                        // creates a new player, with the firstPlayer boolean true if no other players in the hashmap
                        Player newplayer = new Player(this.playerNetHash.isEmpty());
                        // set up a player for this connection
                        this.playerNetHash.put(key, newplayer);

                    }else{
                        SocketChannel cchannel = (SocketChannel)key.channel();
                        Player cplayer = this.playerNetHash.get(key); // player for this connection
                        if (key.isReadable()){
                            Socket socket = cchannel.socket(); // never used, can delete

                            // Open input and output streams
                            inBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);
                            // Read from socket
                            int bytesRecv = cchannel.read(inBuffer);
                            if (bytesRecv <= 0)
                            {
                                System.out.println("read() error for a client, or a client connection has closed");
                                key.cancel();  // deregister the socket
                                continue;
                            }
                            inBuffer.flip();      // make buffer available for reading
                            int cmdNum = inBuffer.getInt(); // command number
                            if(DEBUG) {
                                System.out.println("CMD received: " + cmdNum);
                            }
                            if(cmdNum < this.cmdLen.length) {
                                int len = this.cmdLen[cmdNum]; // command length
                                byte[] pktBytes;
                                if (len == -2) {
                                    inBuffer.flip();
                                    inBuffer.putInt(4);
                                    inBuffer.putInt(4);
                                    inBuffer.flip();
                                    len = 0;
                                    // send an error to the client
                                } else if (len == -1) {
                                    len = inBuffer.getInt();
                                    if(DEBUG){
                                        System.out.println("Variable length: " + len);
                                    }
                                }
                                pktBytes = new byte[len]; // the command data
                                for (int i = 0; i < len; i++) {
                                    pktBytes[i] = inBuffer.get();
                                }
                                inBuffer.flip(); // done reading, flip back to write for output

                                if(DEBUG){
                                    System.out.println(Arrays.toString(pktBytes));
                                }

                                int z = 0;
                                switch(cmdNum){
                                    case 11: // all commands that the clients should not send
                                    case 12:
                                    case 14:
                                    case 20:
                                    case 21:
                                    case 22:
                                    case 23:
                                    case 24:
                                    case 25:
                                    case 26:
                                    case 31:
                                    case 32:
                                    case 40:
                                    case 41:
                                    case 42:
                                    case 43:
                                    case 50:
                                    case 51:
                                    case 52:
                                    case 53:
                                    case 54:
                                        inBuffer.putInt(4);
                                        inBuffer.putInt(4);
                                        inBuffer.flip();
                                        z = cchannel.write(inBuffer); // write invalid command error, cmd 11 is for clients only
                                        break;
                                    case 1:
                                        cplayer.setUsername(Arrays.toString(pktBytes)); // update player name
                                        this.playerNetHash.replace(key, cplayer); // update hashmap player
                                        break;
                                    case 2:
                                        this.playerNetHash.remove(key); // remove player from list
                                        break;
                                    case 3:
                                    case 6:
                                        // to be implemented as an additional feature for reconnection
                                        inBuffer.putInt(4);
                                        inBuffer.putInt(6);
                                        inBuffer.flip();
                                        z = cchannel.write(inBuffer); // write not yet implemented error for reconnection
                                        break;
                                    case 4:
                                        System.out.println("Error received: " + Arrays.toString(pktBytes)); // print error
                                        break;
                                    case 5:
                                        if(DEBUG){
                                            System.out.println("Echoing back: " + new String(pktBytes, StandardCharsets.UTF_8));
                                        }
                                        inBuffer.put(pktBytes);
                                        inBuffer.flip();
                                        z = cchannel.write(inBuffer); // echo back
                                        break;
                                    case 10:
                                        inBuffer.putInt(11); // return command number
                                        int lengthGameTypes = 0;
                                        String gameStr = "";
                                        for(String gt : GAMETYPES){
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
                                    case 13:
                                        if(cplayer.isFirstPlayer()) {
                                            String gameNameStr = Arrays.toString(pktBytes);
                                            boolean validGame = false;
                                            for(String gam : this.GAMETYPES){
                                                if(gam.equals(gameNameStr)){
                                                    validGame = true;
                                                    break;
                                                }
                                            }
                                            if(validGame){
                                                this.selectedGame = gameNameStr;
                                            }else{
                                                inBuffer.putInt(4);
                                                inBuffer.putInt(4);
                                                inBuffer.flip();
                                                z = cchannel.write(inBuffer); // write invalid command error, did not return a game name
                                            }
                                        }else{
                                            inBuffer.putInt(4);
                                            inBuffer.putInt(3);
                                            inBuffer.flip();
                                            z = cchannel.write(inBuffer); // write invalid command error, only "leader" can select game
                                        }
                                        break;
                                    case 30:
                                        int totalLen = 0;
                                        String sendStr = "";
                                        Set<SelectionKey> keyset = this.playerNetHash.keySet();
                                        String[] nameArr = new String[keyset.size()];
                                        int[] scoreArr = new int[keyset.size()];
                                        int it = 0;
                                        for(SelectionKey k : keyset){
                                            Player p = this.playerNetHash.get(k);
                                            nameArr[it] = p.getUsername();
                                            scoreArr [it] = p.getScore();
                                            it++;
                                            totalLen += 6 + p.getUsername().length();
                                        }

                                        for(int i = 0; i < keyset.size(); i++){
                                            inBuffer.putInt(scoreArr[i]); // put score
                                            inBuffer.putChar(','); // put comma

                                            // put name in buffer
                                            cBuffer = CharBuffer.allocate(MAXNAMELEN);
                                            cBuffer.clear();
                                            cBuffer.put(nameArr[i]);
                                            cBuffer.flip();
                                            inBuffer.put(encoder.encode(cBuffer));
                                            cBuffer.flip();

                                            // end entry
                                            inBuffer.putChar('\n');

                                            z = cchannel.write(inBuffer);
                                        }
                                        break;
                                    case 33:
                                        cplayer.setUsername(Arrays.toString(pktBytes));
                                        this.playerNetHash.replace(key,cplayer);
                                        break;
                                    default:
                                        inBuffer.putInt(4);
                                        inBuffer.putInt(99);
                                        inBuffer.flip();
                                        z = cchannel.write(inBuffer); // write unknown error
                                        break;
                                }
                            }else{
                                inBuffer.clear();
                               // inBuffer.flip(); // done reading, flip back to write for output
                                inBuffer.putInt(4);
                                inBuffer.putInt(99);
                                inBuffer.flip();
                                int z = cchannel.write(inBuffer); // unknown error
                                System.err.println("CMD receive error, flushing byte buffer.");
                            }

                            // ######### BEGIN COMMAND HANDLING

                        }
                    }
                } // end of while (readyItor.hasNext())
            } // end of while (!terminated)
        }
        catch (IOException e) {
            System.out.println(e);
        }

        // close all connections
        Set keys = selector.keys();
        Iterator itr = keys.iterator();
        while (itr.hasNext())
        {
            SelectionKey key = (SelectionKey)itr.next();
            try {
                if (key.isAcceptable())
                    ((ServerSocketChannel) key.channel()).socket().close();
                else if (key.isValid())
                    ((SocketChannel) key.channel()).socket().close();
            }catch (IOException e){
                // do nothing
            }
        }
    }

    private void initCmdLen(){
        this.cmdLen = new int[256];
        Arrays.fill(this.cmdLen, -2);
        // manually init valid commands
        this.cmdLen[1] = -1; // -1 == n length
        this.cmdLen[2] = 0;
        this.cmdLen[3] = 0;
        this.cmdLen[4] = 4;
        this.cmdLen[5] = -1;
        this.cmdLen[6] = 6;
        this.cmdLen[10] = 0;
        this.cmdLen[11] = 4;
        this.cmdLen[12] = 0;
        this.cmdLen[13] = 4;
        this.cmdLen[14] = 2;
        this.cmdLen[20] = 4;
        this.cmdLen[21] = -1;
        this.cmdLen[22] = -1;
        this.cmdLen[23] = 0;
        this.cmdLen[24] = 0;
        this.cmdLen[25] = 0;
        this.cmdLen[26] = 0;
        this.cmdLen[30] = 0;
        this.cmdLen[31] = -1;
        this.cmdLen[32] = 0;
        this.cmdLen[33] = -1;
        this.cmdLen[40] = 0;
        this.cmdLen[41] = -1;
        this.cmdLen[42] = -1;
        this.cmdLen[43] = -1;
        this.cmdLen[50] = 0;
        this.cmdLen[51] = -1;
        this.cmdLen[52] = -1;
        this.cmdLen[53] = -1;
        this.cmdLen[54] = -1;
    }
    // ####### NETWORK MANAGEMENT CODE END
}