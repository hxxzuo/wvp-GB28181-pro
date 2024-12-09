package com.genersoft.iot.vmp.gb28181.bean;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CheckDerivativeTask {

    private Long id;
    private String name;
    private Long checkScheduleId;
    private Integer status;
    private Timestamp startTime;
    private Timestamp endTime;
    private Integer totalNum;
    private Integer completeNum;
}
