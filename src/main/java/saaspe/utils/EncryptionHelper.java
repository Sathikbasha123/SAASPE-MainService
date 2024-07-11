package saaspe.utils;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionHelper {

	private EncryptionHelper() {
		throw new IllegalStateException("Utility class");
	}

	private static byte[] salt = new byte[] { 0x49, 0x76, 0x61, 0x6e, 0x20, 0x4d, 0x65, 0x64, 0x76, 0x65, 0x64, 0x65,
			0x76 };

	public static String encrypt(String key, String clearText) {
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 10000, 256);
			SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			byte[] iv = generateIV(); // Generating a random IV
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
			byte[] cipherText = cipher.doFinal(clearText.getBytes());

			byte[] encryptedData = new byte[iv.length + cipherText.length];
			System.arraycopy(iv, 0, encryptedData, 0, iv.length);
			System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

			return Base64.getEncoder().encodeToString(encryptedData).replace("/", ":");
		} catch (Exception e) {
			e.printStackTrace(); // Handle the exception properly based on your application needs
			return null;
		}
	}

	public static String decrypt(String key, String cipherText) {
		try {
			byte[] encryptedData = Base64.getDecoder().decode(cipherText.replace(":", "/"));

			byte[] iv = new byte[12]; // IV size for GCM mode is 12 bytes
			System.arraycopy(encryptedData, 0, iv, 0, iv.length);
			byte[] cipherTextBytes = new byte[encryptedData.length - iv.length];
			System.arraycopy(encryptedData, iv.length, cipherTextBytes, 0, cipherTextBytes.length);

			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 10000, 256);
			SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
			byte[] decryptedBytes = cipher.doFinal(cipherTextBytes);

			return new String(decryptedBytes);
		} catch (Exception e) {
			e.printStackTrace(); // Handle the exception properly based on your application needs
			return null;
		}
	}

	private static byte[] generateIV() {
		SecureRandom secureRandom = new SecureRandom();
		byte[] iv = new byte[12]; // IV size for GCM mode is recommended to be 12 bytes
		secureRandom.nextBytes(iv);
		return iv;
	}
}
