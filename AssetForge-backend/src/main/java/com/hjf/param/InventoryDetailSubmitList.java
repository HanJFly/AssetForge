package com.hjf.param;

import lombok.Data;

@Data
public class InventoryDetailSubmitList {
    private Long detailId;
    private String result;
    private Long actualUserId;
    private String actualLocation;
    private String remark;
    private String foundAssetName;
    private String foundAssetCategory;
    private String foundAssetLocation;
    private String foundAssetCode;

}
