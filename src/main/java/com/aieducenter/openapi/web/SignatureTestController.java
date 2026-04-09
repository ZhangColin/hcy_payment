package com.aieducenter.openapi.web;

import com.aieducenter.openapi.application.ApiKeyQueryAppService;
import com.aieducenter.openapi.domain.port.SignatureCalculator;
import com.aieducenter.openapi.domain.service.SignatureService;
import com.cartisan.web.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 签名验证测试控制器
 *
 * <p>模拟两个异构系统之间的完整签名通信，覆盖常见业务场景：
 * <ul>
 *   <li>场景1: POST JSON — 创建类接口（如创建订单）</li>
 *   <li>场景2: GET 无参数 — 简单查询（如健康检查）</li>
 *   <li>场景3: GET 带查询参数 — 条件查询（如搜索订单）</li>
 *   <li>场景4: POST JSON + 查询参数 — 混合场景（如带渠道的转账）</li>
 * </ul>
 *
 * <p>所有 client 端接口均可在 Firefox 地址栏直接访问，传入 apiKey 参数即可。
 * 先通过 OpenApi 管理接口创建 ApiKey，然后复制 apiKey 值作为参数。</p>
 */
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Slf4j
public class SignatureTestController {

    private final ApiKeyQueryAppService apiKeyQueryAppService;
    private final SignatureCalculator signatureCalculator;
    private final SignatureService signatureService;

    @Value("${server.port:8080}")
    private int serverPort;

    // =====================================================================
    //  发送端 — 模拟客户端业务系统（Firefox 直接访问）
    //  每个 client 接口演示一种场景的签名组织方式
    // =====================================================================

    /**
     * 场景1: POST JSON — 创建类接口
     *
     * <p>最常见的场景：业务系统向支付系统提交一个 JSON 对象创建订单。</p>
     * <p>Firefox 访问: GET /api/v1/test/client/create-order?apiKey=xxx</p>
     */
    @GetMapping("/client/create-order")
    public ApiResponse<Map<String, Object>> testCreateOrder(@RequestParam String apiKey) {
        // 模拟业务系统提交的 JSON body
        String body = """
                {"businessOrderNo":"BIZ20260409001","amount":9900,"subject":"Python高级课程","body":"Python编程高级课程-第3期","notifyUrl":"https://example.com/notify","returnUrl":"https://example.com/return"}""";

        return sendSignedPost(apiKey, body, "/api/v1/test/server/create-order", Collections.emptyMap());
    }

    /**
     * 场景2: GET 无参数 — 简单查询
     *
     * <p>GET 请求没有 body，bodyDigest = SHA-256("")（空字符串的哈希）。</p>
     * <p>Firefox 访问: GET /api/v1/test/client/ping?apiKey=xxx</p>
     */
    @GetMapping("/client/ping")
    public ApiResponse<Map<String, Object>> testPing(@RequestParam String apiKey) {
        // GET 请求无 body，bodyDigest 固定为空字符串的 SHA-256
        return sendSignedGet(apiKey, "/api/v1/test/server/ping", Collections.emptyMap());
    }

    /**
     * 场景3: GET 带查询参数 — 条件查询
     *
     * <p>查询参数参与签名：key 按字典序排列后拼接到待签名字符串中。</p>
     * <p>Firefox 访问: GET /api/v1/test/client/search?apiKey=xxx</p>
     */
    @GetMapping("/client/search")
    public ApiResponse<Map<String, Object>> testSearch(@RequestParam String apiKey) {
        // 模拟查询参数: keyword=Python, status=PENDING, page=1
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("keyword", "Python");
        queryParams.put("status", "PENDING");
        queryParams.put("page", "1");

        return sendSignedGet(apiKey, "/api/v1/test/server/search", queryParams);
    }

    /**
     * 场景4: POST JSON + 查询参数 — 混合场景
     *
     * <p>body 通过 bodyDigest 参与签名，URL 查询参数也参与签名。</p>
     * <p>Firefox 访问: GET /api/v1/test/client/transfer?apiKey=xxx</p>
     */
    @GetMapping("/client/transfer")
    public ApiResponse<Map<String, Object>> testTransfer(@RequestParam String apiKey) {
        String body = """
                {"orderNo":"TF20260409001","amount":50000,"payeeAccount":"user001","remark":"月度结算"}""";

        // URL 上的查询参数也参与签名
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("channel", "BANK");
        queryParams.put("operator", "admin");

        return sendSignedPost(apiKey, body, "/api/v1/test/server/transfer", queryParams);
    }

    // =====================================================================
    //  接受端 — 模拟服务端受保护接口（需要 @RequireSignature）
    //  验证通过后回显收到的所有信息
    // =====================================================================

    /**
     * 场景1 接受端: POST JSON 创建订单
     */
    @PostMapping("/server/create-order")
    @RequireSignature
    public ApiResponse<Map<String, Object>> receiveCreateOrder(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        return echoResponse("创建订单", body, null, request);
    }

    /**
     * 场景2 接受端: GET 无参数
     */
    @GetMapping("/server/ping")
    @RequireSignature
    public ApiResponse<Map<String, Object>> receivePing(HttpServletRequest request) {
        return echoResponse("健康检查", null, null, request);
    }

    /**
     * 场景3 接受端: GET 带查询参数
     */
    @GetMapping("/server/search")
    @RequireSignature
    public ApiResponse<Map<String, Object>> receiveSearch(
            @RequestParam String keyword,
            @RequestParam String status,
            @RequestParam String page,
            HttpServletRequest request) {
        Map<String, Object> receivedParams = new LinkedHashMap<>();
        receivedParams.put("keyword", keyword);
        receivedParams.put("status", status);
        receivedParams.put("page", page);
        return echoResponse("搜索查询", null, receivedParams, request);
    }

    /**
     * 场景4 接受端: POST JSON + 查询参数
     */
    @PostMapping("/server/transfer")
    @RequireSignature
    public ApiResponse<Map<String, Object>> receiveTransfer(
            @RequestBody Map<String, Object> body,
            @RequestParam String channel,
            @RequestParam String operator,
            HttpServletRequest request) {
        Map<String, Object> receivedParams = new LinkedHashMap<>();
        receivedParams.put("channel", channel);
        receivedParams.put("operator", operator);
        return echoResponse("转账", body, receivedParams, request);
    }

    // =====================================================================
    //  内部方法 — 签名计算 & 发送 & 回显
    // =====================================================================

    /**
     * 发送带签名的 POST 请求
     */
    private ApiResponse<Map<String, Object>> sendSignedPost(
            String apiKey, String body, String path, Map<String, String> queryParams) {

        SignatureContext ctx = buildSignature(apiKey, body, queryParams);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = buildHeaders(ctx);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建带查询参数的 URL
        String url = buildUrl(path, queryParams);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class
            );
            return buildSuccessResult(ctx, response.getBody());
        } catch (HttpClientErrorException e) {
            return buildFailResult(ctx, e.getStatusCode().value(), e.getResponseBodyAsString());
        }
    }

    /**
     * 发送带签名的 GET 请求
     */
    private ApiResponse<Map<String, Object>> sendSignedGet(
            String apiKey, String path, Map<String, String> queryParams) {
        // GET 无 body，bodyDigest = SHA-256("")
        SignatureContext ctx = buildSignature(apiKey, null, queryParams);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = buildHeaders(ctx);

        String url = buildUrl(path, queryParams);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class
            );
            return buildSuccessResult(ctx, response.getBody());
        } catch (HttpClientErrorException e) {
            return buildFailResult(ctx, e.getStatusCode().value(), e.getResponseBodyAsString());
        }
    }

    /**
     * 核心签名计算逻辑 — 演示客户端应如何组织签名
     */
    private SignatureContext buildSignature(String apiKey, String body, Map<String, String> queryParams) {
        // Step 1: 用 X-App-Id 查找对应的 appSecret
        String apiSecret = apiKeyQueryAppService.getApiSecret(apiKey);

        // Step 2: 计算 bodyDigest
        //   POST: bodyDigest = hex(SHA-256(body.getBytes(UTF-8)))
        //   GET:  bodyDigest = hex(SHA-256(""))  因为没有请求体
        byte[] bodyBytes = body != null ? body.getBytes(StandardCharsets.UTF_8) : new byte[0];
        String bodyDigest = signatureService.calculateBodyDigest(bodyBytes);

        // Step 3: 生成系统参数
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = UUID.randomUUID().toString();

        // Step 4: 收集所有参与签名的参数，按 key 字典序排列
        //   固定参数: appId, bodyDigest, nonce, timestamp
        //   查询参数: URL 上的所有 ?key=value
        TreeMap<String, String> sortedParams = new TreeMap<>();
        sortedParams.put("appId", apiKey);
        sortedParams.put("bodyDigest", bodyDigest);
        sortedParams.put("nonce", nonce);
        sortedParams.put("timestamp", timestamp);
        if (queryParams != null && !queryParams.isEmpty()) {
            sortedParams.putAll(queryParams);
        }

        // Step 5: 拼接待签名字符串  key1=value1&key2=value2&...
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (!sb.isEmpty()) sb.append("&");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        String stringToSign = sb.toString();

        // Step 6: 计算签名  sign = hex(HMAC-SHA256(stringToSign, appSecret))
        String sign = signatureCalculator.calculate(stringToSign, apiSecret);

        // 构建签名详情（展示给调用方看）
        LinkedHashMap<String, Object> details = new LinkedHashMap<>();
        details.put("1.X-App-Id(即appId)", apiKey);
        details.put("2.appSecret(不传输,仅参与计算)", apiSecret.substring(0, 8) + "...");
        details.put("3.请求体body", body != null ? body : "(GET请求, 无body)");
        details.put("4.bodyDigest = SHA-256(body)", bodyDigest);
        details.put("5.timestamp(秒级)", timestamp);
        details.put("5.nonce(UUID)", nonce);
        if (queryParams != null && !queryParams.isEmpty()) {
            details.put("6.查询参数(queryParams)", queryParams);
        }
        details.put("7.待签名字符串(按key排序拼接)", stringToSign);
        details.put("8.签名 sign = HMAC-SHA256(待签名字符串, appSecret)", sign);

        return new SignatureContext(apiKey, bodyDigest, timestamp, nonce, sign, details);
    }

    /**
     * 构建 HTTP Headers（5 个签名相关 Header）
     */
    private HttpHeaders buildHeaders(SignatureContext ctx) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-App-Id", ctx.apiKey());
        headers.set("X-Timestamp", ctx.timestamp());
        headers.set("X-Nonce", ctx.nonce());
        headers.set("X-Sign", ctx.sign());
        headers.set("X-Body-Digest", ctx.bodyDigest());
        return headers;
    }

    /**
     * 构建带查询参数的 URL
     */
    private String buildUrl(String path, Map<String, String> queryParams) {
        StringBuilder url = new StringBuilder("http://localhost:" + serverPort + path);
        if (queryParams != null && !queryParams.isEmpty()) {
            url.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) url.append("&");
                url.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }
        return url.toString();
    }

    private ApiResponse<Map<String, Object>> buildSuccessResult(SignatureContext ctx, String responseBody) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("签名计算过程", ctx.details());
        result.put("发送的Headers", Map.of(
            "X-App-Id", ctx.apiKey(),
            "X-Timestamp", ctx.timestamp(),
            "X-Nonce", ctx.nonce(),
            "X-Sign", ctx.sign(),
            "X-Body-Digest", ctx.bodyDigest()
        ));
        result.put("服务端响应", responseBody);
        result.put("测试结果", "SUCCESS");
        return ApiResponse.ok(result);
    }

    private ApiResponse<Map<String, Object>> buildFailResult(SignatureContext ctx, int httpStatus, String errorBody) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("签名计算过程", ctx.details());
        result.put("服务端响应", errorBody);
        result.put("测试结果", "FAILED - HTTP " + httpStatus);
        return ApiResponse.ok(result);
    }

    /**
     * 接受端通用回显 — 签名验证通过后，返回所有收到的信息
     */
    private ApiResponse<Map<String, Object>> echoResponse(
            String scenario, Map<String, Object> body,
            Map<String, Object> queryParams, HttpServletRequest request) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("场景", scenario);
        result.put("签名验证", "通过");
        result.put("appId(X-App-Id)", request.getAttribute("verifiedAppId"));
        result.put("业务系统名称", request.getAttribute("verifiedBusinessSystemName"));
        if (body != null) {
            result.put("收到的请求体(body)", body);
        }
        if (queryParams != null) {
            result.put("收到的查询参数(query)", queryParams);
        }
        return ApiResponse.ok(result);
    }

    /**
     * 签名计算上下文
     */
    private record SignatureContext(
        String apiKey, String bodyDigest, String timestamp,
        String nonce, String sign, Map<String, Object> details
    ) {}
}
