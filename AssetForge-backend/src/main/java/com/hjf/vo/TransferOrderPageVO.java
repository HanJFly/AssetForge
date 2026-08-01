package com.hjf.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TransferOrderPageVO {
    private List<TransferOrderPageRecord> records = new ArrayList<>();
    private Long total;
    private Integer page;
    private Integer size;
}
