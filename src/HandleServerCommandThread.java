import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class HandleServerCommandThread extends Thread{

    private AtomicBoolean running = new AtomicBoolean();
    private TCPClient tcpClient;

    HandleServerCommandThread(TCPClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    public void terminate() {
        running.set(false);
    }

    @Override
    public void run() {
        running.set(true);
        while (running.get()) {
            IOException e = tcpClient.handleServerCommand();
            if (e != null) {
                terminate();
            }
        }
    }
}
