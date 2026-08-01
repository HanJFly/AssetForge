package com.hjf.vo;

import lombok.Data;

@Data
public class SelectRoleVO {
    private Long userId; // 用户ID
    private Long roleId; // 角色ID
    private String roleCode; // 角色代码
    private String roleName; // 角色名称
}
