package com.hjf.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RequisitionOrderPageVO{

    private List<RequisitionOrderPageRecord> records = new ArrayList<>();
    private Long total;
    private Integer page;
    private Integer size;


}
