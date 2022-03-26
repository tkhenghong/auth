package com.awpghost.auth.utils.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtUtil {

    private final String SECRET_KEY;

    private final int jwtAliveSeconds;

    JwtUtil(@Value("${jwt.secret.key}") String SECRET_KEY,
            @Value("${jwt.alive.seconds}") int jwtAliveSeconds) {
        this.SECRET_KEY = SECRET_KEY;
        this.jwtAliveSeconds = jwtAliveSeconds;
    }

    /**
     * Generate JWT Token.
     *
     * @param userDetails: UserDetails object generated be Spring Security.
     * @return JWT string.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> grantedAuthorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        claims.put("grantedAuthorities", grantedAuthorities);
        claims.put("password", userDetails.getPassword());
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Create JWT Token
     *
     * @param claims:  A Map object that allows you to put some important information about the user.
     * @param subject: The User's username (typically).
     * @return A JWT token in string, made by io.jsonwebtoken library.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        // Jwts object does not exist in the latest version of io.jsonwebtoken library.
        return Jwts.builder()
                .setClaims(claims) // set claims
                .setSubject(subject) // The person who has authenticated successfully.
                .setIssuedAt(new Date(System.currentTimeMillis())) // Set creation date.
                .setExpiration(new Date(System.currentTimeMillis() + jwtAliveSeconds)) // Set expiration date. Set it to 10 hours from now.
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // Set encryption method
                .compact();
    }

    /**
     * Read the claims within the JWT token.
     *
     * @param token:          JWT token string.
     * @param claimsResolver: A Function which has A special way to get the property (an internal object) out of the Claims Object.
     *                        Like sending the whole function into another function.
     * @param <T>:            The output of the function.
     * @return Generic object which is the output of the function. For example, if your Function's output is String, then it will be string.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract Claims object of the JWT.
     *
     * @param token: JWT String.
     * @return Claims object which may contains some information about the User.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    /**
     * Extract the username from JWT.
     *
     * @param token: JWT String.
     * @return Username of the User.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the Date object from JWT about when the JWT is created.
     *
     * @param token: JWT String.
     * @return Date about the issue Date of the JWT.
     */
    public Date extractJWTIssuedDate(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Extract the Date object from JWT about when the JWT will be expired.
     *
     * @param token: JWT String.
     * @return Date about the expiry Date of the JWT.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * To know is the given JWT is expired or not.
     *
     * @param token: JWT String.
     * @return Boolean. If true, means expired. If false, means not expired.
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date()); // Check JWT expire date is before now.
    }

    /**
     * Check whether the JWT is valid or not, by matching the current user's username and expiry of the JWT.
     *
     * @param token:       JWT String.
     * @param userDetails: UserDetails object generated be Spring Security.
     * @return Boolean. If true, means JWT is valid. If false, means JWT is not valid.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
