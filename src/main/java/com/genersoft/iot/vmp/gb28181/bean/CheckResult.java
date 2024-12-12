package com.genersoft.iot.vmp.gb28181.bean;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CheckResult {

    private Long id;
    private Long checkTaskId;
    private Integer structureResult;
    private Integer colorResult;
    private Integer lightResult;
    private Timestamp recordStartTime;
    private Timestamp recordEndTime;
    private Timestamp startTime;
    private Timestamp endTime;
    private Integer status;
    private Long atonId;
    private String videoUrl;
    private String imgsUrl;
}
