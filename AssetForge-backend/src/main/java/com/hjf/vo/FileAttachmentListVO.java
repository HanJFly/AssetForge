package com.hjf.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FileAttachmentListVO {

    private List<FileAttachmentListVOChildren> children = new ArrayList<>();


}
