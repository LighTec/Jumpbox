public class Player {
    private String username;
    private String macAddr;
    private boolean isFirstPlayer;
    private boolean isDrawer;
    private int score;

    public Player(String username, String macAddr, boolean isFirstPlayer) {
        this.username = username;
        this.macAddr = macAddr;
        this.isFirstPlayer = isFirstPlayer;
        this.score = 0;
    }

    public Player(boolean isFirstPlayer){ // for initial connection
        this.username = "";
        this.macAddr = "-1";
        this.isFirstPlayer = isFirstPlayer;
        this.score = 0;
        this.isDrawer = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public boolean isFirstPlayer() {
        return isFirstPlayer;
    }

    public void setFirstPlayer(boolean firstPlayer) {
        isFirstPlayer = firstPlayer;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isDrawer() {
        return isDrawer;
    }

    public void setDrawer(boolean drawer) {
        isDrawer = drawer;
    }
}