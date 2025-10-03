package hris.hris.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.TimeZone;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Instant;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, Long employeeId, String employeeIdStr) {
        // Use WIB (Asia/Jakarta) timezone
        ZoneId jakartaZone = ZoneId.of("Asia/Jakarta");
        ZonedDateTime nowJakarta = ZonedDateTime.now(jakartaZone);
        ZonedDateTime expiryJakarta = nowJakarta.plusMinutes(expiration / 60000); // Convert milliseconds to minutes

        // Convert to Date for JWT
        Date currentTime = Date.from(nowJakarta.toInstant());
        Date expiryDate = Date.from(expiryJakarta.toInstant());

        return Jwts.builder()
                .setSubject(email)
                .claim("employeeId", employeeId)
                .claim("employeeCode", employeeIdStr)
                .claim("timezone", "Asia/Jakarta")
                .setIssuedAt(currentTime)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public Long getEmployeeIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("employeeId", Long.class);
    }

    public String getEmployeeCodeFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("employeeCode", String.class);
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token, String email) {
        final String tokenEmail = getEmailFromToken(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}