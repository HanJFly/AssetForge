package com.hjf.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class InventoryDetailSubmitParam {
    @NotNull(message = "任务ID不能为空")
    private Long taskId;
    @NotNull(message = "资产明细不能为空")
    private List<InventoryDetailSubmitList> detailList;
}
