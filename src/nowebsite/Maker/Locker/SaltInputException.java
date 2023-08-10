package nowebsite.Maker.Locker;

/**This exception throws when a salt value created by Locker failed by the following reasons*/
public class SaltInputException extends Exception{
    public static final String LESS_THAN_0 = "Salt value must be over than 0.";
    public static final String NOT_THE_POWER_OF_2 = "Salt value must be a power of 2.";

    public SaltInputException(String message){
        super(message);
    }
}
