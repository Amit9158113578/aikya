package com.idep.restapi.utils;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class CreateJWTToken {

    public static String createFreshJWTToken(String secretKey) throws UnsupportedEncodingException {

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;
        String token = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
        String currentDateandTime = sdf.format(new Date());
        Date startDate = null;
        try {
            startDate = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z").parse(currentDateandTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        long nowMillis = cal.getTimeInMillis() - 12000L;

        Date now = new Date(nowMillis);
        final long ONE_MINUTE_IN_MILLIS = 60000;

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = new byte[0];
        apiKeySecretBytes = secretKey.getBytes("UTF-8");
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setSubject("KYC_WS_BROKER")
                .setHeaderParam("alg", "HS512")
                .setHeaderParam("typ", "JWT")
                .signWith(signatureAlgorithm, signingKey);

        long expMillis = nowMillis + (30 * ONE_MINUTE_IN_MILLIS);
        Date exp = new Date(expMillis);
        builder.setExpiration(exp);


        //Builds the JWT and serializes it to a compact, URL-safe string
        token = builder.compact();
        return "Bearer "+token;
    }
}