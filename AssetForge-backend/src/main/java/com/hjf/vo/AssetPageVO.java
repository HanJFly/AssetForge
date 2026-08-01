package com.hjf.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AssetPageVO{
    private List<AssetVO> records = new ArrayList<>();
    private Long total;
    private Integer page;
    private Integer size;
}
