import java.io.IOException;

public class HandleServerCommandThread extends Thread{

    private boolean running = true;

    HandleServerCommandThread(){
    }
/*
    public void pauseTCP() {
        running = false;
    }

    public void resumeTCP(){
    }
*/
    @Override
    public void run() {
        while(true){
            while (running) {
                IOException e = TCPClient.handleServerCommand();
                if (e != null) {
                  //  pauseTCP();
                }
            }
        }
    }
}
