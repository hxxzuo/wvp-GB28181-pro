package com.genersoft.iot.vmp.gb28181.service.impl;

import com.genersoft.iot.vmp.gb28181.bean.Aton;
import com.genersoft.iot.vmp.gb28181.bean.CheckDerivativeTask;
import com.genersoft.iot.vmp.gb28181.bean.CheckResult;
import com.genersoft.iot.vmp.gb28181.bean.CheckSchedule;
import com.genersoft.iot.vmp.gb28181.dao.AtonMapper;
import com.genersoft.iot.vmp.gb28181.dao.CheckDerivativeTaskMapper;
import com.genersoft.iot.vmp.gb28181.dao.CheckResultMapper;
import com.genersoft.iot.vmp.gb28181.dao.CheckScheduleMapper;
import com.genersoft.iot.vmp.gb28181.service.IScheduleService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class ScheduleService implements IScheduleService {

    @Autowired
    private CheckScheduleMapper checkScheduleMapper;
    @Autowired
    private AtonMapper atonMapper;
    @Autowired
    private CheckDerivativeTaskMapper checkDerivativeTaskMapper;
    @Autowired
    private CheckResultMapper checkResultMapper;

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    @Override
    public void delete(Long id) {
        checkScheduleMapper.delete(id);
    }

    @Override
    public void update(CheckSchedule checkSchedule) {
        checkScheduleMapper.update(checkSchedule);
    }

    @Override
    public void add(CheckSchedule checkSchedule) {
        checkScheduleMapper.add(checkSchedule);
    }

    @Override
    public PageInfo<CheckSchedule> query(int page, int count, String name) {
        PageHelper.startPage(page, count);
        if (StringUtils.isBlank(name)) {
            name = null;
        }
        return new PageInfo<>(checkScheduleMapper.query(name));
    }


    private void execCheckDerivativeTask(CheckSchedule checkSchedule) {
        CheckDerivativeTask checkDerivativeTask = new CheckDerivativeTask();
        checkDerivativeTask.setCheckScheduleId(checkSchedule.getId());
        checkDerivativeTask.setName(checkSchedule.getName() + System.currentTimeMillis());
        checkDerivativeTask.setStartTime(new Timestamp(System.currentTimeMillis()));
        checkDerivativeTask.setStatus(0);
        checkDerivativeTask.setCompleteNum(0);
        checkDerivativeTask.setTotalNum(0);
        Long id = checkDerivativeTaskMapper.add(checkDerivativeTask);

        AtomicInteger total = new AtomicInteger();
        String administrators = checkSchedule.getAdministrators();
        if (StringUtils.isBlank(administrators)) {
            return;
        }
        String[] c = administrators.split(",");

        Arrays.stream(c).forEach(admin -> {
            List<Aton> atons = atonMapper.queryAtonList(null, null, admin);
            if (CollectionUtils.isEmpty(atons)) {
                return;
            }
            total.addAndGet(atons.size());
            atons.stream().forEach(at -> {
                if (checkSchedule.getCheckColor() == 1) {
                    executor.submit(() -> {
                        try {
                            execCheckColorTask(at);
                        } catch (Exception e) {
                            log.error("execCheckColorTask error", e);
                        }
                    });
                }
                if (checkSchedule.getCheckStructure() == 1) {
                    executor.submit(() -> {
                        try {
                            execCheckStructureTask(at);
                        } catch (Exception e) {
                            log.error("execCheckStructureTask error", e);
                        }
                    });

                }
                if (checkSchedule.getCheckLight() == 1) {
                    executor.submit(() -> {
                        try {
                            execCheckLightTask(at);
                        } catch (Exception e) {
                            log.error("execCheckLightTask error", e);
                        }
                    });

                }
                CheckResult checkResult = new CheckResult();
                checkResult.setAtonId(at.getId());
                checkResult.setCheckTaskId(id);
                checkResult.setStartTime(new Timestamp(System.currentTimeMillis()));
                checkResult.setAtonId(at.getId());
                checkResult.setStatus(0);
                checkResultMapper.add(checkResult);
            });
        });

    }

    private void execCheckColorTask(Aton aton) {
        log.info("执行定时任务: 检查颜色");

    }

    private void execCheckStructureTask(Aton aton) {
        log.info("执行定时任务: 检查结构");
    }

    private void execCheckLightTask(Aton aton) {
        log.info("执行定时任务: 检查灯光");
    }

    private void checkCronTask() {

        List<CheckSchedule> checkSchedules = checkScheduleMapper.query(null);
        if (CollectionUtils.isEmpty(checkSchedules)) {
            return;
        }
        checkSchedules.stream().filter(schedule -> {
            if (schedule.getStatus() == 0) {
                return false;
            }
            CronExpression cron = null;
            try {
                cron = new CronExpression(schedule.getPeriod());
                return cron.isSatisfiedBy(new Date());
            } catch (ParseException e) {
                log.error("定时任务解析失败: {}", schedule.getName(), e);
                return false;
            }

        }).forEach(schedule -> {
            execCheckDerivativeTask(schedule);
        });
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ScheduleService() {
        super();
        scheduler.scheduleAtFixedRate(this::checkCronTask, 0, 1, TimeUnit.MINUTES);
    }

}
