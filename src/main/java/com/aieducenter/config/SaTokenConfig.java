package com.aieducenter.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.dev33.satoken.stp.StpInterface;

import com.aieducenter.admin.application.AdminUserPermissionAppService;

/**
 * Sa-Token 配置。
 *
 * <p>为管理员模块配置独立的 StpInterface 实现，支持管理员权限查询。</p>
 *
 * @since 0.1.0
 */
@Configuration
public class SaTokenConfig {

    private static final String ADMIN_LOGIN_TYPE = "admin";

    private final AdminUserPermissionAppService adminPermissionAppService;

    public SaTokenConfig(AdminUserPermissionAppService adminPermissionAppService) {
        this.adminPermissionAppService = adminPermissionAppService;
    }

    /**
     * Sa-Token 权限接口实现。
     * <p>通过 loginType 区分管理员和普通用户</p>
     */
    @Bean
    public StpInterface cartisanStpInterface() {
        return new StpInterface() {
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                if (ADMIN_LOGIN_TYPE.equals(loginType)) {
                    return adminPermissionAppService.getPermissions((Long) loginId);
                }
                // TODO: 后续实现租户权限
                return List.of();
            }

            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                if (ADMIN_LOGIN_TYPE.equals(loginType)) {
                    return adminPermissionAppService.getRoleCodes((Long) loginId);
                }
                // TODO: 后续实现租户角色
                return List.of();
            }
        };
    }
}
