import java.text.SimpleDateFormat;
import java.util.Date;

public class Message{

    private String messageBody;
    private String timestamp;
    private String sentBy;

    public Message(String messageBody, String sentBy) {
        this.messageBody = messageBody;
        this.sentBy = sentBy;

        Date date = new Date();
        date.setTime(989238232);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        this.timestamp = sdf.format(date);
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