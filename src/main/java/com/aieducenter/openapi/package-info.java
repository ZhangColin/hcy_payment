/**
 * OpenAPI Context.
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>API Key 管理</li>
 *   <li>RSA2 签名生成与验证</li>
 *   <li>权限控制</li>
 * </ul>
 *
 * <h3>限界上下文</h3>
 * <p>可复用的开放平台上下文，为对外服务提供统一的 API Key + 签名验证机制</p>
 *
 * <h3>包结构</h3>
 * <ul>
 *   <li>domain - 领域层：API Key 聚合根、签名服务、端口接口</li>
 *   <li>application - 应用层：API Key 管理服务、DTO</li>
 *   <li>infrastructure - 基础设施层：持久化</li>
 *   <li>endpoints - 北向接口：OpenAPI</li>
 * </ul>
 *
 * @since 1.0.0
 */
@BoundedContext(name = "OpenAPI", subDomain = SubDomain.SUPPORTING)
package com.aieducenter.openapi;

import com.cartisan.core.stereotype.BoundedContext;
import com.cartisan.core.stereotype.SubDomain;
