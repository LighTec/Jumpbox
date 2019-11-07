public class Message implements Comparable<Message>{

    private String messageBody;
    private Long timestamp;
    private String sentBy;

    public Message(String messageBody, String sentBy) {
        this.messageBody = messageBody;
        this.sentBy = sentBy;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public int compareTo(Message o) {
        return this.timestamp.compareTo(o.timestamp);
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getSentBy() {
        return sentBy;
    }
}