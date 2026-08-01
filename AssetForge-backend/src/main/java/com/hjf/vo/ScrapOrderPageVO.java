package com.hjf.vo;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScrapOrderPageVO {
    private List<ScrapOrderPageRecord> records = new ArrayList<>();
    private Long total;
    private Integer page;
    private Integer size;
}
