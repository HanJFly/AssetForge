package com.hjf.param;

import com.hjf.entity.Asset;
import lombok.Data;

import java.util.List;

@Data
public class AssetParam extends Asset {



    private String assetStatus;
    private Long categoryId;
    private Long departmentId;
    private String departmentName;
    private Long userId;
    private String sourceType;
    private String purchaseDateStart;
    private String purchaseDateEnd;
    private List<Long> attachmentIds;
}
