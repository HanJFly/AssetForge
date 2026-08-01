package com.hjf.vo;

import com.hjf.entity.Asset;
import lombok.Data;

@Data
public class AssetBarcodeDetailVO extends Asset {
    private String categoryName;
    private String currentUserName;
    private String barcodeValue;
}
