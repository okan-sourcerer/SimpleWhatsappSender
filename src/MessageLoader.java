import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MessageLoader {

    private File file;
    private PriorityQueue messages;
    private BufferedReader reader;
    private String fileName;
    private Logger logger;

    private String regex = "\\|-\\|"; // in our message file, we have to separate with |-| (AltGR + < = |)
    private char newMessageRegex = '~';

    public MessageLoader(String fileName){
        logger = Logger.getInstance();
        this.fileName = fileName;
        file = new File(fileName);
        messages = new PriorityQueue();
        try{
            reader = new BufferedReader(new FileReader(file));
        }catch (IOException e){
            logger.log("Problem occurred when opening the file: " + fileName);
        }
        populateMessages();
    }

    public PriorityQueue getMessages() {
        return messages;
    }

    public void populateMessages(){
        String line;
        StringBuilder builder = new StringBuilder(0);
        String[] specs;
        try{
            while((line = reader.readLine()) != null){

                if (line.length() != 0 && line.charAt(0) == newMessageRegex) { // this char will indicate new message if it is at the start.
                    if (builder.length() != 0){

                        specs = builder.subSequence(1, builder.length()).toString().split(regex); // each line should contain date, number or id, message in this order.
                        if (specs.length < 3) {
                            logger.log("Encountered badly formatted line. Skipping...\n\t\tLine:" + builder.toString());
                            continue;
                        }
                        Message msg = new Message(specs[0], specs[1], specs[2]);
                        addMessage(msg);
                        builder.setLength(0); // clear builder for next message
                    }
                    builder.append(line);
                }
                else{
                    builder.append(line).append("\n");
                }
            }

            // last message is not added to the queue because messages are being added everytime a new one is found.
            specs = builder.subSequence(1, builder.length()).toString().split(regex); // each line should contain date, number or id, message in this order.
            if (specs.length < 3) {
                logger.log("Encountered badly formatted line. Skipping...\n\t\tLine:" + builder.toString());
            }
            Message msg = new Message(specs[0], specs[1], specs[2]);
            addMessage(msg);
            builder.setLength(0);

        }
        catch (IOException e){
            logger.log("Problem occurred when reading " + fileName);
        }
    }

    public synchronized void addMessage(Message message){
        logger.log("Adding message to the list: " + message);
        messages.add(message);
    }

    public void addMessage(String dateTime, String id, String message){
        addMessage(new Message(dateTime, id, message));
    }
}
