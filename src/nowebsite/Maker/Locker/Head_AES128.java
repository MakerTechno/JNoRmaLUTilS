package nowebsite.Maker.Locker;

import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Head_AES128 {

    private static final String ALGORITHM = "AES";

    // 生成密钥
    private static @NotNull SecretKeySpec generateKey(@NotNull String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[16]; // 盐值，可以是随机生成的字节数组
        // 迭代次数和密钥长度
        int iterations = 10000;
        int keyLength = 128;

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = secretKeyFactory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    // 加密文件
    public static void encryptFile(File inputFile, File outputFile, String password) {
        try {
            SecretKeySpec secretKey = generateKey(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            try (InputStream inputStream = new FileInputStream(inputFile);
                 OutputStream outputStream = new FileOutputStream(outputFile);
                 CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    cipherOutputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public static void encryptFileWithName(@NotNull File inputFile, File outputFile, String password) {
        try {
            SecretKeySpec secretKey = generateKey(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            try (InputStream inputStream = new FileInputStream(inputFile);
                 OutputStream outputStream = new FileOutputStream(outputFile);
                 CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
                // 添加文件名称信息
                byte[] fileNameBytes = inputFile.getName().getBytes(StandardCharsets.UTF_8);
                cipherOutputStream.write(fileNameBytes.length);
                cipherOutputStream.write(fileNameBytes);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    cipherOutputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    // 解密文件
    public static void decryptFile(File inputFile, File outputFile, String password) {
        try {
            SecretKeySpec secretKey = generateKey(password);
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
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public static String  decryptFileWithName(File inputFile, File outputFile, String password) {
        String fileName;
        try {
            SecretKeySpec secretKey = generateKey(password);
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
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException | InvalidKeySpecException e) {
            e.printStackTrace();
            return "error";
        }
        return fileName;
    }

/*
    // 测试示例
    public static void main(String[] args) {
        String inputFile = "path/to/input/file";
        String encryptedFile = "path/to/encrypted/file";
        String decryptedFile = "path/to/decrypted/file";
        String password = "your_password";


        encryptFile(new File(inputFile), new File(encryptedFile), password);
        decryptFile(new File(encryptedFile), new File(decryptedFile), password);
    }*/
}
