package com.genersoft.iot.vmp.gb28181.service;

import com.genersoft.iot.vmp.gb28181.bean.Aton;
import com.genersoft.iot.vmp.gb28181.bean.DeviceChannel;
import com.genersoft.iot.vmp.gb28181.controller.bean.AtonPresetCamera;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface IAtonService {

    PageInfo<Aton>  queryAton(int page, int count, String name,String type,String administer);

    List<AtonPresetCamera> queryAtonPresetCamera(String deviceId, String channelId);
    PageInfo<DeviceChannel>  checkAtonCameraList(int page, int count, String name, Integer radius);

    public PageInfo<DeviceChannel> getAllDeviceChannel();


}
