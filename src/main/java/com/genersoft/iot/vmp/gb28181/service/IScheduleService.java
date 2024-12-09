package com.genersoft.iot.vmp.gb28181.service;

import com.genersoft.iot.vmp.gb28181.bean.CheckSchedule;
import com.github.pagehelper.PageInfo;

public interface IScheduleService {

    void delete(Long id);
    void update(CheckSchedule checkSchedule);
    void add(CheckSchedule checkSchedule);
    PageInfo<CheckSchedule> query(int page, int count, String name);
}
