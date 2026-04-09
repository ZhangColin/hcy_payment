package com.aieducenter.openapi.domain.port;

/**
 * 签名计算端口
 */
public interface SignatureCalculator {
    /**
     * 计算签名
     *
     * @param stringToSign 待签名字符串（已排序拼接）
     * @param secret       签名密钥
     * @return 小写 hex 格式的签名结果
     */
    String calculate(String stringToSign, String secret);
}