package nowebsite.Maker.Locker;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportUtil {
    public enum ReportFormer{
        IO,
        THREAD_SLEEP,
        ENCRYPT_FAILURE,
        ILLEGAL_PASSWORD,
        DELETE_FILE,
        UNKNOWN;
        public @NotNull String getDescription(){
            if (this.equals(IO))return "io";
            if (this.equals(THREAD_SLEEP)) return "Thread.sleep";
            if (this.equals(ENCRYPT_FAILURE)) return "encrypt";
            if (this.equals(ILLEGAL_PASSWORD)) return "illegal password";
            if (this.equals(DELETE_FILE)) return "delete file";
            else return "unknown";
        }
    }
    public static void reportError(
            @NotNull Logger logger, Level reportLevel, Class<?> father,
            @NotNull Class<?> main, String usePlaceDescription, @NotNull ReportFormer description, Exception e
    ){
        if (father.equals(main)) logger.log(reportLevel, main + "(" + main.hashCode() + ") for " + usePlaceDescription + " just threw a(an) " + description.getDescription() + " exception.", e);
        else logger.log(reportLevel, father + " at " + main + "(" + main.hashCode() + ") for " + usePlaceDescription + " just threw a(an) " + description.getDescription() + " exception.", e);
    }
    public static void report(
            @NotNull Logger logger, Level reportLevel, Class<?> father,
            @NotNull Class<?> main, String usePlaceDescription, @NotNull ReportFormer description
    ){
        if (father.equals(main)) logger.log(reportLevel, main + "(" + main.hashCode() + ") for " + usePlaceDescription + " just threw a(an) " + description.getDescription() + " exception.");
        else logger.log(reportLevel, father + " at " + main + "(" + main.hashCode() + ") for " + usePlaceDescription + " just threw a(an) " + description.getDescription() + " exception.");
    }
}
