package com.genersoft.iot.vmp.web.gb28181;

import com.alibaba.fastjson2.JSONObject;
import com.genersoft.iot.vmp.conf.SipConfig;
import com.genersoft.iot.vmp.gb28181.bean.Aton;
import com.genersoft.iot.vmp.gb28181.service.IAtonService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * API兼容：系统接口
 */
@Controller
@Slf4j
@RequestMapping(value = "/api/v1")
public class ApiController {

    @Autowired
    private SipConfig sipConfig;
    @Autowired
    private IAtonService atonService;

    @GetMapping("/getserverinfo")
    private JSONObject getserverinfo() {
        JSONObject result = new JSONObject();
        result.put("Authorization", "ceshi");
        result.put("Hardware", "");
        result.put("InterfaceVersion", "2.5.5");
        result.put("IsDemo", "");
        result.put("Hardware", "false");
        result.put("APIAuth", "false");
        result.put("RemainDays", "永久");
        result.put("RunningTime", "");
        result.put("ServerTime", "2020-09-02 17：11");
        result.put("StartUpTime", "2020-09-02 17：11");
        result.put("Server", "");
        result.put("SIPSerial", sipConfig.getId());
        result.put("SIPRealm", sipConfig.getDomain());
        result.put("SIPHost", sipConfig.getShowIp());
        result.put("SIPPort", sipConfig.getPort());
        result.put("ChannelCount", "1000");
        result.put("VersionType", "");
        result.put("LogoMiniText", "");
        result.put("LogoText", "");
        result.put("CopyrightText", "");

        return result;
    }

    @GetMapping(value = "/userinfo")
    private JSONObject userinfo() {
//        JSONObject result = new JSONObject();
//        result.put("ID","ceshi");
//        result.put("Hardware","");
//        result.put("InterfaceVersion","2.5.5");
//        result.put("IsDemo","");
//        result.put("Hardware","false");
//        result.put("APIAuth","false");
//        result.put("RemainDays","永久");
//        result.put("RunningTime","");
//        result.put("ServerTime","2020-09-02 17：11");
//        result.put("StartUpTime","2020-09-02 17：11");
//        result.put("Server","");
//        result.put("SIPSerial", sipConfig.getId());
//        result.put("SIPRealm", sipConfig.getDomain());
//        result.put("SIPHost", sipConfig.getIp());
//        result.put("SIPPort", sipConfig.getPort());
//        result.put("ChannelCount","1000");
//        result.put("VersionType","");
//        result.put("LogoMiniText","");
//        result.put("LogoText","");
//        result.put("CopyrightText","");

        return null;
    }

    /**
     * 系统接口 - 登录
     *
     * @param username 用户名
     * @param password 密码(经过md5加密,32位长度,不带中划线,不区分大小写)
     * @return
     */
    @GetMapping(value = "/login")
    @ResponseBody
    private JSONObject login(String username, String password) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("模拟接口> 登录 API调用，username：%s ，password：%s ",
                    username, password));
        }

        JSONObject result = new JSONObject();
        result.put("CookieToken", "ynBDDiKMg");
        result.put("URLToken", "MOBkORkqnrnoVGcKIAHXppgfkNWRdV7utZSkDrI448Q.oxNjAxNTM4NDk3LCJwIjoiZGJjODg5NzliNzVj" +
                "Nzc2YmU5MzBjM2JjNjg1ZWFiNGI5ZjhhN2Y0N2RlZjg3NWUyOTJkY2VkYjkwYmEwMTA0NyIsInQiOjE2MDA5MzM2OTcsInUiOiI" +
                "4ODlkZDYyM2ViIn0eyJlIj.GciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJhb");
        result.put("TokenTimeout", 604800);
        result.put("AuthToken", "MOBkORkqnrnoVGcKIAHXppgfkNWRdV7utZSkDrI448Q.oxNjAxNTM4NDk3LCJwIjoiZGJjODg5NzliNzVj" +
                "Nzc2YmU5MzBjM2JjNjg1ZWFiNGI5ZjhhN2Y0N2RlZjg3NWUyOTJkY2VkYjkwYmEwMTA0NyIsInQiOjE2MDA5MzM2OTcsInUiOiI" +
                "4ODlkZDYyM2ViIn0eyJlIj.GciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJhb");
        result.put("Token", "ynBDDiKMg");
        return result;
    }

    @PostMapping(value = "/atonlist")
    public PageInfo atonlist(Integer start, Integer limit, Aton aton) {

        PageInfo<Aton> atons = atonService.queryAton(start, limit, aton);
        return atons;
    }
}
