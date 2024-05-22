package com.idep.kycapi.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public final class EncrypDecryptOperation {

	 //AESCrypt uses CBC and PKCS7Padding
    private static final String AES_MODE = "AES/CBC/ISO10126Padding";
    private static final String CHARSET = "UTF-8";
    static Logger log = Logger.getLogger(EncrypDecryptOperation.class.getName());

    /**
     * Encrypt and encode message using 256-bit AES with key generated from password.
     *
     * @param key     : secrete key
     * @param message the thing you want to encrypt assumed String UTF-8
     * @return Base64 encoded CipherText
     * @throws GeneralSecurityException if problems occur during encryption
     */
    
	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}

    public static String encrypt(final String key, String message, String IV)
        throws GeneralSecurityException {
    	String encoded = null;
        try {

            byte[] ivBytes = IV.getBytes();
            byte[] keyBytes = key.getBytes("UTF-8");

           final SecretKeySpec secreteKey = new SecretKeySpec(keyBytes, "AES");
            byte[] cipherText = encrypt(secreteKey, ivBytes, message.getBytes(CHARSET));
            encoded = Base64.encodeBase64String(cipherText);
            return encoded;
        } catch (UnsupportedEncodingException e) {
           e.printStackTrace();
        }
		return encoded;
    }

    /**
     * More flexible AES encrypt that doesn't encode
     *
     * @param key     AES key typically 128, 192 or 256 bit
     * @param iv      Initiation Vector
     * @param message in bytes (assumed it's already been decoded)
     * @return Encrypted cipher text (not encoded)
     * @throws GeneralSecurityException if something goes wrong during encryption
     */
    public static byte[] encrypt(final SecretKeySpec key, final byte[] iv, final byte[] message)
        throws GeneralSecurityException {
        byte[] cipherText = new byte[0];
        try {
            final Cipher cipher = Cipher.getInstance(AES_MODE);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            cipherText = cipher.doFinal(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    /**
     * Decrypt and decode ciphertext using 256-bit AES with key generated from password
     *
     * @param key
     * @param base64EncodedCipherText the encrpyted message encoded with base64
     * @return message in Plain text (String UTF-8)
     * @throws GeneralSecurityException if there's an issue decrypting
     */
    public static String decrypt(final String key, String base64EncodedCipherText, String IV)
            throws GeneralSecurityException {
        	String message = null;
            try {
                byte[] ivBytes = IV.getBytes();
                byte[] keyBytes = key.getBytes("UTF-8");
                log.info("base64EncodedCipherText :"+base64EncodedCipherText);
                final SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
                //byte[] decodedCipherText = Base64.getDecoder().decode(base64EncodedCipherText);
                byte[] decodedCipherText = Base64.decodeBase64(base64EncodedCipherText);

                log.info("decodedCipherText :"+decodedCipherText);
                byte[] decryptedBytes = decrypt(secretKey, ivBytes, decodedCipherText);
                
                message = new String(decryptedBytes, CHARSET);

                return message;
            } catch (UnsupportedEncodingException e) {
            	log.error("UnsupportedEncodingException :"+e.getMessage());
            } catch (Exception e) {
            	log.error("Exception :"+e.getMessage());
            }
    		return message;
        }

    /**
     * More flexible AES decrypt that doesn't encode
     *
     * @param key               AES key typically 128, 192 or 256 bit
     * @param iv                Initiation Vector
     * @param decodedCipherText in bytes (assumed it's already been decoded)
     * @return Decrypted message cipher text (not encoded)
     * @throws GeneralSecurityException if something goes wrong during encryption
     */
    public static byte[] decrypt(final SecretKeySpec key, final byte[] iv, final byte[] decodedCipherText)
        throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance(AES_MODE);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(decodedCipherText);
        return decryptedBytes;
    }

}