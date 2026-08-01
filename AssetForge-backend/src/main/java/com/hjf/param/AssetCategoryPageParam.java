package com.hjf.param;

import com.hjf.vo.Page;

public class AssetCategoryPageParam extends Page {
    //分类名称（模糊查询）
    private String name;
    //父分类ID
    private Long parentId;
}
