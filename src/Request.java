public class Request {
    public int command;
    public Object arg[];
    public Request(int command, Object arg[]) {
        this.command = command;
        this.arg = arg;
    }
}
