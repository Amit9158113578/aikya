package com.idep.proposal.util;

import java.io.UnsupportedEncodingException; 
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import com.idep.user.profile.processor.UserProfileReqProcessor;

/*
* This class provides method to encrypt the data
* 
* @author  Sandeep Jadhav
* @version 1.0
* @since   2015-12-18
*/

public class MD5Encryption { 
	
	static Logger log = Logger.getLogger(UserProfileReqProcessor.class.getName());

    private static String convertToHex(byte[] data) { 
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) { 
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do { 
                if ((0 <= halfbyte) && (halfbyte <= 9)) 
                    buf.append((char) ('0' + halfbyte));
                else 
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        } 
        return buf.toString();
    } 

    public static String MD5(String text) 
    throws NoSuchAlgorithmException, UnsupportedEncodingException, Exception  { 
    	log.info("MD5 input : "+text);
        MessageDigest md;
        md = MessageDigest.getInstance("MD5");
        byte[] md5hash = new byte[32];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        md5hash = md.digest();
        log.info("md5hash : "+md5hash);
        return convertToHex(md5hash);
    } 

} 
