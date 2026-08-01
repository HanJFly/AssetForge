package com.hjf.vo;

import com.hjf.entity.AssetCategory;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AssetCategoryVO extends AssetCategory {
    /*
    * 子类
    * */
    private List<AssetCategoryVO> children = new ArrayList<>();

    // 父类名称
    private String parentName;
}
