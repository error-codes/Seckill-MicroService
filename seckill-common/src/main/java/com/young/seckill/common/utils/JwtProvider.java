package com.young.seckill.common.utils;

import com.young.seckill.common.exception.SeckillException;
import com.young.seckill.common.response.RespCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JwtProvider {

    // 加密算法
    private final static SecureDigestAlgorithm<SecretKey, SecretKey> ALGORITHM = Jwts.SIG.HS256;

    // Token私钥
    private final static String SECRET = "Pluto0928&YCR1207-Ivy0101&Oak1116-Kuromi2000&BaKu1998";

    // 密钥实例
    public static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Jwt签发者
    private final static String JWT_ISS = "YoungChengRan";

    // Jwt主题
    private final static String SUBJECT = "Yuan0907Yang1228SeckillUser";

    // Jwt接收者
    private final static String AUDIENCE = "Yuan1228Yang0917SeckillSystem";

    // Jwt内容负载数据
    private final static Map<String, Object> CLAIMS = new HashMap<>();

    static {
        Long nowDateTime = System.currentTimeMillis();
        CLAIMS.put(Claims.ISSUER, JWT_ISS);
        CLAIMS.put(Claims.SUBJECT, SUBJECT);
        CLAIMS.put(Claims.AUDIENCE, AUDIENCE);
        CLAIMS.put(Claims.ISSUED_AT, nowDateTime);
        CLAIMS.put(Claims.ID, UUID.randomUUID().toString());
    }

    /**
     * 生成Token令牌
     */
    private static String generateToken(Map<String, Object> claims) {
        return Jwts.builder().claims(claims).signWith(KEY, ALGORITHM).compact();
    }

    /**
     * 根据用户信息生成Token
     */
    public static String generateToken(Long userId) {
        CLAIMS.put(Claims.SUBJECT, String.valueOf(userId));
        return generateToken(CLAIMS);
    }

    /**
     * 解析Token
     */
    private static Claims getPayloadFromToken(String token) {
        return Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload();
    }

    /**
     * 校验Toke是否过期
     */
    private static boolean isExpiredToken(String token) {
        return !getPayloadFromToken(token).getExpiration().before(new Date());
    }

    /**
     * 根据Token获取用户Id
     */
    public static Long getUserIdFromToken(String token) {
        return Long.valueOf(getPayloadFromToken(token).getSubject());
    }

    /**
     * Token能否续期
     */
    public static boolean canRefresh(String token) {
        return isExpiredToken(token);
    }

    /**
     * Token续期
     */
    public static String refreshToken(String token) {
        if (canRefresh(token)) {
            Claims claims = getPayloadFromToken(token);
            Date newExpiredDate = new Date();
            claims.put("exp", newExpiredDate);
            claims.put("iat", newExpiredDate);
            return generateToken(claims);
        } else {
            throw new SeckillException(RespCode.TOKEN_EXPIRE);
        }
    }

}
