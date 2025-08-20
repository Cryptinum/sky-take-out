package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
//        // 指定签名的时候使用的签名算法，也就是header那部分
//        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
//
//        // 生成JWT的时间
//        long expMillis = System.currentTimeMillis() + ttlMillis;
//        Date exp = new Date(expMillis);
//
//        // 设置jwt的body
//        JwtBuilder builder = Jwts.builder()
//                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
//                .setClaims(claims)
//                // 设置签名使用的签名算法和签名使用的秘钥
//                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
//                // 设置过期时间
//                .setExpiration(exp);
//
//        return builder.compact();

        // 以上是0.9.1版本的写法，以下是0.12.x版本的写法
        // 1. 生成密钥对象
        // 对于HS256算法，密钥的字节长度至少应该是 256位（32字节）
        // Keys.hmacShaKeyFor() 方法会确保密钥长度的安全性
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // 2. 计算过期时间
        Instant now = Instant.now();
        Date exp = Date.from(now.plus(ttlMillis, ChronoUnit.MILLIS));

        // 3. 构建JWT
        return Jwts.builder()
                // 设置自定义声明
                .claims(claims)
                // 设置签发时间
                .issuedAt(Date.from(now))
                // 设置过期时间
                .expiration(exp)
                // 使用生成的密钥和HS256算法进行签名
                .signWith(key)
                .compact();
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     * @param token     加密后的token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
//        // 得到DefaultJwtParser
//        Claims claims = Jwts.parser()
//                // 设置签名的秘钥
//                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
//                // 设置需要解析的jwt
//                .parseClaimsJws(token).getBody();
//        return claims;

        // 以上是0.9.1版本的写法，以下是0.12.x版本的写法
        // 1. 根据密钥字符串生成 SecretKey 对象
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // 2. 创建一个 JwtParser 实例
        // 使用 parserBuilder() 来构建解析器
        JwtParser parser = Jwts.parser()
                .verifyWith(key) // 使用 verifyWith 设置验证密钥
                .build();

        // 3. 解析JWT
        // parseSignedClaims(token) 会验证签名并返回包含Claims的Jws对象
        Jws<Claims> jws = parser.parseSignedClaims(token);

        // 4. 返回Claims
        return jws.getPayload();
    }

}
