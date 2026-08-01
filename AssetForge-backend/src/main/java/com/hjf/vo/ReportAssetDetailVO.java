package com.hjf.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReportAssetDetailVO {
    private List<ReportAssetDetailPageRecord> records;
    private Long total;
    private Integer page;
    private Integer pageSize;
}
