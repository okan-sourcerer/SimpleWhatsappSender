import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static Logger logger;
    private String logName;
    private File log;
    private BufferedWriter writer;
    private StringBuilder builder;

    private Logger(String logFileName){
        logName = logFileName;
        log = new File(logFileName);
        try{
            writer = new BufferedWriter(new FileWriter(log));
        }
        catch (IOException ex){
            System.out.println("Error occurred when opening log");
        }
        builder = new StringBuilder();
    }

    /**
     * @return : returns Logger object. If not created already, creates the object and log name will be time of creation.
     */
    public static Logger getInstance(){
        if (logger == null){
            logger = new Logger(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy__HH-mm-ss")) + ".txt");
        }
        return logger;
    }

    /**
     * Writes actions to the log file and resets actions for future use.
     * @param action logs the action to the file.
     */
    public void log(String action){
        try{
            builder.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss"))).append(":");
            builder.append(action).append("\n");
            writer.append(builder);
            System.out.println(builder);
            builder.delete(0, builder.capacity());
        }
        catch (IOException ex){
            System.out.println("Problem occurred when opening log file...");
        }finally {
            try {
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
