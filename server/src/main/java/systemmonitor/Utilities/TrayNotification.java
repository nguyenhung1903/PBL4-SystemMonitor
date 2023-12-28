package systemmonitor.Utilities;
import java.awt.*;
import java.awt.TrayIcon.MessageType;

public class TrayNotification {
    public static MessageType ERROR = MessageType.ERROR;
    public static MessageType WARNING = MessageType.WARNING;
    public static MessageType INFO = MessageType.INFO;
    public static MessageType NONE = MessageType.NONE;


    public static void displayTray(String caption, String text, MessageType msgType) {
        try {
            //Obtain only one instance of the SystemTray object
            SystemTray tray = SystemTray.getSystemTray();

            //If the icon is a file
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            //Alternative (if the icon is on the classpath):
            //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

            TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
            //Let the system resize the image if needed
            trayIcon.setImageAutoSize(true);
            //Set tooltip text for the tray icon
            trayIcon.setToolTip("System tray icon demo");
            tray.add(trayIcon);

            trayIcon.displayMessage(caption, text, msgType);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
