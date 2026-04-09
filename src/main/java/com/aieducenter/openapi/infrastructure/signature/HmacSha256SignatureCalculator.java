package com.aieducenter.openapi.infrastructure.signature;

import com.aieducenter.openapi.domain.port.SignatureCalculator;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * HMAC-SHA256 签名计算实现
 */
@Component
public class HmacSha256SignatureCalculator implements SignatureCalculator {

    private static final String ALGORITHM = "HmacSHA256";

    @Override
    public String calculate(String stringToSign, String secret) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), ALGORITHM
            );
            mac.init(keySpec);
            byte[] hash = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }
}
