import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.*;
import java.util.*;

/**
 * A set of all code that would normally be in multiple TCPServers.
 */
public abstract class TCPServer_Base {

    final int BUFFERSIZE = 64000; // 64k buffer, enough for a 140*140 uncompressed image max
    final int PLAYERMAX = 16;
    final boolean DEBUG = true; // debug print statements print if this is true
    final String[] GAMETYPES = {"skribble"}; // game types available
    final int MAXNAMELEN = 256;

    Charset charset = StandardCharsets.US_ASCII; // what charset we are using for byte <-> string conversion
    CharsetEncoder encoder = charset.newEncoder();
    CharsetDecoder decoder = charset.newDecoder();

    int[] cmdLen; // command length array
    HashMap<Integer, Player> playerNetHash = new HashMap<>(PLAYERMAX); // must be allocated size, so max 16 currently connected players
    HashMap<Integer, TCPMessageHandler> msgNetHandle = new HashMap<>();
    ByteBuffer inBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);
    CharBuffer cBuffer = null;
    Integer maxIntKey = 0; // the current integer key
    ArrayList<Player> disconnectedPlayers = new ArrayList<>(); // list of players that are not currently connected
    private boolean terminated = false; // if the lobby has terminated or not
    private boolean[] canHandleCommand;
    Player cplayer;
    TCPMessageHandler msghandler;
    int intkey;
    SocketChannel cchannel;
    SelectionKey key;
    Selector selector = null;
    int cmdNum;
    byte[] pktBytes;

    public TCPServer_Base(boolean init){
        this.initCmdLen();
        this.initCanHandle();
        if(init){
            this.initServer();
        }
    }

    /**
     * Returns if a command can be handled by the base handler.
     * @param cmd
     * @return
     */
    public boolean canHandleCommand(int cmd){
        return this.canHandleCommand[cmd];
    }

    /**
     * Used to get the specific port to run this server on, based off whatever implements it.
     * @return
     */
    abstract int getPort();

    /**
     * If the game type that this implements requires special commands, it will call them through here.
     * @param cmd
     * @param pktBytes
     */
    abstract void handleSpecializedCommand(int cmd, byte[] pktBytes);

    /**
     * Run once per loop, for custom game logic.
     */
    abstract void customRun();

    /**
     * Initializes the server instance.
     */
    private void initServer(){
        // Try to open a server socket on the given port
        // Note that we can't choose a port less than 1023 if we are not
        // privileged users (root)

        try {
            this.selector = Selector.open();

            // Create a server channel and make it non-blocking
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.configureBlocking(false);

            // Get the port number and bind the socket
            InetSocketAddress isa = new InetSocketAddress(this.getPort());
            channel.socket().bind(isa);

            // Register that the server selector is interested in connection requests
            channel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
        // Wait for something happen among all registered sockets
    }

    /**
     * Runs a lobby instance. A more in-depth explanation to come later. on implementation details.
     */
    public void runServer() {
        try {
            while (!terminated)
            {
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
                    this.key = (SelectionKey)readyItor.next();

                    // Remove current entry
                    readyItor.remove();

                    // Accept new connections, if any
                    if (key.isAcceptable()){
                        this.cchannel = ((ServerSocketChannel)key.channel()).accept();
                        this.cchannel.configureBlocking(false);
                        System.out.println("Accepted a connection from " + cchannel.socket().getInetAddress() + ":" + cchannel.socket().getPort());

                        // creates a new player, with the firstPlayer boolean true if no other players in the hashmap
                        Player newplayer = new Player(this.playerNetHash.isEmpty());
                        // set up a player for this connection
                        this.playerNetHash.put(this.maxIntKey, newplayer);
                        this.msgNetHandle.put(this.maxIntKey, new TCPMessageHandler());

                        //key.attach(this.maxIntKey); // attach an key to the key because a key is not a key if it does not contain a key within the key.
                        // Register the new connection for read operation
                        this.cchannel.register(this.selector, (SelectionKey.OP_READ | SelectionKey.OP_WRITE), this.maxIntKey);

                        this.maxIntKey++;
                    }else{
                        this.cchannel = (SocketChannel)key.channel();

                        this.cplayer = this.playerNetHash.get(key.attachment()); // player for this connection
                        this.msghandler = this.msgNetHandle.get(key.attachment());
                        this.intkey = (Integer)key.attachment();

                        int cmdNum;
                        byte[] pktBytes;

                        if (key.isReadable()) {

                            // Open input and output streams
                            this.inBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);

                            // Read from socket
                            int bytesRecv = cchannel.read(inBuffer);
                            if (bytesRecv <= 0) {
                                this.disconnectedPlayers.add(this.playerNetHash.get(intkey)); // store player in list of possible reconnects
                                this.playerNetHash.remove(intkey);
                                this.msgNetHandle.remove(intkey);
                                System.out.println("read() error for a client, or a client connection has closed for player: " + cplayer.getUsername());
                                System.out.println("Adding them to the disconnected player pool for future reconnections.");
                                key.cancel();  // deregister the socket
                                continue;
                            }
                            inBuffer.flip();      // make buffer available for reading
                            try {

                                byte[] newData = new byte[this.inBuffer.remaining()];
                                this.inBuffer.get(newData, 0, newData.length);

                                this.msghandler.handleMessage(newData);
                                this.handleCommands();

                            } catch (BufferUnderflowException e) {
                                System.err.println("Buffer underflow error while reading from user " + this.playerNetHash.get((Integer) key.attachment()).getUsername());
                                e.printStackTrace();
                            } catch (BufferOverflowException e) {
                                System.err.println("Buffer overflow error while reading from user " + this.playerNetHash.get((Integer) key.attachment()).getUsername());
                                e.printStackTrace();
                            }
                        }else {
                            this.handleCommands();
                        }
                    }
                } // end of while (readyItor.hasNext())

                this.customRun();
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

    private void handleCommands(){
        try {

            if (this.msghandler.hasMessage()) {
                cmdNum = this.msghandler.getNextMessageType();
                pktBytes = this.msghandler.getNextMessage();

                // reallocate buffer to ensure it is clean
                this.inBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);

                if(this.canHandleCommand[cmdNum]){
                    int z = 0;
                    switch(cmdNum){
                        case 6: // all commands that the clients should not send
                        case 31:
                        case 32:
                        case 34:
                            this.sendInvalidCommand();
                            break;
                        case 2:
                            this.disconnectedPlayers.add(this.playerNetHash.get(intkey)); // store player in list of possible reconnects
                            this.playerNetHash.remove(intkey); // remove player from list
                            this.msgNetHandle.remove(intkey);
                            // send updated list w/o this player to all others
                            this.sendUpdates(key,31,this.stringToByteArr(this.playersToSendList(this.playerNetHash.keySet())),false);
                            break;
                        case 4:
                            System.out.println("Error received: " + ByteBuffer.wrap(pktBytes).getInt() + " from user " + this.playerNetHash.get((Integer)key.attachment()).getUsername()); // print error
                            break;
                        case 5:
                            if(DEBUG){
                                System.out.println("Echoing back: " + byteArrToString(pktBytes));
                            }
                            inBuffer.putInt(5);
                            inBuffer.putInt(pktBytes.length);
                            inBuffer.put(pktBytes);
                            this.inBuffer.flip();
                            z = cchannel.write(inBuffer); // echo back
                            inBuffer.flip();
                            break;
                        case 30:
                            this.sendUpdates(key,31,this.stringToByteArr(this.playersToSendList(this.playerNetHash.keySet())),false);
                            break;
                        case 33:
                            cplayer.setUsername(byteArrToString(pktBytes));
                            this.playerNetHash.replace(intkey,cplayer);
                            break;
                        default:
                            inBuffer.putInt(4);
                            inBuffer.putInt(99);
                            this.inBuffer.flip();
                            z = cchannel.write(inBuffer); // write unknown error
                            this.inBuffer.flip();
                            break;
                    }
                }else{
                    this.handleSpecializedCommand(cmdNum, pktBytes);
                }
            }
        }catch(IOException e){
            System.out.println("Error caught while handling commands...");
            e.printStackTrace();
        }
    }

    /**
     * Sends to the current channel that they send an invalid command.
     */
    void sendInvalidCommand(){
        this.inBuffer.clear();
        this.inBuffer.putInt(4); // command 4: error
        this.inBuffer.putInt(4); // error type
        inBuffer.flip();
        try{
            int z = cchannel.write(inBuffer);
        }catch(IOException e){
            System.err.println("failed sending invalid command error to player: " + this.cplayer.getUsername());
        }
        this.inBuffer.flip();
    }

    /**
     * Sends copied commands to multiple users. Used for updating the player list, canvas, etc.
     * @param sender The selectionkey of the sender
     * @param cmd The command number to send.
     * @param msg The message to send, as a byte array
     * @param sendToSender whether to send this message to the sender as well
     * @return true if a valid command, false if a not valid command is received or cmd length does not match string length.
     */
    boolean sendUpdates(SelectionKey sender, int cmd, byte[] msg, boolean sendToSender){
        if(DEBUG && msg != null){
            //System.out.println("SendUpdate argument dump:\n\tCommand: " + cmd + "\n\tMessage:" + this.byteArrToString(msg) + "\n\tMessage Length: " + msg.length + "\n\tCommand Length: " + cmdLen[cmd]);
        }else if(DEBUG){
            //System.out.println("SendUpdate argument dump:\n\tCommand: " + cmd + "\n\tEmpty message" + "\n\tMessage Length: 0" + "\n\tCommand Length: " + cmdLen[cmd]);
        }
        if(cmdLen[cmd] == -2){
            return false;
        }else if((cmdLen[cmd] != msg.length && cmdLen[cmd] != -1) || (msg == null && cmdLen[cmd] == 0)){ // if the command length does not match a fixed length command, return false
//            System.err.println("SendUpdate function used incorrectly. Argument dump:\n\tCommand: " + cmd + "\n\tMessage:" + this.byteArrToString(msg) + "\n\tMessage Length: " + msg.length + "\n\tCommand Length: " + cmdLen[cmd] + "\n\t############");
            return false;
        }else {
            for (Object kObj : this.selector.keys()) {
                    this.inBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);
                    SelectionKey k = (SelectionKey) kObj; // cast the key to the correct object
                    if ((k != sender || sendToSender) && !k.isAcceptable()) { // if we send to the sender, then send to all. Otherwise, send to all but the sender.
                        SocketChannel cchannelu = (SocketChannel)k.channel(); // create channel
                        /*
                        if (DEBUG){
                            System.out.println("Sending to: " + this.playerNetHash.get((Integer)k.attachment()).getUsername());
                        }
                         */
                        inBuffer.putInt(cmd);
                        if (cmdLen[cmd] == -1) {
                            inBuffer.putInt(msg.length);
                        }
                        if(cmdLen[cmd] != 0) {
                            inBuffer.put(msg);
                        }

                        ByteBuffer tb = this.inBuffer.duplicate();
                        tb.flip();
                        byte[] bytes = new byte[tb.remaining()];
                        tb.get(bytes,0,bytes.length);
                        StringBuilder sb = new StringBuilder();
                        String sb2 = "";
                        String sb3 = "";
                        for (byte b : bytes) {
                            sb.append(String.format("%02X  ", b));
                            if((b & 0xFF) == 0x0A){
                                sb2 += "\\n  ";
                            }else if((b & 0xFF) == 0x09){
                                sb2 += "\\t  ";
                            }else{
                                sb2 += ((char) b) + "   ";
                            }
                            sb3 += String.format("%-4s", b & 0xFF);
                        }
                        /*  // debug statement for packets
                        System.out.println("=======SENDING DATA=======");
                        System.out.println("Receiver: " + this.playerNetHash.get(k.attachment()).getUsername());
                        System.out.println(sb.toString());
                        System.out.println(sb2);
                        System.out.println(sb3);
                        System.out.println("==========================");
                        */
                        try {
                            this.inBuffer.flip();
                            int z = cchannelu.write(inBuffer);
                            this.inBuffer.flip();
                        } catch (IOException e) {
                            System.err.println("Failure attempting to send update message to player: " + this.playerNetHash.get((Integer) k.attachment()).getUsername() + "with message: " + msg);
                            e.printStackTrace();
                        }
                    }
            }
            this.inBuffer.flip();
            return true;
        }
    }

    /**
     * Sends copied commands to multiple users. Used for updating the player list, canvas, etc.
     * @param sendToKey who to send to
     * @param cmd The command number to send.
     * @param msg The message to send, as a byte array
     * @return true if a valid command, false if a not valid command is received or cmd length does not match string length.
     */
    boolean sendToPlayer(SelectionKey sendToKey, int cmd, byte[] msg){
        if(DEBUG && msg != null){
            //System.out.println("SendToPlayer argument dump:\n\tCommand: " + cmd + "\n\tMessage:" + this.byteArrToString(msg) + "\n\tMessage Length: " + msg.length + "\n\tCommand Length: " + cmdLen[cmd]);
        }else if(DEBUG){
            //System.out.println("SendToPlayer argument dump:\n\tCommand: " + cmd + "\n\tEmpty message" + "\n\tMessage Length: 0" + "\n\tCommand Length: " + cmdLen[cmd]);
        }
        if(cmdLen[cmd] == -2){
            return false;
        }else if((cmdLen[cmd] != msg.length && cmdLen[cmd] != -1) || (msg == null && cmdLen[cmd] == 0)){ // if the command length does not match a fixed length command, return false
            System.err.println("SendToPlayer function used incorrectly. Argument dump:\n\tCommand: " + cmd + "\n\tMessage:" + this.byteArrToString(msg) + "\n\tMessage Length: " + msg.length + "\n\tCommand Length: " + cmdLen[cmd] + "\n\t############");
            return false;
        }else {
            this.inBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);
            SocketChannel cchannelu = (SocketChannel)sendToKey.channel(); // create channel

            inBuffer.putInt(cmd);
            if (cmdLen[cmd] == -1) {
                inBuffer.putInt(msg.length);
            }
            if(cmdLen[cmd] != 0) {
                inBuffer.put(msg);
            }

            ByteBuffer tb = this.inBuffer.duplicate();
            tb.flip();
            byte[] bytes = new byte[tb.remaining()];
            tb.get(bytes,0,bytes.length);
            StringBuilder sb = new StringBuilder();
            String sb2 = "";
            String sb3 = "";
            for (byte b : bytes) {
                sb.append(String.format("%02X  ", b));
                if((b & 0xFF) == 0x0A){
                    sb2 += "\\n  ";
                }else if((b & 0xFF) == 0x09){
                    sb2 += "\\t  ";
                }else{
                    sb2 += ((char) b) + "   ";
                }
                sb3 += String.format("%-4s", b & 0xFF);
            }
            /* // debug statement for packets
            System.out.println("=======SENDING DATA=======");
            System.out.println("Receiver: " + this.playerNetHash.get(sendToKey.attachment()).getUsername());
            System.out.println(sb.toString());
            System.out.println(sb2);
            System.out.println(sb3);
            System.out.println("==========================");
            */
            try {
                this.inBuffer.flip();
                int z = cchannelu.write(inBuffer);
                this.inBuffer.flip();
            } catch (IOException e) {
                System.err.println("Failure attempting to send update message to player: " + this.playerNetHash.get((Integer)sendToKey.attachment()).getUsername() + "with message: " + msg);
                e.printStackTrace();
            }
            this.inBuffer.flip();
            return true;
        }
    }

    /**
     * Sends copied commands to multiple users. Used for updating the player list, canvas, etc.
     * @param sendTo who to send to (their username as a string)
     * @param cmd The command number to send.
     * @param msg The message to send, as a byte array
     * @return true if a valid command, false if a not valid command is received or cmd length does not match string length.
     */
    boolean sendToPlayer(String sendTo, int cmd, byte[] msg){
        SelectionKey key = null;
        for (Object kObj : this.selector.keys()) {
            try {
                SelectionKey k = (SelectionKey) kObj; // cast the key to the correct object
                if (this.playerNetHash.get((Integer) k.attachment()).getUsername() == sendTo && !k.isAcceptable()) { // if we send to the sender, then send to all. Otherwise, send to all but the sender.
                    key = k;
                }
            }catch(NullPointerException e){
            }
        }
        if(key == null){
            System.err.println("SendToPlayer failed to establish which player to send to. Username given: " + sendTo);
            return false;
        }else{
            return this.sendToPlayer(key,cmd,msg);
        }
    }

    /**
     * Returns a string of a player list, of whoever the keys tie to.
     */
    String playersToSendList(Set<Integer> keyset){
        String retStr = "";
        for(Integer k : keyset){
            Player tp = this.playerNetHash.get(k);
            retStr += ("" + tp.getUsername() + ',' + tp.getScore() + '\n');
        }
        return retStr;
    }

    /**
     * Turns a byte array to a string, as according to the encoding and deconding scheme defined by "charset".
     * @param inBytes
     * @return
     */
    public String byteArrToString(byte[] inBytes){
        if(inBytes == null){
            return "&null";
        }

        ByteBuffer inBuffera = ByteBuffer.allocateDirect(inBytes.length);
        CharBuffer cBuffera = CharBuffer.allocate(inBytes.length);

        inBuffera.put(inBytes);
        inBuffera.flip();

        this.decoder.decode(inBuffera, cBuffera, false);
        cBuffera.flip();
        return cBuffera.toString();
    }

    /**
     * Turns a string into a byte array, as according to the encoding and decoding scheme defined by "charset".
     * @param in
     * @return
     */
    public byte[] stringToByteArr(String in){
        if(in == null){
            return new byte[0];
        }

        ByteBuffer inBufferb = ByteBuffer.allocateDirect(in.length());
        CharBuffer cBufferb = CharBuffer.allocate(in.length());
        inBufferb.clear();
        // put name,score in buffer
        cBufferb = CharBuffer.allocate(in.length());
        cBufferb.clear();
        cBufferb.put(in);
        cBufferb.flip();
        byte[] outBytes = {32};
        try{
            inBufferb.put(encoder.encode(cBufferb));
            inBufferb.flip();
            outBytes = new byte[in.length()];
            for(int i = 0; i < in.length(); i++){
                outBytes[i] = inBufferb.get();
            }

        }catch(CharacterCodingException e){
        }

        cBufferb.flip();

        return outBytes;
    }

    /**
     * Initializes the cmd length array with how long each command is, -1 if n length, or -2 if the cmd is invalid.
     */
    private void initCmdLen(){
        this.cmdLen = new int[256];
        Arrays.fill(this.cmdLen, -2);
        // manually init valid commands
        this.cmdLen[1] = -1; // -1 == n length
        this.cmdLen[2] = 0;
        this.cmdLen[3] = -1;
        this.cmdLen[4] = 4;
        this.cmdLen[5] = -1;
        this.cmdLen[6] = 6;
        this.cmdLen[10] = 0;
        this.cmdLen[11] = -1;
        this.cmdLen[12] = -1;
        this.cmdLen[13] = -1;
        this.cmdLen[14] = 0;
        this.cmdLen[20] = 4;
        this.cmdLen[21] = -1;
        this.cmdLen[22] = -1;
        this.cmdLen[23] = -1;
        this.cmdLen[24] = 0;
        this.cmdLen[25] = 0;
        this.cmdLen[26] = 0;
        this.cmdLen[30] = 0;
        this.cmdLen[31] = -1;
        this.cmdLen[32] = 0;
        this.cmdLen[33] = -1;
        this.cmdLen[34] = -1;
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

    private void initCanHandle(){
        this.canHandleCommand = new boolean[256];
        Arrays.fill(this.canHandleCommand, false);
        // manually fill all commands that this can handle:
        // cannot handle cmd 1 or 3 due to lobby requiring game leader to be the first who connects
        this.canHandleCommand[2] = true;
        this.canHandleCommand[4] = true;
        this.canHandleCommand[5] = true;
        this.canHandleCommand[6] = true;
        // 10-14 are lobby specific commands
        // 20-26 are game specific commands
        this.canHandleCommand[30] = true;
        this.canHandleCommand[31] = true;
        this.canHandleCommand[32] = true;
        this.canHandleCommand[33] = true;
        this.canHandleCommand[34] = true;
        // 40-54 are game specific commands
    }

    public int[] getCmdLen(){
        return this.cmdLen;
    }

    public void terminate(){
        this.terminated = true;
    }

    HashMap<Integer, Player> getPlayerNetHash() {
        return playerNetHash;
    }

    Integer getMaxIntKey() {
        return maxIntKey;
    }

    ArrayList<Player> getDisconnectedPlayers() {
        return disconnectedPlayers;
    }

    Selector getSelector() {
        return selector;
    }
}
