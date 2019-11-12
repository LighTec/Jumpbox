
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class TCPClient {
    //private static Player;
    public static GameController gameController;
    public static LobbyController lobbyController;
    private static int[] cmdLen;
    private static String macAddress;
    private DataOutputStream outBuffer;
    private DataInputStream inBuffer;
    private PrintWriter printWriter;
    private Socket clientSocket;
    private String ip;
    private int port;
    private int cmdFromUser;
    private Object objFromUser;
    private int cmdToUser;
    private Object objToUser;
    private String msgFromUser;
    private int cmdReceived;
    private int cmdSent;
    private String msgReceived = null;
    private String msgSent;
    private int messageReceivedInt;
    private short messageReceivedShort;
    private int lenSent;
    private int lenReceived;
    private byte[] pktBytes;
    private Request request;
    private String playerName;
    private Object[] sentObj;
    private Player player;
    private Message message;

    public TCPClient() {
        this.port = 9000;
        this.initCmdLen();
    }

    public TCPClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.initCmdLen();
    }

    public void runClient() {

        initCmdLen();

        //get user information
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
        //String userName;

        macAddress = getMacAddress();






        boolean logout = false;

        //System.out.println("Please enter a message to be sent to the server ('logout' to terminate): ");

    }

    public void initialize(int port) {

        // Initialize a client socket connection to the server
        try {
            clientSocket = new Socket(this.ip, port);
        } catch (UnknownHostException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
        // Initialize input and an output stream for the connection(s)
        /*PrintWriter outBuffer =
                new PrintWriter(clientSocket.getOutputStream(), true);

        BufferedReader inBuffer =
                new BufferedReader(new
                        InputStreamReader(clientSocket.getInputStream()));*/

        try {
            outBuffer = new DataOutputStream(clientSocket.getOutputStream());
            inBuffer = new DataInputStream(clientSocket.getInputStream());
            printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void handleServerCommand() {

        Player player;
        try {
            // Recieving server messages
            cmdReceived = inBuffer.readInt();
            System.out.println("cmdReceived: "+ cmdReceived);
            lenReceived = this.cmdLen[cmdReceived];
            System.out.println("cmdReceived: "+ cmdReceived);
            if (lenReceived == -2) {
                //send an error
            } else if (lenReceived == -1) {
                lenReceived = inBuffer.readInt();
                System.out.println("Length received: "+lenReceived);
                pktBytes = new byte[lenReceived];
                inBuffer.read(pktBytes);
                msgReceived = new String(pktBytes, StandardCharsets.UTF_8);
            } else if (lenReceived == 4) {
                messageReceivedInt = inBuffer.readInt();

            } else if (lenReceived == 2) {
                messageReceivedShort = inBuffer.readShort();
                //Short shortObject = new Short(messageReceivedShort);
                //messageReceivedInt = short.intValue();
            }


            switch (cmdReceived) {
                case 4:
                    //Do something
                    break;
                case 5:
                    System.out.println("Message Recieved: " + msgReceived);
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
                    lobbyController.sendCommand(request);
                    break;
                case 14: // send game port
                    Object gamePort = messageReceivedShort;
                    sentObj = new Object[1];
                    sentObj[0] = gamePort;
                    request = new Request(14, sentObj);
                    lobbyController.sendCommand(request);
                    break;
                case 20: //send time left
                    Object timeLeft = (Integer) messageReceivedInt;
                    sentObj = new Object[1];
                    sentObj[0] = timeLeft;
                    request = new Request(14, sentObj);
                    gameController.sendCommand(request);
                    break;
                case 21: //send draw options
                    sentObj = msgReceived.split(",");
                    request = new Request(21, sentObj);
                    gameController.sendCommand(request);
                    break;
                case 23: //send draw leader
                    player = new Player(true);
                    System.out.println("Drawer: " + playerName);
                    player.setUsername(playerName);
                    sentObj = new Object[1];
                    sentObj[0] = player;
                    request = new Request(23, sentObj);
                    gameController.sendCommand(request);

                    // TODO CONTINUE HERE ASK SERVER FOR DRAW OPTIONS
                    break;
                case 24: //guessed correctly
                    sentObj = null;
                    request = new Request(24, sentObj);
                    gameController.sendCommand(request);
                    break;
                case 25: // new round
                    sentObj = null;
                    request = new Request(25, sentObj);
                    gameController.sendCommand(request);
                    break;
                case 26: // end of game
                    sentObj = null;
                    request = new Request(26, sentObj);
                    gameController.sendCommand(request);
                    break;
                case 31: // send list of players with their score

                    List<Player> players = new ArrayList<>();
                    //Player player;
                    String[] allPlayers = msgReceived.split("\\r?\\n");
                    for (String eachPlayer : allPlayers) {
                        String[] playerInfo = eachPlayer.split(",");
                        player = new Player();
                        player.setScore(Integer.parseInt(playerInfo[0]));
                        player.setUsername(playerInfo[1]);
                        players.add(player);
                    }

                    sentObj = new Object[players.size()];
                    sentObj = players.toArray(sentObj);

                    request = new Request(31, sentObj);
                    gameController.sendCommand(request);

                    //Player player;
                    break;
                case 32: //get player name
                    break;
                case 34: //update user
                    //TO DO
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
            System.out.println(e);
        }
    }



    public void sendFromUser(Request request) {
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
                    lenSent = msgFromUser.length();
                    initialize(9000);
                    System.out.println(msgFromUser);
                    System.out.println(lenSent);
                    outBuffer.writeInt(cmdSent);
                    outBuffer.writeInt(lenSent);
//                    outBuffer.writeBytes(msgFromUser + "\n");
                    printWriter.println(msgFromUser);

                    //waiting for response from server with command send game leader 12
                    System.out.println("running handleServerCommand...");
                    handleServerCommand();
                    break;

                case 2: //close connection
                    cmdSent = 2;
                    outBuffer.writeInt(cmdSent);
                    //System.out.println(msgFromUser);
                    // TO DO: close connection from client here
                    clientSocket.close();
                    break;

                case 3: // reconnect
                    //To Do: connect the client socket to the game instead of lobby
                    cmdSent = 3;
                    msgFromUser = (String) request.arg[0];
                    playerName = msgFromUser;
                    lenSent = msgFromUser.length();
                    System.out.println(msgFromUser);
                    outBuffer.writeInt(cmdSent);
                    outBuffer.writeInt(lenSent);
//                    outBuffer.writeBytes(msgFromUser + "\n");
                    printWriter.println(msgFromUser);

                    handleServerCommand();
                    break;

                case 10: //get game type
                    // not implemented in the GUI side since we have only one game
                    cmdSent = 10;
                    outBuffer.writeInt(cmdSent);
                    //System.out.println(msgFromUser);
                    //waiting for respone from server with command 11
                    handleServerCommand();
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
//                    outBuffer.writeBytes(msgFromUser);
                    printWriter.println(msgFromUser + "\n");
                    break;

                case 30: //get players and scores
                    //send the message
                    cmdSent = 30;
                    outBuffer.writeInt(cmdSent);
                    //System.out.println(msgFromUser);

                    //wait for server to send list of players with command 31
                    handleServerCommand();
                    break;

                case 33: //send player names
                    //not used
                    break;

                case 40: //get all chat

                    cmdSent = 40;
                    outBuffer.writeInt(cmdSent);
                    printWriter.println(msgFromUser + "\n");

                    //wait for response from server with command 41
                    handleServerCommand();
                    break;

                case 42: //send new message
                    cmdSent = 42;
                    Message message = (Message) request.arg[0];
                    String userName = message.getSentBy();
                    String messageBody = message.getMessageBody();
                    String timeStamp = message.getTimestamp();
                    msgFromUser = timeStamp + "," + userName + "," + messageBody;
                    System.out.println(msgFromUser);
                    lenSent = msgFromUser.length();
                    outBuffer.writeInt(cmdSent);
                    outBuffer.writeInt(lenSent);
//                    outBuffer.writeBytes(msgFromUser + "\n");
                    printWriter.println(msgFromUser);

                    //wait for server to reply back the message to all client with command 43
                    handleServerCommand();
                    break;

                case 51: //update image
                    cmdSent = 51;
                    msgFromUser = (String) request.arg[0];
                    lenSent = msgFromUser.length();
                    System.out.println(msgFromUser);
                    outBuffer.writeInt(cmdSent);
                    outBuffer.writeInt(lenSent);
//                    outBuffer.writeBytes(msgFromUser + "\n");
                    printWriter.println(msgFromUser + "\n");

                    //wait for server to reply back the frame to all client with command
                    handleServerCommand();
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

    public void sentToUser() {

    }

    public int getCmd() {
        return cmdFromUser;
    }
    // this code is taken from
    // https://www.mkyong.com/java/how-to-get-mac-address-in-java/
    public String getMacAddress() {
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

    private void initCmdLen() {
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
}