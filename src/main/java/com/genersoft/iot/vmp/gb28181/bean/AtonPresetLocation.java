package com.genersoft.iot.vmp.gb28181.bean;

import lombok.Data;

@Data
public class AtonPresetLocation {

    private Long id;
    private Long atonId;
    private String deviceId;
    private String channelId;
    private Integer presetLocation;
}
