package com.genersoft.iot.vmp.gb28181.controller.bean;

import lombok.Data;

@Data
public class AtonCameraListParam {

    private String deviceId;
    private String channelId;
    private Long atonId;
    private String name;
    private Integer radius;
}
