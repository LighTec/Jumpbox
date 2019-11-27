import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;

public class TCPMessageHandler {

    final int BUFFERSIZE = 64000; // 64k buffer, enough for a 140*140 uncompressed image max

    private int[] cmdLen = new int[256];
    private ByteBuffer inBuffer;
    private int state = 0;
    private int currentCmd = -1;
    private byte[] currentBytes = null;
    private int bytesFilled;
    private byte[] rem = null;

    private LinkedList<Integer> cmdQueue = new LinkedList<>();
    private LinkedList<byte[]> byteQueue = new LinkedList<>();

    public TCPMessageHandler(){
        initCmdLen();
    }

    public boolean handleMessage(byte[] newdata){

        StringBuilder sb = new StringBuilder();
        String sb2 = "";
        String sb3 = "";
        for (byte b : newdata) {
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
        System.out.println("=======RECEIVING DATA======");
        System.out.println(sb.toString());
        System.out.println(sb2);
        System.out.println(sb3);
        System.out.println("==========================");
        this.inBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);
        if(this.rem != null){
            this.inBuffer.put(rem);
            this.rem = null;
        }
        if(newdata.length == 0){
            return this.hasMessage();
        }
        System.out.println("Newdata length: " + newdata.length);
        this.inBuffer.put(newdata);
        this.inBuffer.flip();
        boolean con = true;
        while(this.inBuffer.hasRemaining() && con){
            switch(state){
                case 0:
                    if(this.inBuffer.remaining() < 4){
                        //System.out.println("Not enough data in buffer for command num, ending loop...");
                        con = false;
                    }else{
                        int cmd = this.inBuffer.getInt();
                        this.state = 1;
                        this.currentCmd = cmd;
                        //System.out.println("Processing command: " + this.currentCmd);
                    }
                    break;
                case 1:
                    // error handling
                    try {
                        int temp = this.cmdLen[this.currentCmd];
                    }catch(ArrayIndexOutOfBoundsException e){
                        System.err.println("Invalid command sent: " + this.currentCmd);
                        this.state = 0;
                        this.currentCmd = -1;
                        break;
                    }
                    System.out.println("Command to parse length for: " + this.currentCmd);
                    if(this.currentCmd == 0){
                        throw new UnknownError("Command 0 error! Where does the zero come from?!?");
                    }
                    if(this.cmdLen[this.currentCmd] == -1){
                        if(this.inBuffer.remaining() < 4){
                            //System.out.println("Not enough data in buffer for command len, ending loop...");
                            con = false;
                        }else{
                            int len = this.inBuffer.getInt();
                            System.out.println("Command length received: " + len);
                            this.state = 2;
                            this.currentBytes = new byte[len];
                            this.bytesFilled = 0;
                        }
                    }else if(this.cmdLen[this.currentCmd] != -2){
                        this.currentBytes = new byte[this.cmdLen[this.currentCmd]];
                        System.out.println("Byte buffer size for cmd args:  " + this.currentBytes.length);
                        if(this.cmdLen[this.currentCmd] == 0){
                            this.state = 0;
                            this.cmdQueue.add(this.currentCmd);
                            this.byteQueue.add(this.currentBytes);
                            this.currentCmd = -1;
                            this.currentBytes = null;
                        }else{
                            this.state = 2;
                            this.bytesFilled = 0;
                        }
                    }else{
                        //System.out.println("Message handler given invalid command... Clearing buffer.");
                        //System.out.println("Command given: " +this.currentCmd);
                        this.inBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);
                        this.state = 0;
                        this.currentCmd = -1;
                    }
                    break;
                case 2:
                    this.currentBytes[bytesFilled] = this.inBuffer.get();
                    this.bytesFilled++;
                    if(bytesFilled >= this.currentBytes.length){
                        System.out.println("Command sucessfully received: " + this.currentCmd);
                        //System.out.println("Byte buffer remaining: " +this.inBuffer.remaining());
                        this.byteQueue.add(this.currentBytes);
                        this.cmdQueue.add(this.currentCmd);

                        this.currentCmd = -1;
                        this.currentBytes = null;
                        this.state = 0;
                    }
                    break;
                default:
                    System.err.println("TCPMessage switch error! You should never see this.");
                    break;
            }
        }
        if(this.inBuffer.remaining() > 0){
            this.rem = new byte[this.inBuffer.remaining()];
            this.inBuffer.get(rem,0,rem.length);
        }
        //System.out.println("Buffer remaining to write to after op: " + this.inBuffer.remaining());
        if(!this.cmdQueue.isEmpty()){
            System.out.println(this.cmdQueue);
        }
        return !this.byteQueue.isEmpty();
    }

    public int getNextMessageType() throws IOException{
        if(this.byteQueue.isEmpty()){
            throw new IOException("No message is currently in queue to be read!");
        }
        //System.out.println("returning command number " + this.cmdQueue.peekFirst());
        //System.out.println("LL  " + this.cmdQueue);
        int cmd = this.cmdQueue.remove();
        //System.out.println("LL  " + this.cmdQueue);
        return cmd;
    }

    public byte[] getNextMessage() throws IOException {
        if(this.byteQueue.isEmpty()){
            throw new IOException("No message is currently in queue to be read!");
        }
        return this.byteQueue.removeFirst();
    }

    public boolean hasMessage(){
        return !this.byteQueue.isEmpty();
    }

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
}
