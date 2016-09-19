package net.solarnetwork.node.loxone.client;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Crypto {

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	// Convert bytes into a hexadecimal string
	private static String bytesToHex(byte[] bytes) {
		Formatter formatter = new Formatter();
		
		for (byte b : bytes) {
			formatter.format("%02x", b);
		}

		String formatted = formatter.toString();
		
		formatter.close();
		
		return formatted;
	}
	
	// Convert hex string to UTF-8 string
	public static String hexToString(String hexString) {
		byte[] bytes = DatatypeConverter.parseHexBinary(hexString);
		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	// Create HMAC SHA1 hash
	public static String createHmacSha1Hash(String data, String key) {
		try{
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			return bytesToHex(mac.doFinal(data.getBytes()));
		}catch(NoSuchAlgorithmException ex){
			ex.printStackTrace();
		}catch(InvalidKeyException ex){
			ex.printStackTrace();
		}
		return null;
	}

}
