package com.genersoft.iot.vmp.gb28181.service.impl;

import com.genersoft.iot.vmp.conf.exception.ControllerException;
import com.genersoft.iot.vmp.gb28181.bean.*;
import com.genersoft.iot.vmp.gb28181.controller.DeviceControl;
import com.genersoft.iot.vmp.gb28181.controller.PtzController;
import com.genersoft.iot.vmp.gb28181.dao.*;
import com.genersoft.iot.vmp.gb28181.service.IDeviceChannelService;
import com.genersoft.iot.vmp.gb28181.service.IDeviceService;
import com.genersoft.iot.vmp.gb28181.service.IPlayService;
import com.genersoft.iot.vmp.gb28181.service.IScheduleService;
import com.genersoft.iot.vmp.media.event.media.MediaRecordMp4Event;
import com.genersoft.iot.vmp.storager.dao.CloudRecordServiceMapper;
import com.genersoft.iot.vmp.vmanager.bean.ErrorCode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@Configuration
public class ScheduleService implements IScheduleService {

    @Autowired
    private CheckScheduleMapper checkScheduleMapper;
    @Autowired
    private AtonMapper atonMapper;
    @Autowired
    private AtonPresetLocationMapper atonPresetLocationMapper;
    @Autowired
    private CheckDerivativeTaskMapper checkDerivativeTaskMapper;
    @Autowired
    private CheckResultMapper checkResultMapper;
    @Autowired
    private PtzController ptzController;
    @Autowired
    private DeviceControl deviceControl;
    @Autowired
    private CloudRecordServiceMapper cloudRecordServiceMapper;
    @Autowired
    private IDeviceService deviceService;
    @Autowired
    private IDeviceChannelService channelService;
    @Autowired
    private IPlayService playService;

    private ExecutorService cronExecutor = Executors.newFixedThreadPool(5);
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Bean(name = "scheduleExecutor")
    public Executor scheduleExecutor() {
        return Executors.newFixedThreadPool(5);
    }

    public AtonPresetLocation getAtonPresetLocation(Long atonId, String deviceId, String channelId) {
        List<AtonPresetLocation> atonPresetLocationList = atonPresetLocationMapper.query(atonId, deviceId, channelId);
        if (CollectionUtils.isEmpty(atonPresetLocationList)) {
            return null;
        }
        return atonPresetLocationList.get(0);
    }

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
    public PageInfo<CheckSchedule> queryCheckSchedule(int page, int count, String name) {
        PageHelper.startPage(page, count);
        if (StringUtils.isBlank(name)) {
            name = null;
        }
        return new PageInfo<>(checkScheduleMapper.query(name));
    }

    public PageInfo<CheckDerivativeTask> queryCheckDerivativeTask(int page, int count, String name) {
        PageHelper.startPage(page, count);
        if (StringUtils.isBlank(name)) {
            name = null;
        }
        return new PageInfo<>(checkDerivativeTaskMapper.query(name));
    }

    public PageInfo<CheckResult> queryCheckResult(int page, int count, String name) {
        PageHelper.startPage(page, count);
        if (StringUtils.isBlank(name)) {
            name = null;
        }
        return new PageInfo<>(checkResultMapper.query(name));
    }

    @Override
    public void addAtonPresetLocation(AtonPresetLocation atonPresetLocation) {
        atonPresetLocationMapper.add(atonPresetLocation);
    }

    @Override
    public void updateAtonPresetLocation(AtonPresetLocation atonPresetLocation) {
        atonPresetLocationMapper.add(atonPresetLocation);
    }

    private void controlAndSaveVideoRecord(String deviceId, String channelId, Long atonId, Long taskId) {
        log.info("controlAndSaveVideoRecord start deviceId={} channelId={} atonId={}", deviceId, channelId, atonId);
        List<AtonPresetLocation> atonPresetLocationList = atonPresetLocationMapper.query(atonId, deviceId, channelId);
        if (CollectionUtils.isEmpty(atonPresetLocationList)) {
            return;
        }

        AtonPresetLocation atonPresetLocation = atonPresetLocationList.get(0);
        ptzController.frontEndCommand(deviceId, channelId, 130, 0, atonPresetLocation.getPresetLocation(), 0);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long startTime = System.currentTimeMillis();
        deviceControl.recordApi(deviceId, "Record", channelId);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        deviceControl.recordApi(deviceId, "StopRecord", channelId);
        long endTime = System.currentTimeMillis();

        CheckResult checkResult = new CheckResult();
        checkResult.setAtonId(atonId);
        checkResult.setCheckTaskId(taskId);
        checkResult.setStartTime(new Timestamp(startTime));
        checkResult.setStatus(1);
        checkResult.setRecordStartTime(new Timestamp(startTime));
        checkResult.setRecordEndTime(new Timestamp(endTime));
        checkResultMapper.add(checkResult);
        Long resultId = checkResultMapper.getLastInsertId();

        Device device = deviceService.getDeviceByDeviceId(deviceId);
        if (device == null) {
            log.warn("controlAndSaveVideoRecord 未找到设备 deviceId: {},channelId:{}", deviceId, channelId);
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "未找到设备：" + deviceId);
        }

        DeviceChannel channel = channelService.getOne(deviceId, channelId);
        if (channel == null) {
            log.warn("controlAndSaveVideoRecord 未找到通道 deviceId: {},channelId:{}", deviceId, channelId);
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "未找到通道：" + channelId);
        }

        channel.setScheduleTaskResultId("taskResultId-" + resultId);
        playService.download(device, channel, sdf.format(new Date(startTime)), sdf.format(new Date(endTime)), 4,
                (code, msg, data) -> {
                    log.info("controlAndSaveVideoRecord download callback code={} msg={} data={}", code, msg, data);
                    checkResultMapper.updateStatus(resultId, 2);
                });
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ptzController.frontEndCommand(deviceId, channelId, 130, 0, 0, 0);
        log.info("controlAndSaveVideoRecord end deviceId={} channelId={} atonId={}", deviceId, channelId, atonId);
    }

    private CheckResult extractVideoFrame(String taskResultId) {
        List<String> records = null;
        records = cloudRecordServiceMapper.queryRecordFilePathList(null, taskResultId, null, null, null, null);
        log.info("queryRecordFilePathList taskResultId={} records={}", taskResultId, records);
        if (CollectionUtils.isEmpty(records)) {
            log.warn("录像记录为空 taskResultId={} records={}", taskResultId, records);
            return null;
        }

        String resourcePath = ScheduleService.class.getResource("/").getPath();
        String videoPath = records.get(0);
        String outputDir = resourcePath + "screenshots/" + taskResultId;

        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        List<String> screenshots = Lists.newArrayList();
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();
            int frameNumber = 0;
            long frameInterval = 3 * 1000000L;
            long nextFrameTime = 0;
            Frame frame;
            Java2DFrameConverter converter = new Java2DFrameConverter();
            while ((frame = grabber.grabImage()) != null) {
                long currentTimestamp = grabber.getTimestamp();
                if (currentTimestamp >= nextFrameTime) {
                    BufferedImage bufferedImage = converter.convert(frame);
                    String outputFilePath = String.format("%s/frame_%04d.jpg", outputDir, frameNumber);
                    screenshots.add(outputFilePath);
                    ImageIO.write(bufferedImage, "jpg", new File(outputFilePath));
                    frameNumber++;
                    nextFrameTime += frameInterval; // 更新下一帧时间
                }
            }
            grabber.stop(); // 停止视频读取
        } catch (Exception e) {
            log.error("extractVideoFrame error", e);
        }
        log.info("extractVideoFrame end taskResultId={} screenshots={}", taskResultId, screenshots);

        CheckResult checkResult = new CheckResult();
        checkResult.setImgsUrl(String.join(";", screenshots));
        checkResult.setVideoUrl(videoPath);
        return screenshots;
    }

    private void execCheckDerivativeTask(CheckSchedule checkSchedule) {
        log.info("execCheckDerivativeTask start 执行定时任务: {}", checkSchedule.getName());
        CheckDerivativeTask checkDerivativeTask = new CheckDerivativeTask();
        checkDerivativeTask.setCheckScheduleId(checkSchedule.getId());
        checkDerivativeTask.setName(checkSchedule.getName() + System.currentTimeMillis());
        checkDerivativeTask.setStartTime(new Timestamp(System.currentTimeMillis()));
        checkDerivativeTask.setStatus(0);
        checkDerivativeTask.setCompleteNum(0);
        checkDerivativeTask.setTotalNum(0);
        checkDerivativeTaskMapper.add(checkDerivativeTask);
        Long id = checkDerivativeTaskMapper.getLastInsertId();
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
                cronExecutor.submit(() -> {
                    try {
                        List<AtonPresetLocation> atonPresetLocationList = atonPresetLocationMapper.query(at.getId(), null, null);
                        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(atonPresetLocationList)) {
                            AtonPresetLocation atonPresetLocation = atonPresetLocationList.get(0);
                            controlAndSaveVideoRecord(atonPresetLocation.getDeviceId(), atonPresetLocation.getChannelId(), at.getId(), id);
                        }
                    } catch (Exception e) {
                        log.error("execCheckDerivativeTask error", e);
                    }
                });


            });
        });

    }

    private void execCheckColorTask(Aton aton, List<String> screenshots) {
        log.info("execCheckColorTask atonId={}", aton.getId());

    }

    private void execCheckStructureTask(Aton aton, List<String> screenshots) {
        log.info("execCheckStructureTask atonId={}", aton.getId());
    }

    private void execCheckLightTask(Aton aton, List<String> screenshots) {
        log.info("execCheckLightTask atonId={}", aton.getId());
    }

    private void checkCronTask() {
        log.info("checkCronTask start 检查定时任务");
        List<CheckSchedule> checkSchedules = checkScheduleMapper.query(null);
        if (CollectionUtils.isEmpty(checkSchedules)) {
            return;
        }
        checkSchedules.stream().filter(schedule -> {
            if (schedule.getStatus() == 0) {
                return false;
            }
            CronExpression cron = null;
//            try {
//                cron = new CronExpression(schedule.getPeriod());
//                boolean s = cron.isSatisfiedBy(new Date());
//                log.info("定时任务解析结果: schedule={} isSatisfied={}", schedule, s);
//                return s;
//            } catch (ParseException e) {
//                log.error("定时任务解析失败: {}", schedule.getName(), e);
//                return false;
//            }
            return true;

        }).forEach(schedule -> {
            execCheckDerivativeTask(schedule);
        });
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void startCheckCronTask() {
        checkCronTask();
//        scheduler.scheduleAtFixedRate(this::checkCronTask, 0, 1, TimeUnit.MINUTES);
    }

    @Async("scheduleExecutor")
    @EventListener
    public void onApplicationEvent(MediaRecordMp4Event event) {
        log.info("scheduleExecutor MediaRecordMp4Event: {}", event);
        if (!event.getStream().contains("taskResultId")) {
            return;
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Long resultId = Long.valueOf(event.getStream().split("-")[1]);
        CheckResult checkResult = extractVideoFrame(event.getStream());
        log.info("scheduleExecutor MediaRecordMp4Event: checkResult={}", checkResult);
        if (checkResult!= null &&
                StringUtils.isNotBlank(checkResult.getImgsUrl())) {

            CheckResult checkResult = checkResultMapper.get(resultId).get(0);
            CheckDerivativeTask checkDerivativeTask = checkDerivativeTaskMapper.get(checkResult.getCheckTaskId()).get(0);
            CheckSchedule checkSchedule = checkScheduleMapper.get(checkDerivativeTask.getCheckScheduleId()).get(0);
            Aton at = atonMapper.get(checkResult.getAtonId()).get(0);
            checkResultMapper.updateStatus(resultId, 3);
            if (checkSchedule.getCheckColor() == 1) {
                execCheckColorTask(at, screenshots);
            }
            if (checkSchedule.getCheckStructure() == 1) {
                execCheckStructureTask(at, screenshots);
            }
            if (checkSchedule.getCheckLight() == 1) {
                execCheckLightTask(at, screenshots);
            }
            checkResultMapper.updateStatusTime(resultId, 4, new Timestamp(System.currentTimeMillis()));
            checkDerivativeTaskMapper.incrementCompleteNum(checkDerivativeTask.getId());
        }

    }
}
