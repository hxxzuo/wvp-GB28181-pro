package com.genersoft.iot.vmp.gb28181.service.impl;

import com.genersoft.iot.vmp.gb28181.bean.Aton;
import com.genersoft.iot.vmp.gb28181.dao.AtonMapper;
import com.genersoft.iot.vmp.gb28181.service.IAtonService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AtonServiceImpl implements IAtonService {

    @Autowired
    private AtonMapper atonMapper;

    @Override
    public PageInfo<Aton> queryAton(int page, int count, Aton aton) {
        PageHelper.startPage(page, count);
        List<Aton> all = atonMapper.queryAtonList(aton);
        return new PageInfo<>(all);
    }
}
