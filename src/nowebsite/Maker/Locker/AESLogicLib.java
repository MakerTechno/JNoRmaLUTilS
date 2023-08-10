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

/**This lib is a simple reference for these methods, let them clearer.*/
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
    public enum EFileIOModule{
        DEFAULT_RUN_ENCRYPT,
        RUN_WITH_FILENAME_ENCRYPT;
        @Contract(pure = true)
        public @NotNull IFileWriterSupplier getRun(){
            if (this.equals(DEFAULT_RUN_ENCRYPT)) return (inputFile, outputFile, inputStream, cipherOutputStream) -> {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    cipherOutputStream.write(buffer, 0, bytesRead);
                }
            };
            else return (inputFile, outputFile, inputStream, cipherOutputStream) -> {
                //Add file name and put to stream first.
                byte[] fileNameBytes = inputFile.getName().getBytes(StandardCharsets.UTF_8);
                cipherOutputStream.write(fileNameBytes.length);
                cipherOutputStream.write(fileNameBytes);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    cipherOutputStream.write(buffer, 0, bytesRead);
                }
            };
        }
    }

    public interface IFileWriterSupplier{
        void run(File inputFile, File outputFile, FileInputStream inputStream, CipherOutputStream cipherOutputStream) throws IOException;
    }

    /**Generate secret key, it supports the encrypt and decrypt progress
     * @param password The password which must be entered.
     * @param saltLength The salt byte array for generations, it may not be too large(over 32768).
     * @param iterations The iterations of the key. Suggest 10000 for best.
     * @param length The length for encode AES itself. Suggest {@link KeyLength#AES_128}
     */
    @Contract("_, _, _, _ -> new")
    private static @NotNull SecretKeySpec generateAESKey(@NotNull String password, int saltLength, int iterations, @NotNull KeyLength length) throws NoSuchAlgorithmException, InvalidKeySpecException, SaltInputException{
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
     * @param length The length for encode AES itself. Suggest {@link KeyLength#AES_128)
     */
    private static @NotNull SecretKeySpec generateAESKey(@NotNull String password, int iterations, KeyLength length) throws SaltInputException, NoSuchAlgorithmException, InvalidKeySpecException {
        return generateAESKey(password, 24, iterations, length);
    }

    /**Generate secret key, it supports the encrypt and decrypt progress
     * @param password The password which must be entered.
     * @param length The length for encode AES itself. Suggest {@link KeyLength#AES_128)
     */
    private static @NotNull SecretKeySpec generateAESKey(@NotNull String password, KeyLength length) throws SaltInputException, NoSuchAlgorithmException, InvalidKeySpecException {
        return generateAESKey(password, 10000, length);
    }

    /**Generate secret key, it supports the encrypt and decrypt progress
     * @param password The password which must be entered.
     */
    private static @NotNull SecretKeySpec generateAESKey(@NotNull String password) throws SaltInputException, NoSuchAlgorithmException, InvalidKeySpecException {
        return generateAESKey(password, KeyLength.AES_128);
    }

    /**Special check for the salt to make sure it will not throw any other errors.*/
    public static void checkSalt(int saltLength) throws SaltInputException {
        if (saltLength <= 0) throw new SaltInputException(SaltInputException.LESS_THAN_0);
        if (!((saltLength & (saltLength -1)) ==0)) throw new SaltInputException(SaltInputException.NOT_THE_POWER_OF_2);
        if (saltLength >32768) System.err.println("Warning: salt value was set over 32768");//警告超出32768
    }

    /**Simple encrypt the file and output to the output path.*/
    public static void encryptFile(File inputFile, File outputFile, String password, IFileWriterSupplier supplier) {
        try {
            SecretKeySpec secretKey = generateAESKey(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            try (FileInputStream inputStream = new FileInputStream(inputFile);
                 OutputStream outputStream = new FileOutputStream(outputFile);
                 CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
                iRunFileOutputByte(supplier, inputFile, outputFile, inputStream, cipherOutputStream);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException |
                 InvalidKeySpecException | SaltInputException e) {
            System.err.println(e);
        }
    }

    /**Encrypt the file WITH ITS FILE NAME and output to the output path. IT MUST DECRYPT WITH FILENAME。*/
    public static void encryptFileWithName(@NotNull File inputFile, File outputFile, String password) {
        encryptFile(inputFile, outputFile, password, EFileIOModule.RUN_WITH_FILENAME_ENCRYPT.getRun());
    }


    public static void iRunFileOutputByte(@NotNull IFileWriterSupplier supplier, File inputFile, File outputFile, FileInputStream inputStream, CipherOutputStream cipherOutputStream) throws IOException {
        supplier.run(inputFile, outputFile, inputStream, cipherOutputStream);
    }

    // 解密文件
    public static void decryptFile(File inputFile, File outputFile, String password) {
        try {
            SecretKeySpec secretKey = generateAESKey(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            try (InputStream inputStream = new FileInputStream(inputFile);
                 OutputStream outputStream = new FileOutputStream(outputFile);
                 CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException |
                 InvalidKeySpecException | SaltInputException e) {
            e.printStackTrace();
        }
    }

    public static String  decryptFileWithName(File inputFile, File outputFile, String password) {
        String fileName;
        try {
            SecretKeySpec secretKey = generateAESKey(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            try (InputStream inputStream = new FileInputStream(inputFile);
                 OutputStream outputStream = new FileOutputStream(outputFile);
                 CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher)) {
                // 读取文件名称信息
                int fileNameLength = cipherInputStream.read();
                byte[] fileNameBytes = new byte[fileNameLength];
                cipherInputStream.read(fileNameBytes);
                fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException |
                 InvalidKeySpecException | SaltInputException e) {
            e.printStackTrace();
            return "error";
        }
        return fileName;
    }

/*测试示例
    public static void main(String[] args) {
        String inputFile = "path/to/input/file";
        String encryptedFile = "path/to/encrypted/file";
        String decryptedFile = "path/to/decrypted/file";
        String password = "your_password";


        encryptFile(new File(inputFile), new File(encryptedFile), password);
        decryptFile(new File(encryptedFile), new File(decryptedFile), password);
    }*/
}
