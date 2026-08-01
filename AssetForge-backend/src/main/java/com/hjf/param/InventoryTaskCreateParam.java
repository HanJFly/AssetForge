package com.hjf.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class InventoryTaskCreateParam {
    @NotBlank(message = "任务名不能为空")
    private String taskName;
    @NotBlank(message = "范围类型不能为空")
    private String scopeType;
    private List<Long> scopeValue = new ArrayList<>();
    private List<String> assetStatusFilter = new ArrayList<>();
    @NotNull(message = "截止日期不能为空")
    private LocalDate deadLine;
    @NotNull(message = "责任人不能为空")
    private Long responsibleUserId;

}
