public class Message{

    private String messageBody;
    private String timestamp;
    private String sentBy;

    public Message(String messageBody, String sentBy) {
        this.messageBody = messageBody;
        this.sentBy = sentBy;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }


    public String getMessageBody() {
        return messageBody;
    }

    public String getSentBy() {
        return sentBy;
    }

    public String getTimestamp() {
        return timestamp;
    }
}