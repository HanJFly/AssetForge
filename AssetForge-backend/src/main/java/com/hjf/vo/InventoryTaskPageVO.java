package com.hjf.vo;

import lombok.Data;

import java.util.List;

@Data
public class InventoryTaskPageVO {
    private List<InventoryTaskPageRecord> records;
    private Long total;
    private Integer page;
    private Integer pageSize;
}
