package com.hjf.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InventoryTaskReportParam {
    @JsonProperty("id")
    private Long taskId;
}
