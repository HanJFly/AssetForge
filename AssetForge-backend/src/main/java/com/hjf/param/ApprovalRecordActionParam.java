package com.hjf.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovalRecordActionParam {
    private Long id;
    @NotBlank(message = "是否通过不能为空")
    private String decision;
    @NotBlank(message = "审批意见不能为空")
    private String comment;
}
