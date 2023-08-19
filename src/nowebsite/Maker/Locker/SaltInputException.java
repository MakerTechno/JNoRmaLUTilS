package nowebsite.Maker.Locker;

/**This exception throws when a salt value created by Locker failed by the following reasons*/
public class SaltInputException extends Exception{
    public static final String LESS_THAN_0 = "Salt value must be over than 0.";

    public SaltInputException(String message){
        super(message);
    }
}
