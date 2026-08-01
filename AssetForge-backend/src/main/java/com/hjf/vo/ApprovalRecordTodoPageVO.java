package com.hjf.vo;

import com.github.pagehelper.Page;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ApprovalRecordTodoPageVO extends Page {


    private List<TodoPageVo> records = new ArrayList<>();


}
