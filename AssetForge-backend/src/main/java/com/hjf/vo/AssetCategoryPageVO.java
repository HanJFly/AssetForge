package com.hjf.vo;

import com.github.pagehelper.Page;
import lombok.Data;

@Data
public class AssetCategoryPageVO {
    private Page<AssetCategoryVO> records;
    private Long total;
    private Integer page;
    private Integer size;
}
