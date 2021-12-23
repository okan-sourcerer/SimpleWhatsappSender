import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Clicker implements ClipboardOwner {

    public static final int OPTION_MOVE = 0; // Mouse directly snaps to desired position
    public static final int OPTION_GLIDE = 1; // mouse glides to desired position

    private final Robot bot;
    private BufferedImage searchImage;
    private final Clipboard cb;

    private int iterationCount;
    private long timeSteps;
    private int option;

    public Logger logger;

    private final PriorityQueue messages;
    private final MessageLoader loader;

    private URI website;

    public Clicker() throws AWTException{
        // when capturing screenshot, resolution is not native resolution. This property needs to be set.
        System.setProperty("sun.java2d.uiScale", "1");

        logger = Logger.getInstance();
        try{
            website = new URI("https://web.whatsapp.com");
        }
        catch (URISyntaxException uri){
            logger.log("Problem occurred when parsing the given URI");
        }
        bot = new Robot();
        cb = Toolkit.getDefaultToolkit().getSystemClipboard(); // get system clipboard

        iterationCount = 200;
        timeSteps = 2;
        option = OPTION_GLIDE; // default is glide. Can be set to move through setOption method.

        try{
            searchImage = ImageIO.read(new File("firefox_search_dark.png")); // load search icon for dark theme
            // searchImage = ImageIO.read(new File("wp_search_light.png")); // load search icon for light theme
        }catch (IOException e){
            logger.log("Could not read the search icon. Please check icon name.");
        }

        loader = new MessageLoader("pendingMessages.txt"); // creates the class and reads the file.
        messages = loader.getMessages();
    }

    public void start(){
        while(messages.getSize() != 0){
            delayForMessage();
        }
    }

    /**
     * This method will wait until first element in the queue is ready to be sent.
     */
    private void delayForMessage(){
        Message msg = messages.get();
        // top message in the queue has a time that has already passed. We can not send this message.
        if (msg.dateTime().compareTo(LocalDateTime.now()) < 0){
            // log the message and reason.
            logger.log(msg.toString() + "\n\t\tSkipped: Sending time is already passed");

            // remove the message because it is invalid
            messages.remove();
            return;
        }
        long zonedMessage = ZonedDateTime.of(msg.dateTime(), ZoneId.systemDefault()).toEpochSecond();
        long delay = 30L; // 30 seconds of delay for Thread.sleep . This way, we will check every 30 second
        boolean looping = true;
        while(looping){
            long zonedNow = ZonedDateTime.now(ZoneId.systemDefault()).toEpochSecond();
            if (zonedMessage < zonedNow + 30){
                delay = zonedMessage - zonedNow;
                looping = false;
            }
            try{
                Thread.sleep(delay * 1000);
            }
            catch (InterruptedException e){
                logger.log("Problem occurred in Thread.sleep in Clicker.delayForMessage method.");
            }
        }
        messages.remove();
        searchChat(msg);
    }

    /**
     * Connection to web.whatsapp.com . Assumes user already logged in.
     * Thread.sleep is necessary. If internet speed or browser launching speed is not fast enough,
     * user might need to increase Thread.sleep timeout.
     */
    public void connect(){
        try{
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(website);
            logger.log("Connecting to URI: " + website.getHost());
            Thread.sleep(10000); // wait 10 seconds for website to load.
        } catch (IOException io){
            logger.log("Problem occurred when connecting to website. Clicker.connect");
        } catch (InterruptedException inter){
            logger.log("Thread.sleep caused error in Clicker.connect.");
        }
    }

    /**
     * This method uses other methods in this class to find the chatRoom, type the message and send.
     * @param msg : message object that holds message data.
     */
    public void searchChat(Message msg){
        String chatID = msg.getUserId();
        String message = msg.getMessage();
        BufferedImage screen = bot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        int[] pos = findSubimage(screen, searchImage);

        if (pos[0] < 0 || pos[1] < 0){
            logger.log("Reconnecting...");
            connect();

            screen = bot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            pos = findSubimage(screen, searchImage);
        }

        mouseMove(pos[0], pos[1], option, getIterationCount(), getTimeSteps());
        mouseClick();

        logger.log(msg.toString());

        // enter predefined number or group name.
        cb.setContents(new StringSelection(chatID), this);
        try{
            Thread.sleep(100); // this delay is necessary. Otherwise other messages all goes to same person.
        }catch (InterruptedException e){
            logger.log("Problem occurred in Thread.sleep. Clicker.searchChat");
        }

        pasteString();

        try{
            Thread.sleep(1000); // it may take time to load the correct person
        }catch (InterruptedException e){
            logger.log("Problem occurred in Thread.sleep. Clicker.searchChat");
        }

        bot.keyPress(KeyEvent.VK_ENTER);
        bot.keyRelease(KeyEvent.VK_ENTER);

        try{
            Thread.sleep(1000); // it may take time to load previous conversations with the person
        }catch (InterruptedException e){
            logger.log("Problem occurred in Thread.sleep. Clicker.searchChat");
        }
        // enter predefined message
        cb.setContents(new StringSelection(message), this);

        pasteString();

        /*
        bot.keyPress(KeyEvent.VK_ENTER);
        bot.keyRelease(KeyEvent.VK_ENTER);
         */
        // sending message is not yet implemented because i don't want to send message to wrong people :P
    }

    private void mouseClick(){
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private void pasteString(){ // to hide unnecessary code
        bot.keyPress(KeyEvent.VK_CONTROL);
        bot.keyPress(KeyEvent.VK_V);
        bot.keyRelease(KeyEvent.VK_V);
        bot.keyRelease(KeyEvent.VK_CONTROL);
    }

    /**
     * This method selects URL of the current website. Later on, this url will be checked to see whether we
     * are in the correct tab or not.
     * This method works in Mozilla Firefox, Google Chrome, Microsoft Edge.(These are the only ones i tested.)
     */
    private void selectURL(){
        bot.keyPress(KeyEvent.VK_ALT);
        bot.keyPress(KeyEvent.VK_D);

        bot.keyRelease(KeyEvent.VK_D);
        bot.keyRelease(KeyEvent.VK_ALT);
    }

    /**
     * This method is a gateway method for selecting between different mouse movement options.
     * @param endX : our target x coordinate
     * @param endY : our target y coordinate
     * @param OPTION : mouse movement option. Mouse either directly snaps or glides.
     * @param iteration : amount of iteration for smoothing mouse movement. more it is, slower the mouse movement will get
     *                  If option is OPTION_MOVE, this value is ignored
     * @param timeSteps : delay between each step. lower is faster
     *                  If option is OPTION_MOVE, this value is ignored
     */
    private void mouseMove(int endX, int endY, int OPTION, int iteration, long timeSteps){
        if(OPTION == OPTION_GLIDE){
            mouseGlide(endX, endY, iteration, timeSteps);
        } else{
            bot.mouseMove(endX, endY);
        }
    }

    /**
     * Mouse glides from current position to destination
     * @param endX : our target x coordinate
     * @param endY : our target y coordinate
     * @param iteration : amount of iteration for smoothing mouse movement. more it is, slower the mouse movement will get
     * @param timeSteps : delay between each step. lower is faster
     */
    private void mouseGlide(int endX, int endY, int iteration, long timeSteps){
        Point pos = MouseInfo.getPointerInfo().getLocation(); // current mouse pos
        double diffX =  endX - pos.getX(), diffY = endY - pos.getY(); // calc diff of positions
        double currentX = pos.getX(), currentY = pos.getY();
        for (int i = 0; i < iteration; i ++){
            currentX += diffX / iteration; // re-calc the pos
            currentY += diffY / iteration;
            bot.mouseMove((int)currentX, (int)currentY); // move mouse to re-calced pos
            try{
                Thread.sleep(timeSteps); // use Thread.sleep to make mouse smooth
            }
            catch (InterruptedException e){
                System.out.println("Skipped an iteration...");
            }
        }
    }

    /**
     * Finds the a region in one image that best matches another, smaller, image.
     * @param im1 bigger image
     * @param im2 small image to search in big image
     * @return middle of the searched image. If not found, returns closest match.
     */
    private int[] findSubimage(BufferedImage im1, BufferedImage im2){
        int w1 = im1.getWidth(); int h1 = im1.getHeight();
        int w2 = im2.getWidth(); int h2 = im2.getHeight();
        assert(w2 <= w1 && h2 <= h1);
        // will keep track of best position found
        int bestX = 0; int bestY = 0; double lowestDiff = Double.POSITIVE_INFINITY;
        // brute-force search through whole image (slow...)
        for(int x = 0;x < w1-w2;x++){
            for(int y = 0;y < h1-h2;y++){
                double comp = compareImages(im1.getSubimage(x,y,w2,h2),im2);
                if(comp < lowestDiff){
                    bestX = x; bestY = y; lowestDiff = comp;
                }
                if (lowestDiff == 0){ // we already have a match. No need to search further
                    break;
                }
            }
        }


        // output similarity measure from 0 to 1, with 0 being identical
        // return best location
        return new int[]{bestX + im2.getWidth() / 2, bestY + im2.getWidth() / 2};
    }

    /**
     * Determines how different two identically sized regions are.
     * @param im1 exact sized part of the big image.
     * @param im2 small image
     * @return If im1 is the searched image, then should return 0.
     */
    private double compareImages(BufferedImage im1, BufferedImage im2){
        assert(im1.getHeight() == im2.getHeight() && im1.getWidth() == im2.getWidth());
        double variation = 0.0;
        for(int x = 0;x < im1.getWidth();x++){
            for(int y = 0;y < im1.getHeight();y++){
                variation += compareARGB(im1.getRGB(x,y),im2.getRGB(x,y))/Math.sqrt(3);
                if(variation > 0){ // we need identical image, if there is variation, then it is not our icon
                    return variation;
                }
            }
        }
        return variation/(im1.getWidth()*im1.getHeight());
    }

    /**
     * Calculates the difference between two ARGB colours (BufferedImage.TYPE_INT_ARGB).
     */
    private double compareARGB(int rgb1, int rgb2){
        double r1 = ((rgb1 >> 16) & 0xFF)/255.0; double r2 = ((rgb2 >> 16) & 0xFF)/255.0;
        double g1 = ((rgb1 >> 8) & 0xFF)/255.0;  double g2 = ((rgb2 >> 8) & 0xFF)/255.0;
        double b1 = (rgb1 & 0xFF)/255.0;         double b2 = (rgb2 & 0xFF)/255.0;
        //double a1 = ((rgb1 >> 24) & 0xFF)/255.0; double a2 = ((rgb2 >> 24) & 0xFF)/255.0;
        // if there is transparency, the alpha values will make difference smaller
        //return a1*a2*Math.sqrt((r1-r2)*(r1-r2) + (g1-g2)*(g1-g2) + (b1-b2)*(b1-b2));
        return Math.sqrt((r1-r2)*(r1-r2) + (g1-g2)*(g1-g2) + (b1-b2)*(b1-b2));
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {

    }

    // Getters and setters. These values could be changed through a network.
    public synchronized int getIterationCount() {
        return iterationCount;
    }

    public synchronized long getTimeSteps() {
        return timeSteps;
    }

    public synchronized void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public synchronized void setTimeSteps(long timeSteps) {
        this.timeSteps = timeSteps;
    }

    public synchronized int getOption() {
        return option;
    }

    public synchronized void setOption(int option) {
        if(option == OPTION_MOVE || option == OPTION_GLIDE){
            this.option = option;
        }
    }
}
