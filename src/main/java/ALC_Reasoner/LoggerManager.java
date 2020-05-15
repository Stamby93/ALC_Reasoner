package ALC_Reasoner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;


/**
 * The Logger manager.
 */
public class LoggerManager {

    private LoggerManager() {
    }

    /**
     * Write info log.
     *
     * @param msg String The text to write on the log.
     * @param c   Class&lt;?&gt; The class that call the method.
     */
    public static void writeInfoLog(String msg, Class<?> c){

        Logger log = Logger.getLogger(c.getName());
        InputStream properties = LoggerManager.class.getClassLoader().getResourceAsStream("log4j.properties");
        PropertyConfigurator.configure(properties);
        log.info(msg + "\n");

    }

    /**
     * Write error log.
     *
     * @param msg String The text to write on the log.
     * @param c   Class&lt;?&gt; The class that call the method.
     */
    public static void writeErrorLog(String msg, Class<?> c){

        Logger log = Logger.getLogger(c.getName());
        InputStream properties = LoggerManager.class.getClassLoader().getResourceAsStream("log4j.properties");
        PropertyConfigurator.configure(properties);
        log.error(msg + "\n");

    }

    /**
     * Write debug log.
     *
     * @param msg String The text to write on the log.
     * @param c   Class&lt;?&gt; The class that call the method.
     */
    public static void writeDebugLog(String msg, Class<?> c) {
        Logger log = Logger.getLogger(c.getName());
        InputStream properties = LoggerManager.class.getClassLoader().getResourceAsStream("log4j.properties");
        PropertyConfigurator.configure(properties);
        log.debug(msg + "\n");
    }

    /**
     * Sets file.
     *
     * @param name String The name of the new .log file.
     * @param c    Class&lt;?&gt; The class that call the method.
     * @param overW boolean The flag that set the log file overwriting.
     */
    public static void setFile(String name, Class<?> c, boolean overW) {
        File file = new File ("LOG/"+c.getName()+"/"+name+".log");
        if(overW){
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.setProperty("logfile.name","LOG/"+c.getName()+"/"+name+".log");
    }


}
