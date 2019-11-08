import java.util.ArrayList;
import java.util.List;

public class GameController {

    private String currentPlayerName;
    private String serverIp;

    void initData(String currentPlayerName, String serverIp) {
        this.currentPlayerName = currentPlayerName;
        this.serverIp = serverIp;

    }

    private ChatBox chatBox;
    private Canvas canvas;
    private ArrayList<Player> players;
    private TCPClient tcpClient;

    private int timeRemaining;
    private int round;
    private String[] wordOptions;

    public GameController(){}

    public GameController(ArrayList<Player> players, TCPClient tcpClient) {
        this.chatBox = new ChatBox();
        this.canvas = new Canvas();
        this.players = players;
        this.tcpClient = tcpClient;
        this.round = 0;
    }

    public void newRound() {
        this.round = 0;
        resetCanvas();

        for (Player p : players) p.setDrawer(false);
    }

    public void setDrawer(Player player) {
        for (Player p : players) {
            if (p.getMacAddr().equals(player.getMacAddr())) {
                p.setDrawer(true);
            }
        }
    }



    private void resetCanvas() {
        // TODO
    }

    private void sendChosenWord() {
        // TODO send chosen word to tcp client
    }

    private void sendMessage() {
        // TODO send guess to tcp client
    }





    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public void setWordOptions(String[] wordOptions) {
        this.wordOptions = wordOptions;
        // TODO update gui
    }

    public void sendAllChat(List<Message> messages) {
        this.chatBox.clearMessages();
        for (Message m : messages) this.chatBox.addMessage(m);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Message> getAllChat() {
        return chatBox.getMessages();
    }

}