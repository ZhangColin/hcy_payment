# ICBC Payment Callback Design

## Overview

Two-layer callback mechanism:

1. **ICBC -> Payment Service**: ICBC sends payment result to our callback endpoint
2. **Payment Service -> Business System**: We notify the business system of the payment result

## 1. ICBC Callback Endpoint

### Endpoint

`POST /api/v1/payment/callbacks/icbc`

Already excluded from API signature verification in `WebConfig`.

### Notify URL Configuration

Configured in `application.yml` per environment:

```yaml
gateway:
  icbc:
    notify-base-url: https://api.example.com
```

The full notify URL sent to ICBC as `mer_url`: `{notify-base-url}/api/v1/payment/callbacks/icbc`

This value is set in `IcbcPaymentGatewayAdapter` when creating payment orders.

### Callback Handling Flow

1. Receive POST request (`Content-Type: application/x-www-form-urlencoded`)
2. Extract parameters: `api`, `app_id`, `biz_content`, `charset`, `format`, `from`, `sign_type`, `timestamp`, `sign`
3. **Verify signature** using ICBC gateway public key:
   - Sign type is always `RSA` for callbacks
   - Signature plaintext = request path + "?" + parameters sorted by ASCII ascending (excluding `sign`)
4. Parse `biz_content` JSON, extract key fields
5. Find `PaymentOrder` by `out_trade_no`
6. Update order status and details
7. Write `PaymentLog` with full callback content
8. Notify business system
9. Return ICBC-required response format

### ICBC Callback Scenarios

With `result_type=0` (configured in our order creation):
- **Payment success**: `return_code=0`, `returnCode=0` -> update order to PAID
- **Payment failure**: `return_code` non-zero -> update order to FAILED

ICBC retries up to 3 times within 10 minutes if we don't return a valid response.

### Data Recording (PaymentOrder update)

| Field | Source (ICBC callback biz_content) |
|---|---|
| status | PAID if return_code=0, else FAILED |
| bankOrderNo | order_id |
| thirdPartyOrderNo | third_trade_no |
| paidAt / failedAt | Current timestamp |
| actualAmount | payment_amt (actual paid amount in fen) |

Additional fields stored in payment log or extension fields: pay_type, access_type, card_no, card_flag, decr_flag, total_amt, total_disc_amt, attach, pay_time, promotion_detail, etc.

### Response to ICBC

```json
{
  "response_biz_content":{"return_code":0,"return_msg":"success","msg_id":"unique-msg-id"},
  "sign_type":"RSA2",
  "sign":"signature-using-merchant-private-key"
}
```

Key requirements:
- Sign string is the exact content: `"response_biz_content":{"return_code":0,"return_msg":"success","msg_id":"xxx"},"sign_type":"RSA2"` (no spaces or newlines)
- Field order must be: response_biz_content, sign_type, sign
- sign_type matches the type registered with ICBC (RSA2)

## 2. Business System Notification

After processing ICBC callback, we POST JSON to the business system's `notifyUrl` (provided when they created the payment order).

### Request

```
POST {notifyUrl}
Content-Type: application/json
```

### Request Body

```json
{
  "orderId": "payment-order-uuid",
  "outTradeNo": "business-system-order-no",
  "status": "PAID",
  "amount": "100",
  "actualAmount": "100",
  "bankOrderNo": "icbc-order-id",
  "payTime": "2024-01-12 12:12:12",
  "payType": "9",
  "attach": "business-attached-data"
}
```

| Field | Type | Description |
|---|---|---|
| orderId | String | Our payment order ID |
| outTradeNo | String | Business system's order number |
| status | String | PAID or FAILED |
| amount | String | Order total amount (fen) |
| actualAmount | String | Actual paid amount (fen) |
| bankOrderNo | String | ICBC order ID |
| payTime | String | Payment completion time (yyyy-MM-dd HH:mm:ss) |
| payType | String | Payment method (9=WeChat, 10=Alipay, etc.) |
| attach | String | Business system's attached data, passed through |

### Business System Response

- HTTP 200 = success
- No retry on failure. Business systems can query order status via the query API.

## 3. Implementation Notes

### Signature Verification for ICBC Callback

ICBC callback uses `RSA` sign_type (not RSA2). Must use ICBC gateway public key.

Signature plaintext construction:
```
/api/v1/payment/callbacks/icbc?api=/api/cardbusiness/qrcode/consumption/V1&app_id=xxx&biz_content={...}&charset=utf-8&format=json&from=icbc-api&sign_type=RSA&timestamp=2017-01-12 17:33:56
```

Parameters sorted by ASCII, joined with `&`, preceded by request path + `?`.

### Response Signing to ICBC

Sign the response using our merchant private key with RSA2 algorithm:
- Input: the exact JSON string of `"response_biz_content":{"return_code":0,"return_msg":"success","msg_id":"xxx"},"sign_type":"RSA2"` (no spaces/newlines)
- Algorithm: RSA2 (RSAWITHSHA256)
- Output: Base64 encoded signature
