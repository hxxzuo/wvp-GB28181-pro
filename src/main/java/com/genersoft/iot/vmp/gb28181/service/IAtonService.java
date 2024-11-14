package com.genersoft.iot.vmp.gb28181.service;

import com.genersoft.iot.vmp.gb28181.bean.Aton;
import com.github.pagehelper.PageInfo;

public interface IAtonService {

    PageInfo<Aton> queryAton(int page, int count, Aton aton);

}
