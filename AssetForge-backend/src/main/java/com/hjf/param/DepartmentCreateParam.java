package com.hjf.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepartmentCreateParam {
    @NotBlank(message = "部门名称不能为空")
    private String name;
    private Long parentId;
    @NotNull(message = "部门管理员不能为空")
    private Long managerUserId;
    private Integer sortOrder;

}
