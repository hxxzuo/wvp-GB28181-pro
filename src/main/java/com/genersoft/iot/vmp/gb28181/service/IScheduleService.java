package com.genersoft.iot.vmp.gb28181.service;

import com.genersoft.iot.vmp.gb28181.bean.AtonPresetLocation;
import com.genersoft.iot.vmp.gb28181.bean.CheckDerivativeTask;
import com.genersoft.iot.vmp.gb28181.bean.CheckResult;
import com.genersoft.iot.vmp.gb28181.bean.CheckSchedule;
import com.github.pagehelper.PageInfo;

public interface IScheduleService {

    void delete(Long id);

    void update(CheckSchedule checkSchedule);

    void add(CheckSchedule checkSchedule);

    PageInfo<CheckSchedule> queryCheckSchedule(int page, int count, String name);
    PageInfo<CheckDerivativeTask> queryCheckDerivativeTask(int page, int count, String name);
    PageInfo<CheckResult> queryCheckResult(int page, int count, String name);
    void addAtonPresetLocation(AtonPresetLocation atonPresetLocation);

    void updateAtonPresetLocation(AtonPresetLocation atonPresetLocation);

    AtonPresetLocation getAtonPresetLocation(Long atonId, String deviceId, String channelId);

    void startCheckCronTask();

}
