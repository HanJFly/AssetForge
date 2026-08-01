package com.hjf.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InventoryTaskConclusionParam {
    private Long id;
    @NotBlank(message = "结论不能为空")
    private String conclusion;
    private String status;
}
