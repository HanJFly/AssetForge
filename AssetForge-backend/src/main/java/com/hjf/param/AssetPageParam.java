package com.hjf.param;

import com.hjf.vo.Page;
import lombok.Data;

@Data
public class AssetPageParam extends Page {

    private Long currentUserId;
    private String name;
    private String assetCode;
    private String brandModel;
    private String realName;


    private String assetStatus;
    private String categoryName;
    private String departmentName;
    private String sourceType;
    private String purchaseDateStart;
    private String purchaseDateEnd;
}
