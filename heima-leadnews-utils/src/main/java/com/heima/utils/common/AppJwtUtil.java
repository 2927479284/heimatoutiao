package com.heima.utils.common;

import io.jsonwebtoken.*;

import java.util.*;

public class AppJwtUtil {

  // 设置token过期时间30分钟
  private static final long EXPIRE_TIME = 30 * 60 * 1000; // 30分钟
  // 加密KEY
  private static final String TOKEN_ENCRY_KEY = "MDk4ZjZiY2Q0NjIxZDM3M2NhZGU0ZTgzMjYyN2I0ZjY";

  // 生产ID
  public static String getToken(Long id) {
    Map<String, Object> claimMaps = new HashMap<>();
    claimMaps.put("id", id);
    long currentTime = System.currentTimeMillis();
    return Jwts.builder()
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(new Date(currentTime)) // 签发时间
            .setSubject("system") // 说明
            .setIssuer("heima") // 签发者信息
            .setAudience("app") // 接收用户
            .compressWith(CompressionCodecs.GZIP) // 数据压缩方式
            .signWith(SignatureAlgorithm.HS512, TOKEN_ENCRY_KEY) // 加密方式
            .setExpiration(new Date(currentTime + EXPIRE_TIME)) // 过期时间戳
            .addClaims(claimMaps) // cla信息
            .compact();
  }

  /**
   * 获取payload body信息
   *
   * @param token
   * @return
   */
  public static Claims getClaimsBody(String token) {
    try {
      Jws<Claims> jwt = Jwts.parser().setSigningKey(TOKEN_ENCRY_KEY).parseClaimsJws(token);
      return jwt.getBody();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 获取hearder body信息
   *
   * @param token
   * @return
   */
  public static JwsHeader getHeaderBody(String token) {
    Jws<Claims> jwt = Jwts.parser().setSigningKey(TOKEN_ENCRY_KEY).parseClaimsJws(token);
    return jwt.getHeader();
  }

  /**
   * 是否过期
   *
   * @param claims
   * @return 1：有效，0：无效
   */
  public static int verifyToken(Claims claims) {
    if (claims == null) {
      return 0;
    }
    // 当前时间在有效期范围内
    if(new Date().before(claims.getExpiration())){
      return 1;
    }
    return 0;
  }


}
