
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class TCPClient {
    //private static Player;
    public static volatile GameController gameController;
    public static volatile LobbyController lobbyController;
    private static String macAddress;
    private static DataOutputStream outBuffer;
    private static DataInputStream inBuffer;
    private static PrintWriter printWriter;
    private static Socket clientSocket;
    public static String ip;
    private static int port = 9000;
    private static int cmdFromUser;
    private static Object objFromUser;
    private static int cmdToUser;
    private static Object objToUser;
    private static String msgFromUser;
    private static int cmdReceived;
    private static int cmdSent;
    private static String msgReceived = null;
    private static String msgSent;
    private static int messageReceivedInt;
    private static short messageReceivedShort;
    private static int lenSent;
    private static int lenReceived;
    private static byte[] pktBytes;
    private static Request request;
    private static String playerName;
    private static Object[] sentObj;
    private static Player player;
    private static Message message;
    private static TCPMessageHandler msgHandler = new TCPMessageHandler();
    private static int totalCanvasPackets = 0;

    public static void runClient() {

        //get user information
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
        //String userName;

        macAddress = getMacAddress();

        boolean logout = false;
    }

    public static synchronized void initialize(int port) {

        // Initialize a client socket connection to the server
        try {
            clientSocket = new Socket(ip, port);
            outBuffer = new DataOutputStream(clientSocket.getOutputStream());
            inBuffer = new DataInputStream(clientSocket.getInputStream());
            printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (UnknownHostException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static IOException handleServerCommand() {
        //System.out.print("CURRENT TCP CLIENT THREAD BEGIN: (handleServerCommand):"  );
        //System.out.println(Thread.currentThread().getName());

        Player player;
        try {
            if(!msgHandler.hasMessage()){
                // Recieving server messages
                while(true){
                    byte[] stuff = new byte[inBuffer.available()];
                    for(int i = 0; i < stuff.length; i++){
                        stuff[i] = inBuffer.readByte();
                    }
                    //System.out.println(stuff.length);
                    if(stuff.length != 0){
                        boolean newcmd = msgHandler.handleMessage(stuff);
                        if(newcmd){
                            break;
                        }
                    }
                }
            }

            cmdReceived = msgHandler.getNextMessageType();
            pktBytes = msgHandler.getNextMessage();
            msgReceived = new String(pktBytes, StandardCharsets.US_ASCII);
            if(cmdReceived == 20){
                //System.out.println("Time received:::" + ByteBuffer.wrap(pktBytes).getInt());
            }
            //System.out.println("New command received at client:\n\tCommand: " + cmdReceived + "\tLength: " + pktBytes.length + "\n\tString representation: " + msgReceived);

            switch (cmdReceived) {
                case 4:
                    //Do something
                    break;
                case 5:
                    //System.out.println("Message Recieved: " + msgReceived);
                    break;
                // send game types
                case 11:
                    sentObj = new Object[1];
                    sentObj[0] = msgReceived;
                    request = new Request(11, sentObj);
                    lobbyController.sendCommand(request);
                    break;

                // send game leader
                case 12:
                    player = new Player(true);
                    playerName = msgReceived;
                    player.setUsername(playerName);
                    sentObj = new Object[1];
                    sentObj[0] = player;
                    request = new Request(12, sentObj);
                    while (lobbyController == null) {}
                    lobbyController.sendCommand(request);
//                    handleServerCommand();
                    break;
                case 14: // send game start
                    System.out.println("Made it to game start!");
                    lobbyController.sendCommand(new Request(14, null));
                    break;
                case 20: //send time left
                    Object timeLeft = (Integer) ByteBuffer.wrap(pktBytes).getInt();
                    sentObj = new Object[1];
                    sentObj[0] = timeLeft;
                    request = new Request(20, sentObj);
                    gameController.sendCommand(request);
                    break;
                case 21: //send draw options
                    sentObj = msgReceived.split(",");
                    request = new Request(21, sentObj);
                    gameController.sendCommand(request);
                    break;
                case 23: //send draw leader
                    player = new Player(true);
                    player.setUsername(playerName);
                    sentObj = new Object[1];
                    sentObj[0] = player;
                    request = new Request(23, sentObj);
                    while (gameController == null) { }
                    gameController.sendCommand(request);
                    break;
                case 24: //guessed correctly
                    sentObj = null;
                    request = new Request(24, sentObj);
                    gameController.sendCommand(request);
                    break;
                case 25: // new round
                    totalCanvasPackets = 0;
                    System.out.println("made it to new round");
                    sentObj = null;
                    request = new Request(25, sentObj);
                    while (gameController == null) { }
                    gameController.sendCommand(request);
                    break;
                case 26: // end of game
                    sentObj = null;
                    request = new Request(26, sentObj);
                    gameController.sendCommand(request);
                    break;
                case 31: // send list of players with their score
                    System.out.println(msgReceived);
                    List<Player> players = new ArrayList<>();
                    //Player player;
                    String[] allPlayers = msgReceived.split("\n");
                    for (String eachPlayer : allPlayers) {
                        String[] playerInfo = eachPlayer.split(",");
                        player = new Player();
                        player.setScore(Integer.parseInt(playerInfo[1]));
                        player.setUsername(playerInfo[0]);
                        players.add(player);
                    }

                    sentObj = new Object[players.size()];
                    sentObj = players.toArray(sentObj);

                    request = new Request(31, sentObj);
                    if (lobbyController != null) {
                        //System.out.println("in the lobby controller");
                        lobbyController.sendCommand(request);
                    }
                    if (gameController != null) {
                       // System.out.println("in the game controller");
                        gameController.sendCommand(request);
                    }
                    //Player player;
                    break;
                case 32: //get player name
                    break;
                case 34: //update user
                    lobbyController.sendCommand(new Request(34, new Object[]{msgReceived}));
                    break;
                case 41: // send all chat

                    //TO DO: asks about the format of the message
                    List<Message> messages = new ArrayList<>();
                    //Message message;
                    String[] allChats = msgReceived.split("\\r?\\n");
                    for (String chat: allChats) {
                        String[] eachChat = chat.split(",");
                        message = new Message(eachChat[1], eachChat[0]);
                        messages.add(message);
                    }

                    sentObj = new Object[messages.size()];
                    sentObj = messages.toArray(sentObj);

                    request = new Request(41, sentObj);
                    gameController.sendCommand(request);
                    break;
                case 43: //new message from server to client
                    String[] chat = msgReceived.split(",");
                    System.out.println("MESSAGE RECEIVED: " + msgReceived + " CURR TIME MILLIS: "+System.currentTimeMillis());
                    //new Message(messagebody, sentBye)
                    message = new Message(chat[1], chat[0]);
                    sentObj = new Object[1];
                    sentObj[0] = message;
                    request = new Request(43, sentObj);
                    gameController.sendCommand(request);
                    break;

                case 50: // reset image
                    sentObj = null;
                    request = new Request(50, sentObj);
                    gameController.sendCommand(request);
                    break;
                case 53:
                    totalCanvasPackets++;
                    System.out.println("TOTAL CANVAS PACKETS RECEIVED: " + totalCanvasPackets);
                    sentObj = new Object[1];
                    sentObj[0] = msgReceived;
                    request = new Request(53, sentObj);
                    gameController.sendCommand(request);
                    break;
                case 54:
                    //what is it?
                    break;
                default:

                    cmdSent = 4;
                    outBuffer.writeInt(cmdSent);
                    outBuffer.writeInt(99); //unknown command
                    break;
            }
        } catch (IOException e) {
            //System.out.print("CURRENT TCP CLIENT THREAD END: (handleServerCommand):"  );
            //System.out.println(Thread.currentThread().getName());
            return e;
        }
        //System.out.print("CURRENT TCP CLIENT THREAD END: (handleServerCommand):"  );
        //System.out.println(Thread.currentThread().getName());
        return null;
    }



    public static synchronized void sendFromUser(Request request) {
        //System.out.print("CURRENT TCP CLIENT THREAD: (sendFromUser):  ");
        //System.out.println(Thread.currentThread().getName());

        cmdFromUser = request.command;
        //objFromUser = request.arg;
        //handleUsercommand(cmdFromUser, objFromUser);

        try {

            switch (cmdFromUser) {

                case 1: //initial connection
                    cmdSent = 1;
                    msgFromUser = (String) request.arg[0];
                    playerName = msgFromUser;
                    ip = (String) request.arg[1];

                    TCPClient.initialize(9000); // initialize self
                    Main.t.start(); // start tcpclient receive thread

                    lenSent = msgFromUser.length() + 1; // with printWriter.println, it adds an extra char
                    System.out.println(msgFromUser);
                    System.out.println(lenSent);
                    outBuffer.writeInt(cmdSent);
                    outBuffer.writeInt(lenSent);
//                    outBuffer.writeBytes(msgFromUser + "\n");
                    printWriter.println(msgFromUser);

                    //waiting for response from server with command send game leader 12
                    System.out.println("running handleServerCommand...");
                    break;

                case 2: //close connection
                    cmdSent = 2;
                    outBuffer.writeInt(cmdSent);
                    //System.out.println(msgFromUser);
                    // TO DO: close connection from client here
                    clientSocket.close();
                    break;

                case 3: // reconnect
                    msgFromUser = (String) request.arg[0];
                    playerName = msgFromUser;
                    ip = (String) request.arg[1];

                    TCPClient.initialize(9000); // initialize self
                    Main.t.start(); // start tcpclient receive thread
                    //To Do: connect the client socket to the game instead of lobby
                    cmdSent = 3;

                    lenSent = msgFromUser.length() + 1; // with printWriter.println, it adds an extra char
                    System.out.println(msgFromUser);
                    System.out.println(lenSent);
                    outBuffer.writeInt(cmdSent);
                    outBuffer.writeInt(lenSent);
//                    outBuffer.writeBytes(msgFromUser + "\n");
                    printWriter.println(msgFromUser);

                    System.out.println("Case 3 end.");
//                    handleServerCommand();
                    break;

                case 10: //get game type
                    // not implemented in the GUI side since we have only one game
                    cmdSent = 10;
                    outBuffer.writeInt(cmdSent);
                    //System.out.println(msgFromUser);
                    //waiting for respone from server with command 11
//                    handleServerCommand();
                    break;

                case 13: //select game
                    cmdSent = 13;
                    msgFromUser = (String) request.arg[0];
                    lenSent = msgFromUser.length();
                    System.out.println(msgFromUser);
                    outBuffer.writeInt(cmdSent);
                    outBuffer.writeInt(lenSent);
                    outBuffer.writeBytes(msgFromUser);
                    break;


                case 22: //send chosen draw options
                    cmdSent = 22;
                    msgFromUser = (String) request.arg[0];
                    lenSent = msgFromUser.length();
                    System.out.println(msgFromUser);
                    outBuffer.writeInt(cmdSent);
                    outBuffer.writeInt(lenSent);
                    outBuffer.writeBytes(msgFromUser);
//                    printWriter.println(msgFromUser + "\n");
                    break;

                case 30: //get players and scores
                    //send the message
                    System.out.println("Requesting player names & scores...");
                    cmdSent = 30;
                    outBuffer.writeInt(cmdSent);
                    //System.out.println(msgFromUser);

                    //wait for server to send list of players with command 31
//                    handleServerCommand();
                    break;

                case 33: //send player names
                    //not used
                    break;

                case 40: //get all chat

                    cmdSent = 40;
                    outBuffer.writeInt(cmdSent);
                    printWriter.println(msgFromUser + "\n");

                    //wait for response from server with command 41
//                    handleServerCommand();
                    break;

                case 42: //send new message
                    cmdSent = 42;
                    Message message = (Message) request.arg[0];
                    String userName = message.getSentBy();
                    String messageBody = message.getMessageBody();
                    String timeStamp = message.getTimestamp();
                    msgFromUser = timeStamp + "," + userName + "," + messageBody;
                    System.out.println("MESSAGE SENT: " + msgFromUser);
                    lenSent = msgFromUser.length() + 1; // +1 for the \n below
                    outBuffer.writeInt(cmdSent);
                    outBuffer.writeInt(lenSent);
                    outBuffer.writeBytes(msgFromUser + "\n");
//                    printWriter.println(msgFromUser);

                    //wait for server to reply back the message to all client with command 43
//                    handleServerCommand();
                    break;

                case 51: //update image
                    cmdSent = 51;
                    msgFromUser = (String) request.arg[0];
                    lenSent = msgFromUser.length();
                    System.out.println(msgFromUser);
                    outBuffer.writeInt(cmdSent);
                    outBuffer.writeInt(lenSent + 1); // +1 because .println
//                    outBuffer.writeBytes(msgFromUser + "\n");
                    printWriter.println(msgFromUser);

                    //wait for server to reply back the frame to all client with command
//                    handleServerCommand();
                    break;

                case 52: //get updated image
                    //TO DO: additional features for reconnecting
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static int getCmd() {
        return cmdFromUser;
    }
    // this code is taken from
    // https://www.mkyong.com/java/how-to-get-mac-address-in-java/
    public static String getMacAddress() {
        InetAddress ip;
        String macAddress = null;
        try {

            ip = InetAddress.getLocalHost();
            System.out.println("Current IP address : " + ip.getHostAddress());

            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            byte[] mac = network.getHardwareAddress();

            System.out.print("Current MAC address : ");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            macAddress = sb.toString();
            System.out.println(macAddress);

        } catch (UnknownHostException e) {

            e.printStackTrace();

        } catch (SocketException e) {

            e.printStackTrace();

        }

        return macAddress;

    }
}