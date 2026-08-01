package com.hjf.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepartmentDeleteParam {
    @NotNull(message = "部门id不能为空")
    private Long id;
}
