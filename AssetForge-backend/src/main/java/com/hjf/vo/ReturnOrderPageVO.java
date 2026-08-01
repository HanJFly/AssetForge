package com.hjf.vo;

import lombok.Data;
import net.sf.jsqlparser.statement.select.PivotVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReturnOrderPageVO {
    private List<ReturnOrderPageRecord> records = new ArrayList<>();
    private Long total;
    private Integer page;
    private Integer size;
}
