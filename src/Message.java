import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Comparable<Message>{

    private String dateTime;
    private String userId;
    private String message;
    private LocalDateTime daTi;

    public Message(String dateTime, String userId, String message) {
        this.dateTime = dateTime; // format of time should be as dd MM yyyy HH:mm:ss
        this.userId = userId;
        this.message = message;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss");
        daTi = LocalDateTime.parse(dateTime, formatter);
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime dateTime(){
        return daTi;
    }

    @Override
    public String toString() {
        return "Message{User:" + userId + " message:" + message + " sending time:" + dateTime + "}";
    }

    /**
     * Compares time values of the messages. Message time format is: "dd MM yyyy HH:mm:ss"
     * @param msg : Message we are going to compare it to.
     * @return : If this message is earlier, returns negative; if msg is earlier, returns positive.
     */
    @Override
    public int compareTo(Message msg) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss");
        LocalDateTime currDateTime = LocalDateTime.parse(this.getDateTime(), formatter);
        LocalDateTime msgDateTime = LocalDateTime.parse(msg.getDateTime(), formatter);

        return currDateTime.compareTo(msgDateTime);
    }
}
