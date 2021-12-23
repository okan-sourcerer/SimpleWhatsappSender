import java.awt.*;
import java.io.IOException;

public class Processes {

    public static void main(String[] args) throws AWTException, IOException{
        Clicker clicker = new Clicker();

        clicker.connect();

        clicker.setOption(Clicker.OPTION_GLIDE);

        clicker.start();
    }
}
