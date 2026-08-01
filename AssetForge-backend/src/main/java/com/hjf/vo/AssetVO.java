package com.hjf.vo;

import com.hjf.entity.Asset;
import com.hjf.entity.FileAttachment;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AssetVO extends Asset {
    private String categoryName;
    private String currentUserName;
    private List<FileAttachment> attachmentList = new ArrayList<>();

}
