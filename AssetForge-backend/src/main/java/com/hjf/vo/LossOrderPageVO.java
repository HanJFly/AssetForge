package com.hjf.vo;

import lombok.Data;

import java.util.List;

@Data
public class LossOrderPageVO {
    private List<LossOrderPageRecord> records;
    private Long total;
    private Integer page;
    private Integer pageSize;
}
