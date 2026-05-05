package com.banksim.auth_service.security.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@EnableConfigurationProperties(JwtProps.class)
@Configuration
public class JwtKeyConfig {

    @Value("${security.jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${security.jwt.public-key-path}")
    private String publicKeyPath;

    private static final String PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String PRIVATE_KEY_FOOTER = "-----END PRIVATE KEY-----";
    private static final String PUBLIC_KEY_HEADER = "-----BEGIN PUBLIC KEY-----";
    private static final String PUBLIC_KEY_FOOTER = "-----END PUBLIC KEY-----";


    @Bean
    public KeyPair keyPair() throws Exception {
        RSAPrivateKey privateKey = (RSAPrivateKey) readPrivateKey(privateKeyPath);
        RSAPublicKey publicKey = (RSAPublicKey) readPublicKey(publicKeyPath);
        return new KeyPair(publicKey, privateKey);
    }

    @Bean
    public RSAKey rsaKey(KeyPair keyPair) {
        try {
            String kid = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(keyPair.getPublic().getEncoded())
                    );
            return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyID(kid)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not supported\n", e);
        }
    }

    @Bean
    public JWKSet jwkSet(RSAKey rsaKey) {
        return new JWKSet(rsaKey);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSet jwkSet) {
        return new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(jwkSet));
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey,
                                 JwtProps jwtProps) throws JOSEException {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(jwtProps.issuer())
        ));
        return decoder;
    }

    private static PrivateKey readPrivateKey(String pemPath) throws Exception {
        String pem = Files.readString(Path.of(pemPath), StandardCharsets.UTF_8);
        String base64 = pem
                .replace(PRIVATE_KEY_HEADER, "")
                .replace(PRIVATE_KEY_FOOTER, "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(base64);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private static PublicKey readPublicKey(String pemPath) throws Exception {
        String pem = Files.readString(Path.of(pemPath), StandardCharsets.UTF_8);
        String base64 = pem
                .replace(PUBLIC_KEY_HEADER, "")
                .replace(PUBLIC_KEY_FOOTER, "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(base64);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(new X509EncodedKeySpec(der));
    }
}

