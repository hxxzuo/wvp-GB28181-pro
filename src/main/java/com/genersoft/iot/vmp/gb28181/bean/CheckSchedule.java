package com.genersoft.iot.vmp.gb28181.bean;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CheckSchedule {

    private Long id;
    private String name;
    private String administrators;
    private Long atonListId;
    private String period;
    private Integer status;
    private Integer priority;
    private Integer checkLight;
    private Integer checkColor;
    private Integer checkStructure;
    private Timestamp createTime;
    private Timestamp updateTime;


}
