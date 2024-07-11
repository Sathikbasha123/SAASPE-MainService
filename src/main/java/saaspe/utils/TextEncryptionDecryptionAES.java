package saaspe.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class TextEncryptionDecryptionAES {

	private TextEncryptionDecryptionAES() {
		throw new IllegalStateException("Utility class");
	}

	static Cipher cipher;

	public static SecretKey keyGenerator() throws NoSuchAlgorithmException, NoSuchPaddingException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(128);
		SecretKey secretKey = keyGenerator.generateKey();
		cipher = Cipher.getInstance("AES/GCM/NoPadding");
		return secretKey;
	}

	public static String encrypt(String plainText, SecretKey secretKey)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		byte[] plainTextByte = plainText.getBytes();
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedByte = cipher.doFinal(plainTextByte);
		Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(encryptedByte);
	}

	public static String decrypt(String encryptedText, SecretKey secretKey)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] encryptedTextByte = decoder.decode(encryptedText);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
		return new String(decryptedByte);
	}

}
