package com.genersoft.iot.vmp.web.gb28181;

import com.genersoft.iot.vmp.gb28181.bean.AtonPresetLocation;
import com.genersoft.iot.vmp.gb28181.dao.AtonPresetLocationMapper;
import com.genersoft.iot.vmp.gb28181.service.IScheduleService;
import com.genersoft.iot.vmp.service.IUserService;
import com.genersoft.iot.vmp.storager.dao.dto.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping(value = "/auth")
public class AuthController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IScheduleService scheduleService;
    @Autowired
    private AtonPresetLocationMapper atonPresetLocationMapper;

    @GetMapping("/login")
    public String devices(String name, String passwd){
        User user = userService.getUser(name, passwd);
        if (user != null) {
            return "success";
        }else {
            return "fail";
        }
    }

    @GetMapping("/tet")
    public void tet() {
        scheduleService.startCheckCronTask();
    }

    @GetMapping("/tet2")
    public void tet2() {
        AtonPresetLocation atonPresetLocation = new AtonPresetLocation();
        atonPresetLocation.setAtonId(1L);
        atonPresetLocation.setDeviceId("1");
        atonPresetLocation.setChannelId("1");
        atonPresetLocation.setPresetLocation(1);
        atonPresetLocationMapper.add(atonPresetLocation);
        Long id2 = atonPresetLocationMapper.getLastInsertId();
        log.info("id2:{}", id2);
    }
}
