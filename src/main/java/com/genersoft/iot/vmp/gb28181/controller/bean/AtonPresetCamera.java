package com.genersoft.iot.vmp.gb28181.controller.bean;

import lombok.Data;

@Data
public class AtonPresetCamera {

    private Integer presetLocationId;
    private String name;

    public AtonPresetCamera(Integer presetLocationId, String name) {
        this.presetLocationId = presetLocationId;
        this.name = name;
    }
}
