package com.hjf.param;

import com.hjf.vo.Page;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InventoryTaskPageParam extends Page {
    private String status;
    private String scopeType;
    private String taskName;
    private LocalDate startDate;
    private LocalDate endDate;
}
