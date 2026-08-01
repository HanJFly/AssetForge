package com.hjf.vo;

import lombok.Data;

import java.util.List;

@Data
public class InventoryDetailPageVO {
    private List<InventoryDetailPageRecord> records;
    private Long total;
    private Integer page;
    private Integer pageSize;}
