package com.aieducenter.payment.endpoints.api.v1;

import com.aieducenter.payment.application.PaymentAppService;
import com.aieducenter.payment.application.dto.command.CreatePaymentCommand;
import com.aieducenter.payment.application.dto.response.PaymentOrderResponse;
import com.cartisan.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 支付 API v1 控制器
 *
 * <p>提供支付相关的 REST API</p>
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Payment API v1", description = "支付接口 v1")
public class PaymentApiV1Controller {

    private final PaymentAppService paymentAppService;

    @PostMapping
    @Operation(summary = "创建支付订单")
    public ApiResponse<PaymentOrderResponse> createPayment(
            @Valid @RequestBody CreatePaymentCommand command,
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey
    ) {
        // TODO: 从 API Key 中提取 businessSystemName
        // 当前暂时使用默认值
        String businessSystemName = "DemoSystem";

        PaymentOrderResponse response = paymentAppService.createPayment(command, businessSystemName);
        return ApiResponse.ok(response);
    }

    @GetMapping("/{paymentOrderNo}")
    @Operation(summary = "查询支付订单")
    public ApiResponse<PaymentOrderResponse> getPayment(
            @PathVariable String paymentOrderNo
    ) {
        PaymentOrderResponse response = paymentAppService.getPayment(paymentOrderNo);
        return ApiResponse.ok(response);
    }

    @PostMapping("/{paymentOrderNo}/query")
    @Operation(summary = "主动查询支付状态")
    public ApiResponse<PaymentOrderResponse> queryPaymentStatus(
            @PathVariable String paymentOrderNo
    ) {
        PaymentOrderResponse response = paymentAppService.queryPaymentStatus(paymentOrderNo);
        return ApiResponse.ok(response);
    }
}
