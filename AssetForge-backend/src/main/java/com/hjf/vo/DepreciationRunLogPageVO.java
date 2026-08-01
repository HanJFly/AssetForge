package com.hjf.vo;

import lombok.Data;

import java.util.List;

@Data
public class DepreciationRunLogPageVO {
    private List<DepreciationRunLogPageRecord> records;
    private Long total;
    private Integer page;
    private Integer Size;
}
