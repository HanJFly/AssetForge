package com.hjf.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
@Data
public class JwtUtils {
    @Value("${jwt.signKey}")
    private String signKey ;
    @Value("${jwt.expire}")
    private Long expire ;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(signKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     * @return
     */
    public String generateJwt(Map<String,Object> claims){
        return Jwts.builder()
                .claims().add(claims).and()
                .signWith(getSigningKey())
                .expiration(new Date(System.currentTimeMillis() + expire))
                .compact();
    }

    /**
     * 解析JWT令牌
     * @param jwt JWT令牌
     * @return JWT第二部分负载 payload 中存储的内容
     */
    public Claims parseJWT(String jwt){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
}
