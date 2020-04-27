import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public class LoggerManager {

    private LoggerManager() {
    }

    public static void writeInfoLog(String msg, Class<?> c){
        Logger log = Logger.getLogger(c.getName());
        PropertyConfigurator.configure("log4j.properties");
        log.info(msg+"\n");
    }

    public static void writeErrorLog(String msg, Class<?> c){
        Logger log = Logger.getLogger(c.getName());
        PropertyConfigurator.configure("log4j.properties");
        log.error(msg+"\n");
    }

    public static void writeDebugLog(String msg, Class<?> c) {
        Logger log = Logger.getLogger(c.getName());
        PropertyConfigurator.configure("log4j.properties");
        log.debug(msg+"\n");
    }

    public static void setFile(String name, Class<?> c) {
        File file = new File ("LOG/"+c.getName()+"/"+name+".log");
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.setProperty("logfile.name","LOG/"+c.getName()+"/"+name+".log");
    }


}
