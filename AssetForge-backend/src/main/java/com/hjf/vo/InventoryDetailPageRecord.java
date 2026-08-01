package com.hjf.vo;

import lombok.Data;

@Data
public class InventoryDetailPageRecord {
    private Long id;
    private Long taskId;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long systemUserId;
    private String systemUserName;
    private String result;
    private Long actualUserId;
    private String actualUserName;
}
