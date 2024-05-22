package com.idep.Tokenizer.API;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import java.security.Key;
import io.jsonwebtoken.*;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

public class Tokenizer {

    static ObjectMapper objectMapper = new ObjectMapper();
    static CBService service = CBInstanceProvider.getServerConfigInstance();
	static String SECRET_KEY = null;
	static String issuer = null;
	static long ttlMillis = -1;
    static{
    	try{
    		JsonNode configNode = objectMapper.readTree(service.getDocBYId("WEBTokenConfiguration").content().toString());
    		SECRET_KEY = configNode.get("SECRET_KEY").textValue();
    		issuer = configNode.get("issuer").textValue();
    		ttlMillis = configNode.get("timeout").longValue();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public static String createToken(String data) {
        
    	JwtBuilder builder = null;
    	try{  		
    		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
	        long nowMillis = System.currentTimeMillis();
	        Date now = new Date(nowMillis);
	        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
	        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
	
	        builder = Jwts.builder().setId(data)
	                .setIssuedAt(now)
	                .setIssuer(issuer)
	                .signWith(signatureAlgorithm, signingKey);
	
	        if (ttlMillis >= 0) {
	            long expMillis = nowMillis + ttlMillis;
	            Date exp = new Date(expMillis);
	            builder.setExpiration(exp);
	        }
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
    	return builder.compact(); 
    }

    public static Claims verifyToken(String token) {  	
        
    	Claims claims = null;
    	try{    		
	    	claims = Jwts.parser()
	                .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
	                .parseClaimsJws(token).getBody();
			}
        catch (Exception e)
        {
        	e.printStackTrace();
        }   
    	return claims;
    }
}
