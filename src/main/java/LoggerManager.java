import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 * The Logger manager.
 *      * {@link <a href="https://logging.apache.org/log4j/2.x/log4j-api/apidocs/index.html"> Apache Log4j</a> }
 * {@see Apache Log4j}
 */
public class LoggerManager {

    private static boolean enable = false;
    private LoggerManager() {
    }

    /**
     * Write info log.
     *
     * @param msg String The text to write on the log.
     * @param c   Class<?> The class that call the method.
     */
    public static void writeInfoLog(String msg, Class<?> c){
        if(enable) {
            Logger log = Logger.getLogger(c.getName());
            PropertyConfigurator.configure("log4j.properties");
            log.info(msg + "\n");
        }
    }

    /**
     * Write error log.
     *
     * @param msg String The text to write on the log.
     * @param c   Class<?> The class that call the method.
     */
    public static void writeErrorLog(String msg, Class<?> c){
        if(enable) {
            Logger log = Logger.getLogger(c.getName());
            PropertyConfigurator.configure("log4j.properties");
            log.error(msg + "\n");
        }
    }

    /**
     * Write debug log.
     *
     * @param msg String The text to write on the log.
     * @param c   Class<?> The class that call the method.
     */
    public static void writeDebugLog(String msg, Class<?> c) {
        if(enable) {
            Logger log = Logger.getLogger(c.getName());
            PropertyConfigurator.configure("log4j.properties");
            log.debug(msg + "\n");
        }
    }

    /**
     * Sets file.
     *
     * @param name String The name of the new .log file.
     * @param c   Class<?> The class that call the method.
     */
    public static void setFile(String name, Class<?> c) {
        File file = new File ("LOG/"+c.getName()+"/"+name+".log");
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.setProperty("logfile.name","LOG/"+c.getName()+"/"+name+".log");
        enable = true;
    }


}
