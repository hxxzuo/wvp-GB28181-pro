package com.genersoft.iot.vmp.gb28181.service.impl;

import com.genersoft.iot.vmp.gb28181.bean.Aton;
import com.genersoft.iot.vmp.gb28181.bean.DeviceChannel;
import com.genersoft.iot.vmp.gb28181.dao.AtonMapper;
import com.genersoft.iot.vmp.gb28181.dao.DeviceChannelMapper;
import com.genersoft.iot.vmp.gb28181.service.IAtonService;
import com.genersoft.iot.vmp.utils.GpsUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AtonServiceImpl implements IAtonService {

    @Autowired
    private AtonMapper atonMapper;
    @Autowired
    private DeviceChannelMapper deviceChannelMapper;

    @Override
    public PageInfo<Aton> queryAton(int page, int count, String name, String type) {
        PageHelper.startPage(page, count);
        if (StringUtils.isBlank(name)) {
            name = null;
        }
        if (StringUtils.isBlank(type)) {
            type = null;
        }
        List<Aton> all = atonMapper.queryAtonList(name, type);
        return new PageInfo<>(all);
    }

    @Override
    public PageInfo<DeviceChannel> checkAtonCameraList(int page, int count, String name, Integer radius) {
        List<Aton> all = atonMapper.queryAtonList(name, null);
        Aton aton = all.get(0);

        PageHelper.startPage(page, count);
        List<DeviceChannel> deviceChannels = deviceChannelMapper.getAllDeviceLocation();
        if (CollectionUtils.isEmpty(deviceChannels)) {
            return new PageInfo<>(deviceChannels);
        }
        deviceChannels = deviceChannels.stream().filter(deviceChannel -> {
            if (radius != null) {
                return GpsUtil.calculateDistance(aton.getLongitude(), aton.getLatitude(), deviceChannel.getLongitude(), deviceChannel.getLatitude()) <= radius;
            }
            return true;
        }).collect(Collectors.toList());
        return new PageInfo<>(deviceChannels);
    }
}
