package nowebsite.Maker.Locker;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**This lib is a simple reference for these methods, let them clearer.
 * @author MakerTechno
 */
public class AESLogicLib {

    /**Default String for class.*/
    private static final String ALGORITHM = "AES";

    /**Special enum to avoid enter wrong AES format.*/
    public enum KeyLength{
        /**Use {@link KeyLength#getNum()} to return its number.*/
        AES_128,
        /**Use {@link KeyLength#getNum()} to return its number.*/
        AES_192,
        /**Use {@link KeyLength#getNum()} to return its number.*/
        AES_256;

        /**Here we have a method to return the right number.
         * @return An int */
        public int getNum(){
            if (this.equals(AES_128)) return 128;
            else if (this.equals(AES_192)) return 192;
            else return 256;
        }
    }

    private interface KeyGetter{
        SecretKeySpec getKey();
    }




    /**Generate secret key, it supports the encrypt and decrypt progress
     * @param password The password which must be entered.
     * @param saltLength The salt byte array for generations, it may not be too large(over 32768).
     * @param iterations The iterations of the key. Suggest 10000 for best.
     * @param length The length for encode AES itself. Suggest {@link KeyLength#AES_128}
     */
    @Contract("_, _, _, _ -> new")
    public static @NotNull SecretKeySpec generateAESKey(@NotNull String password, int saltLength, int iterations, @NotNull KeyLength length) throws NoSuchAlgorithmException, InvalidKeySpecException, SaltInputException{
        checkSalt(saltLength);
        byte[] salt = new byte[saltLength];//Init
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, length.getNum());//Get key generator by creating instance.
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = secretKeyFactory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
    /**Generate secret key, it supports the encrypt and decrypt progress
     * @param password The password which must be entered.
     * @param iterations The iterations of the key. Suggest 10000 for best.
     * @param length The length for encode AES itself. Suggest {@link KeyLength#AES_128}
     */
    public static @NotNull SecretKeySpec generateAESKey(@NotNull String password, int iterations, KeyLength length) throws SaltInputException, NoSuchAlgorithmException, InvalidKeySpecException {
        return generateAESKey(password, 24, iterations, length);
    }
    /**Generate secret key, it supports the encrypt and decrypt progress
     * @param password The password which must be entered.
     * @param length The length for encode AES itself. Suggest {@link KeyLength#AES_128}
     */
    public static @NotNull SecretKeySpec generateAESKey(@NotNull String password, KeyLength length) throws SaltInputException, NoSuchAlgorithmException, InvalidKeySpecException {
        return generateAESKey(password, 10000, length);
    }
    /**Generate secret key, it supports the encrypt and decrypt progress
     * @param password The password which must be entered.
     */
    public static @NotNull SecretKeySpec generateAESKey(@NotNull String password) throws SaltInputException, NoSuchAlgorithmException, InvalidKeySpecException {
        return generateAESKey(password, KeyLength.AES_128);
    }

    /**Special check for the salt to make sure it will not throw any other errors.*/
    private static void checkSalt(int saltLength) throws SaltInputException {
        if (saltLength <= 0) throw new SaltInputException(SaltInputException.LESS_THAN_0);
        if (!((saltLength & (saltLength -1)) ==0)) throw new SaltInputException(SaltInputException.NOT_THE_POWER_OF_2);
        if (saltLength >32768) System.err.println("Warning: salt value was set over 32768");//警告超出32768
    }



    /**Main calculate method.
     * @param inputFile The file will be encrypted/decrypted.
     * @param outputFile The finish output path.
     * @param password The password which the user inputted.
     * @param mode Encrypt or decrypt, Please use {@link Cipher#ENCRYPT_MODE}/{@link Cipher#DECRYPT_MODE} only.
     * @param withFileName True if you need to remember it. But be careful if the file was not doing the same before.
     * @return A string with fileName,
     * null if run with "no filename",
     * "error" if the progress failed or not the expected mode.*/
    public static String encryptOrDecryptFile(File inputFile, File outputFile, String password, int mode, boolean withFileName) {
        try {
            SecretKeySpec secretKey = generateAESKey(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(mode, secretKey);

            try (FileInputStream inputStream = new FileInputStream(inputFile);
                 OutputStream outputStream = new FileOutputStream(outputFile);
                 CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
                 CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
                if (mode == Cipher.ENCRYPT_MODE) {
                     return extraFileFunc(inputFile, inputStream, cipherOutputStream, withFileName, true);
                } else  if (mode == Cipher.DECRYPT_MODE){
                    return extraFileFunc(inputFile, cipherInputStream, outputStream, withFileName, false);
                } else {
                    return "error";
                }
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException |
                 InvalidKeySpecException | SaltInputException e) {
            e.printStackTrace();
            return "error";
        }
    }


    /**Simply encrypt the file and output to the output path.*/
    public static void encryptFile(File inputFile, File outputFile, String password) {
        encryptOrDecryptFile(inputFile, outputFile, password,Cipher.ENCRYPT_MODE, false);
    }
    /**Encrypt the file WITH ITS FILE NAME and output to the output path. IT MUST DECRYPT WITH FILENAME.*/
    public static String encryptFileWithName(@NotNull File inputFile, File outputFile, String password) {
        return encryptOrDecryptFile(inputFile, outputFile, password,Cipher.ENCRYPT_MODE, true);
    }
    /**Simply decrypt the file and output to the output path.*/
    public static void decryptFile(File inputFile, File outputFile, String password){
        encryptOrDecryptFile(inputFile, outputFile, password, Cipher.DECRYPT_MODE, false);
    }
    /**Decrypt the file WITH ITS ENCRYPTED FILE NAME and output to the output path. IT MUST BE ENCRYPTED WITH FILENAME.*/
    public static String decryptFileWithName(@NotNull File inputFile, File outputFile, String password) {
        return encryptOrDecryptFile(inputFile, outputFile, password,Cipher.DECRYPT_MODE, true);
    }


    /**Extra controllable method for different output style.*/
    public static <I extends InputStream,O extends OutputStream> String extraFileFunc(
            File inputFile, I inputStream, O outputStream, boolean withFilename, boolean isEncrypt
    ) throws IOException {
        String fileName;
        if (withFilename) {
            if (isEncrypt) {
                //Add filename and put to stream first.
                fileName = inputFile.getName();
                byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
                outputStream.write(fileNameBytes.length);
                outputStream.write(fileNameBytes);
            } else {
                //Get the filename first.
                int fileNameLength = inputStream.read();
                byte[] fileNameBytes = new byte[fileNameLength];
                inputStream.read(fileNameBytes);
                fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
            }
        } else {
            fileName = null;
        }
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return fileName;
    }

/*Test example
    public static void main(String[] args) {
        String inputFile = "path/to/input/file";
        String encryptedFile = "path/to/encrypted/file";
        String decryptedFile = "path/to/decrypted/file";
        String password = "your_password";


        encryptFile(new File(inputFile), new File(encryptedFile), password);
        decryptFile(new File(encryptedFile), new File(decryptedFile), password);
    }*/
}
