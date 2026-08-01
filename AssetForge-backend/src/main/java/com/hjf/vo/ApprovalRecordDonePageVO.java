package com.hjf.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApprovalRecordDonePageVO extends Page{
    private List<DonePageRecordVO> records = new ArrayList<>();

}
