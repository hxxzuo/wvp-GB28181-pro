package com.genersoft.iot.vmp.gb28181.service;

import com.genersoft.iot.vmp.gb28181.bean.Aton;
import com.genersoft.iot.vmp.gb28181.bean.DeviceChannel;
import com.github.pagehelper.PageInfo;

public interface IAtonService {

    PageInfo<Aton>  queryAton(int page, int count, String name,String type);

    PageInfo<DeviceChannel>  checkAtonCameraList(int page, int count, String name, Integer radius);

    public PageInfo<DeviceChannel> getAllDeviceChannel();
}
