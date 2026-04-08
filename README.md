# 海创元智研云平台 - 支付服务

为各业务系统提供统一的支付、退款、查询功能，支持多银行接入（当前对接工行）。

## 技术栈

- Java 21 + Spring Boot 3.x + cartisan-boot
- PostgreSQL + Redis + Spring Data JPA
- 工行 API 开放平台 SDK（二维码聚合支付）
- Flyway 数据库迁移

## 快速开始

### 1. 准备工行 SDK

将工行 JAR 包放到 `lib/icbc/` 目录：

```
lib/icbc/
├── icbc-api-sdk-cop.jar
├── icbc-api-sdk-cop-io.jar
└── icbc-ca.jar
```

### 2. 配置数据库和工行参数

参考 `src/main/resources/application-local.yml.example` 创建 `application-local.yml`。

### 3. 创建 API Key

```bash
curl -X POST http://localhost:8080/api/v1/security/api-keys \
  -H "Content-Type: application/json" \
  -d '{
    "businessSystemName": "MySystem",
    "description": "测试系统",
    "permissions": ["payment:write", "payment:read"]
  }'
```

### 4. 创建支付订单

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: your_api_key" \
  -d '{
    "businessOrderNo": "ORDER001",
    "amount": 10000,
    "subject": "课程购买",
    "body": "Python编程课程",
    "businessType": "EDUCATION",
    "notifyUrl": "https://example.com/notify",
    "returnUrl": "https://example.com/return"
  }'
```

## API 接口

### Security API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/security/api-keys` | 创建 API Key |
| GET | `/api/v1/security/api-keys/{id}` | 查询 API Key |
| PUT | `/api/v1/security/api-keys/{id}/disable` | 禁用 |
| PUT | `/api/v1/security/api-keys/{id}/enable` | 启用 |

### Payment API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/payments` | 创建支付订单 |
| GET | `/api/v1/payments/{paymentOrderNo}` | 查询支付订单 |
| POST | `/api/v1/payments/{paymentOrderNo}/query` | 主动查询银行状态 |

### Refund API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/refunds` | 创建退款订单 |
| POST | `/api/v1/refunds/{refundOrderNo}/audit` | 审核退款 |
| GET | `/api/v1/refunds/{refundOrderNo}` | 查询退款订单 |

## 项目结构

```
com.aieducenter/
├── security/                          # Security Context
│   ├── domain/                        # 领域层
│   │   ├── aggregate/ApiKey.java
│   │   ├── entity/ApiKeyPermission.java
│   │   ├── repository/ApiKeyRepository.java
│   │   ├── enums/ApiKeyStatus.java
│   │   └── error/SecurityMessage.java
│   ├── application/                   # 应用层
│   │   ├── ApiKeyManagementAppService.java
│   │   └── dto/
│   └── endpoints/api/v1/             # 接口层
│       └── SecurityApiV1Controller.java
│
└── payment/                           # Payment Context
    ├── domain/                        # 领域层
    │   ├── aggregate/
    │   │   ├── PaymentOrder.java      # 支付订单
    │   │   ├── RefundOrder.java       # 退款订单（含审核工作流）
    │   │   └── PaymentLog.java        # 支付日志
    │   ├── port/
    │   │   ├── PaymentGatewayPort.java        # 支付网关端口
    │   │   └── response/                      # 响应 DTO
    │   ├── repository/
    │   ├── enums/
    │   └── error/PaymentMessage.java
    ├── application/                   # 应用层
    │   ├── PaymentAppService.java
    │   ├── RefundAppService.java
    │   └── dto/
    ├── infrastructure/                # 基础设施层
    │   └── icbc/
    │       ├── IcbcConfig.java        # 工行配置
    │       ├── IcbcClientFactory.java # 客户端工厂
    │       └── IcbcPaymentGatewayAdapter.java  # 工行适配器
    └── endpoints/api/v1/             # 接口层
        ├── PaymentApiV1Controller.java
        └── RefundApiV1Controller.java
```

## 工行接口

| 接口 | 路径 | 说明 |
|------|------|------|
| 二维码支付 | `/api/cardbusiness/qrcode/consumption/V1` | 生成支付二维码 |
| 支付查询 | `/api/cardbusiness/aggregatepay/b2c/online/orderqry/V1` | 查询支付状态 |
| 线上退货 | `/api/cardbusiness/aggregatepay/b2c/online/merrefund/V1` | 发起退款 |

## 退款审核流程

```
PENDING → APPROVED → REFUNDING → SUCCESS
   │                              │
   └──→ REJECTED                  └──→ FAILED
```

所有退款必须经过审核后才会调用银行退款接口。
