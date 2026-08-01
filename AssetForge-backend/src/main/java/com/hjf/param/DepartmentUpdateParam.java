package com.hjf.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepartmentUpdateParam {
    @NotNull(message = "部门id不能为空")
    private Long id;
    private String name;
    private Long parentId;
    private Long managerUserId;
    private Integer sortOrder;
    private String remark;
}
