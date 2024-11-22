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
        all.forEach(aton -> {
            double[] p = GpsUtil.wgs84ToGcj02(aton.getLongitude(), aton.getLatitude());
            aton.setLongitude(p[0]);
            aton.setLatitude(p[1]);
        });
        return new PageInfo<>(all);
    }

    @Override
    public PageInfo<DeviceChannel> checkAtonCameraList(int page, int count, String name, Integer radius) {
        List<Aton> all = atonMapper.queryAtonList(name, null);
        if (CollectionUtils.isEmpty(all)) {
            return new PageInfo<>();
        }
        Aton aton = all.get(0);
        double[] p = GpsUtil.wgs84ToGcj02(aton.getLongitude(), aton.getLatitude());
        aton.setLongitude(p[0]);
        aton.setLatitude(p[1]);
        PageHelper.startPage(page, count);
        List<DeviceChannel> deviceChannels = deviceChannelMapper.getAllDeviceLocation();
        if (CollectionUtils.isEmpty(deviceChannels)) {
            return new PageInfo<>(deviceChannels);
        }
        deviceChannels = deviceChannels.stream().filter(deviceChannel -> {
            double distance = GpsUtil.calculateDistance(aton.getLongitude(), aton.getLatitude(), deviceChannel.getLongitude(), deviceChannel.getLatitude());
            if (distance <= radius) {
                deviceChannel.setToAtonDistance((int) distance);
                deviceChannel.setRelativeAtonDirection(GpsUtil.calculateRelativeDirection(deviceChannel.getLongitude(), deviceChannel.getLatitude(), aton.getLongitude(), aton.getLatitude()));
                return true;
            } else return false;
        }).collect(Collectors.toList());
        return new PageInfo<>(deviceChannels);
    }

    public PageInfo<DeviceChannel> getAllDeviceChannel() {
        PageHelper.startPage(1, 1000);
        List<DeviceChannel> deviceChannels = deviceChannelMapper.getAllDeviceLocation();
        if (CollectionUtils.isEmpty(deviceChannels)) {
            return new PageInfo<>();
        }
        return new PageInfo<>(deviceChannels);
    }

}
