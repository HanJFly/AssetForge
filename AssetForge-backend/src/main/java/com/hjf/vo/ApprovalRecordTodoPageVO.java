package com.hjf.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApprovalRecordTodoPageVO extends Page {

    private List<TodoPageVo> records = new ArrayList<>();

}
