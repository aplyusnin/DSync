package ru.nsu.fit.dsync.utils;

import java.io.InputStream;
import java.security.MessageDigest;

public class Misc {

	/**
	 * Get hash of input
	 * @param str - string of hashing data
	 * @return String of data hash
	 * @throws Exception unable to hash input
	 */
	public static byte[] getSHA256Hash(String str) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.update(str.getBytes());
		return digest.digest();
	}

	/**
	 * Get hash of input
	 * @param stream - stream of hashing data
	 * @return String of data hash
	 * @throws Exception unable to hash input
	 */
	public static byte[] getSHA256Hash(InputStream stream) throws Exception{
		byte[] buffer = new byte[8192];

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		int count = 0;
		int off = 0;
		int size = 8192;
		while ((count = stream.read(buffer, off, size)) > 0)
		{
			digest.update(buffer, 0, count);
			off += count;
			if (count < size) break;
		}
		byte[] hash = digest.digest();
		return hash;
	}

	public static String bytesToHex(byte[] hash) {
		StringBuilder hexString = new StringBuilder(2 * hash.length);
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if(hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}
}
