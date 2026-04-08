# 支付服务 - AI 协作开发配置

> **项目定位**：为各业务系统提供统一的支付服务，支持多银行接入，当前以工行为主。

## 项目概述

这是一个基于 **cartisan-boot** 框架的支付服务项目，采用 **DDD（领域驱动设计）** 架构，为业务系统提供支付、退款、查询等功能。

### 核心目标

1. **详细的支付记录**：记录支付、退款、银行接口调用的完整明细
2. **可扩展的银行接入**：当前对接工行，预留其他银行扩展能力
3. **统一的对外接口**：为业务系统提供标准的支付 API
4. **可复用的认证鉴权**：API Key + 签名验证机制，可复用到其他服务

### 技术栈

- **框架**：Spring Boot 3.x + cartisan-boot
- **数据库**：PostgreSQL + Redis
- **ORM**：Spring Data JPA
- **银行 SDK**：工行 icbc-api-sdk-cop
- **架构模式**：DDD 六边形架构（端口适配器）

## 限界上下文

项目采用 **双上下文架构**：

### 1. Security Context（认证鉴权上下文）

**职责**：
- API Key 管理
- RSA2 签名生成与验证
- 权限控制
- **可复用到其他对外服务**

**包结构**：
```
com.aieducenter.security/
├── domain/
│   ├── aggregate/ApiKey.java
│   ├── entity/ApiKeyPermission.java
│   ├── service/SignatureService.java
│   ├── port/SignatureGenerator.java
│   ├── port/SignatureValidator.java
│   ├── repository/ApiKeyRepository.java
│   └── enums/ApiKeyStatus.java
├── application/
│   ├── dto/
│   ├── mapper/
│   └── ApiKeyManagementAppService.java
├── infrastructure/persistence/
└── endpoints/api/v1/SecurityApiV1Controller.java
```

**对外接口**：
- `POST /api/v1/security/api-keys` - 创建 API Key
- `GET /api/v1/security/api-keys/{id}` - 查询 API Key
- `PUT /api/v1/security/api-keys/{id}` - 更新 API Key

### 2. Payment Context（支付上下文）

**职责**：
- 支付订单管理（创建、查询、状态更新）
- 退款订单管理（创建、审核、查询）
- 支付流水记录（银行接口调用明细）
- 业务编排（调用银行接口）
- 银行适配器（南向接口）

**包结构**：
```
com.aieducenter.payment/
├── domain/
│   ├── aggregate/
│   │   ├── PaymentOrder.java
│   │   ├── RefundOrder.java
│   │   └── PaymentLog.java
│   ├── entity/
│   ├── service/PaymentService.java
│   ├── port/PaymentGatewayPort.java
│   ├── repository/
│   │   ├── PaymentOrderRepository.java
│   │   ├── RefundOrderRepository.java
│   │   └── PaymentLogRepository.java
│   └── enums/
│       ├── PaymentStatus.java
│       ├── RefundStatus.java
│       ├── PaymentMethod.java
│       └── BusinessType.java
├── application/
│   ├── dto/
│   │   ├── command/
│   │   ├── query/
│   │   └── response/
│   ├── mapper/
│   ├── PaymentAppService.java
│   └── RefundAppService.java
├── infrastructure/
│   ├── persistence/
│   └── icbc/
│       ├── IcbcPaymentGatewayAdapter.java
│       ├── IcbdRefundGatewayAdapter.java
│       ├── IcbcConfig.java
│       ├── IcbcSignatureService.java
│       └── IcbcClientFactory.java
└── endpoints/api/v1/
    ├── PaymentApiV1Controller.java
    └── RefundApiV1Controller.java
```

## 核心领域模型

### PaymentOrder（支付订单）

```java
@Aggregate
public class PaymentOrder extends AuditableSoftDeletable {
    private Long id;

    // 基本信息
    private String businessOrderNo;      // 业务订单号（业务系统提供）
    private String paymentOrderNo;       // 支付订单号（支付服务生成）
    private String businessSystemName;   // 业务系统名称（从 API Key 获取）
    private BusinessType businessType;   // 业务类型（业务系统提供）

    // 支付信息
    private PaymentStatus status;        // 支付状态
    private Long amount;                 // 支付金额（分）
    private String currency;             // 币种（CNY）
    private String subject;              // 支付标题
    private String body;                 // 支付描述（可包含 JSON 格式的订单明细）
    private PaymentMethod paymentMethod; // 支付方式

    // 时间信息
    private LocalDateTime createdAt;     // 创建时间
    private LocalDateTime paidAt;        // 支付完成时间
    private LocalDateTime failedAt;      // 支付失败时间
    private LocalDateTime cancelledAt;   // 取消时间
    private LocalDateTime expiredAt;     // 过期时间

    // 银行信息
    private String bankOrderNo;          // 银行订单号
    private String thirdPartyOrderNo;   // 第三方订单号（微信/支付宝等）

    // 客户端信息
    private String clientIp;             // 客户端 IP

    // 回调信息
    private String notifyUrl;            // 异步通知地址
    private String returnUrl;           // 同步返回地址（用户支付完成后跳转）

    // 其他
    private String attach;               // 附加数据
    private Long expiredSeconds;         // 过期时长（秒）
}
```

### RefundOrder（退款订单）

```java
@Aggregate
public class RefundOrder extends AuditableSoftDeletable {
    private Long id;

    // 基本信息
    private String businessOrderNo;      // 业务订单号
    private String refundOrderNo;        // 退款订单号（支付服务生成）
    private String paymentOrderNo;       // 原支付订单号
    private String businessSystemName;   // 业务系统名称
    private BusinessType businessType;   // 业务类型

    // 退款信息
    private RefundStatus status;         // 退款状态
    private RefundType refundType;       // 退款类型（AUTO-自动，AUDIT-审核）
    private Long refundAmount;           // 退款金额（分）
    private Long refundableAmount;       // 可退款金额
    private String reason;               // 退款原因

    // 审核信息（审核退款时使用）
    private Long auditorId;              // 审核人 ID
    private String auditorName;          // 审核人姓名
    private LocalDateTime auditedAt;     // 审核时间
    private String auditRemark;          // 审核备注

    // 时间信息
    private LocalDateTime createdAt;     // 创建时间
    private LocalDateTime refundedAt;    // 退款完成时间
    private LocalDateTime failedAt;      // 退款失败时间

    // 银行信息
    private String bankRefundNo;         // 银行退款流水号

    // 其他
    private String attach;               // 附加数据
}
```

### PaymentLog（支付流水）

```java
@Aggregate
public class PaymentLog extends AuditableSoftDeletable {
    private Long id;

    // 关联信息
    private String paymentOrderNo;       // 关联支付订单号
    private String refundOrderNo;        // 关联退款订单号（可选）

    // 日志信息
    private String logType;              // 日志类型
    private String bankCode;             // 银行编码（ICBC）
    private String bankInterface;        // 银行接口
    private String requestUrl;           // 请求 URL

    // 请求响应
    private String requestParams;        // 请求参数（JSON）
    private String responseParams;       // 响应参数（JSON）
    private Integer httpStatus;          // HTTP 状态码
    private String returnCode;           // 业务返回码
    private String returnMsg;            // 业务返回消息

    // 性能与结果
    private Long executionTime;          // 执行耗时（毫秒）
    private Boolean success;             // 是否成功
    private String errorMessage;         // 错误信息

    // 时间
    private LocalDateTime createdAt;     // 创建时间
}
```

## 对外接口设计

### 支付接口

```
POST /api/v1/payments
Content-Type: application/json
X-API-KEY: your_api_key
X-Signature: generated_signature
X-Timestamp: 2024-01-01T12:00:00Z

{
  "businessOrderNo": "ORDER202401010001",
  "amount": 10000,
  "currency": "CNY",
  "subject": "课程购买",
  "body": "{\"items\":[{\"name\":\"Python课程\",\"price\":10000,\"quantity\":1}]}",
  "businessType": "EDUCATION",
  "clientIp": "123.456.789",
  "notifyUrl": "https://your-site.com/payment/notify",
  "returnUrl": "https://your-site.com/payment/return",
  "expiredSeconds": 3600,
  "attach": "custom data"
}

Response:
{
  "code": 0,
  "message": "success",
  "data": {
    "paymentOrderNo": "PAY20240101123456",
    "businessOrderNo": "ORDER202401010001",
    "status": "PENDING",
    "qrCodeUrl": "weixin://wxpay/bizpayurl?pr=xxxxx",
    "amount": 10000,
    "createdAt": "2024-01-01T12:00:00",
    "expiredAt": "2024-01-01T13:00:00"
  }
}
```

### 查询支付

```
GET /api/v1/payments/{paymentOrderNo}
Response: 类似上面，包含完整订单信息

POST /api/v1/payments/{paymentOrderNo}/query
Response: 主动查询银行状态后的订单信息
```

### 退款接口

```
POST /api/v1/refunds
{
  "businessOrderNo": "ORDER202401010001",
  "paymentOrderNo": "PAY20240101123456",
  "refundAmount": 10000,
  "reason": "课程取消",
  "refundType": "AUTO",  // AUTO-自动退款，AUDIT-审核退款
  "attach": ""
}

Response:
{
  "code": 0,
  "message": "success",
  "data": {
    "refundOrderNo": "REF20240101123456",
    "status": "PENDING",
    "createdAt": "2024-01-01T12:00:00"
  }
}
```

### 审核退款

```
POST /api/v1/refunds/{refundOrderNo}/approve
{
  "auditorId": 123,
  "auditorName": "张三",
  "remark": "同意退款"
}
```

### 查询退款

```
GET /api/v1/refunds/{refundOrderNo}
POST /api/v1/refunds/{refundOrderNo}/query
```

### 回调接口

```
POST /api/v1/payment/callbacks/icbc/payment
POST /api/v1/payment/callbacks/icbc/refund
```

## 工行配置信息

**环境配置**（`application-local.yml`）：

```yaml
gateway:
  icbc:
    # 环境配置
    env: local  # local, test, prod

    # 应用配置
    app-id: 11000000000000079872
    mer-id: 020004161912
    mer-prtcl-no: 0200041619120201  # 协议号 = 商户号 + 0201

    # 密钥配置
    private-key: |
      -----BEGIN PRIVATE KEY-----
      你的商户私钥
      -----END PRIVATE KEY-----
    public-key: |
      -----BEGIN PUBLIC KEY-----
      工行网关公钥
      -----END PUBLIC KEY-----

    # 接口地址
    payment-url: https://gw.open.icbc.com.cn/api/cardbusiness/qrcode/consumption/V1
    payment-query-url: https://gw.open.icbc.com.cn/api/cardbusiness/aggregatepay/b2c/online/orderqry/V1
    refund-url: https://gw.open.icbc.com.cn/api/cardbusiness/aggregatepay/b2c/online/merrefund/V1
    refund-query-url: https://gw.open.icbc.com.cn/api/cardbusiness/aggregatepay/b2c/online/refundqry/V1
```

## 开发规范

1. **遵循 cartisan-boot 规范**：
   - 严格遵循《限界上下文代码编写规范.md》
   - 使用 DDD 六边形架构
   - 领域层零外部依赖

2. **代码风格**：
   - 使用 Java Record 作为 DTO
   - 使用 MapStruct 进行对象转换
   - 使用 hutool 工具类（`CollUtil.newArrayList()` 等）

3. **测试要求**：
   - 单元测试覆盖率：领域层 95%+
   - 集成测试：应用层 90%+
   - 使用 given-when-then 命名规范

4. **文档要求**：
   - 使用 JavaDoc 注释
   - 复杂逻辑添加注释说明
   - 接口变更更新文档

## AI 协作注意事项

1. **先理解再编码**：
   - 遇到不确定的地方，先问清楚再写代码
   - 参考工行 SDK 的 example 代码
   - 对接工行参数时，仔细对照文档

2. **遵循现有架构**：
   - 不破坏现有的 DDD 分层
   - 使用端口接口解耦银行实现
   - 保持代码风格一致

3. **渐进式开发**：
   - 先实现核心功能（支付、支付查询）
   - 再实现扩展功能（退款、退款查询、回调）
   - 最后优化和测试

4. **数据安全**：
   - 敏感信息（密钥）放在配置文件，不提交到 Git
   - 日志中不记录完整的敏感信息
   - 支付流水详细记录，便于问题排查

## 当前任务

当前处于**设计阶段**，等待设计批准后开始实现。

### 待实现功能

1. **Security Context**：
   - [ ] API Key 管理
   - [ ] RSA2 签名服务
   - [ ] 签名验证拦截器

2. **Payment Context**：
   - [ ] 支付订单管理（创建、查询、状态更新）
   - [ ] 退款订单管理（创建、审核、查询）
   - [ ] 支付流水记录
   - [ ] 工行适配器实现

3. **接口开发**：
   - [ ] 支付接口（创建支付、查询支付）
   - [ ] 退款接口（创建退款、审核退款、查询退款）
   - [ ] 回调接口（工行支付回调、工行退款回调）

4. **测试**：
   - [ ] 单元测试
   - [ ] 集成测试
   - [ ] 接口测试

## 联系方式

- **项目负责人**：[待填写]
- **技术栈咨询**：cartisan-boot 文档
- **工行技术支持**：工行开放平台

---

**最后更新**：2026-04-08
**版本**：v1.0
