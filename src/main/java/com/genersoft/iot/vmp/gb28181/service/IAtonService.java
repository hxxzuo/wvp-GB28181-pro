package com.genersoft.iot.vmp.gb28181.service;

import com.genersoft.iot.vmp.gb28181.bean.Aton;
import com.genersoft.iot.vmp.web.gb28181.dto.DeviceChannelExtend;
import com.github.pagehelper.PageInfo;

public interface IAtonService {

    PageInfo<Aton>  queryAton(int page, int count, String name,String type);

    PageInfo<DeviceChannelExtend>  checkAtonCameraList(int page, int count, String name, Integer radius);
}
