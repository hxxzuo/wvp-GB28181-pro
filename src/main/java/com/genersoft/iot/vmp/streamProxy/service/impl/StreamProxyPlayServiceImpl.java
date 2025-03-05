package com.genersoft.iot.vmp.streamProxy.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.genersoft.iot.vmp.common.StreamInfo;
import com.genersoft.iot.vmp.conf.exception.ControllerException;
import com.genersoft.iot.vmp.media.bean.MediaInfo;
import com.genersoft.iot.vmp.media.bean.MediaServer;
import com.genersoft.iot.vmp.media.event.media.MediaArrivalEvent;
import com.genersoft.iot.vmp.media.service.IMediaServerService;
import com.genersoft.iot.vmp.service.bean.ErrorCallback;
import com.genersoft.iot.vmp.service.bean.InviteErrorCode;
import com.genersoft.iot.vmp.streamProxy.bean.StreamProxy;
import com.genersoft.iot.vmp.streamProxy.dao.StreamProxyMapper;
import com.genersoft.iot.vmp.streamProxy.service.IStreamProxyPlayService;
import com.genersoft.iot.vmp.vmanager.bean.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import javax.sip.message.Response;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 视频代理业务
 */
@Slf4j
@Service
@DS("master")
public class StreamProxyPlayServiceImpl implements IStreamProxyPlayService {

    @Autowired
    private StreamProxyMapper streamProxyMapper;

    @Autowired
    private IMediaServerService mediaServerService;

    private ConcurrentHashMap<Integer, ErrorCallback<StreamInfo>> callbackMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Integer, StreamInfo> streamInfoMap = new ConcurrentHashMap<>();

    /**
     * 流到来的处理
     */
    @Async("taskExecutor")
    @Transactional
    @EventListener
    public void onApplicationEvent(MediaArrivalEvent event) {
        if ("rtsp".equals(event.getSchema())) {
            StreamProxy streamProxy = streamProxyMapper.selectOneByAppAndStream(event.getApp(), event.getStream());
            if (streamProxy != null) {
                ErrorCallback<StreamInfo> callback = callbackMap.remove(streamProxy.getId());
                StreamInfo streamInfo = streamInfoMap.remove(streamProxy.getId());
                if (callback != null && streamInfo != null) {
                    callback.run(InviteErrorCode.SUCCESS.getCode(), InviteErrorCode.SUCCESS.getMsg(), streamInfo);
                }
            }
        }
    }

    @Override
    public void start(int id, ErrorCallback<StreamInfo> callback) {
        StreamProxy streamProxy = streamProxyMapper.select(id);
        if (streamProxy == null) {
            throw new ControllerException(ErrorCode.ERROR404.getCode(), "代理信息未找到");
        }
        StreamInfo streamInfo = startProxy(streamProxy);
        if (streamInfo == null) {
            callback.run(Response.BUSY_HERE, "busy here", null);
            return;
        }
        callbackMap.put(id, callback);
        streamInfoMap.put(id, streamInfo);

        MediaServer mediaServer = mediaServerService.getOne(streamProxy.getMediaServerId());
        if (mediaServer != null) {
            MediaInfo mediaInfo = mediaServerService.getMediaInfo(mediaServer, streamProxy.getApp(), streamProxy.getStream());
            if (mediaInfo != null) {
                callbackMap.remove(id);
                streamInfoMap.remove(id);
                callback.run(InviteErrorCode.SUCCESS.getCode(), InviteErrorCode.SUCCESS.getMsg(), streamInfo);
            }
        }
    }

    @Override
    public StreamInfo start(int id, Boolean record, ErrorCallback<StreamInfo> callback) {
        StreamProxy streamProxy = streamProxyMapper.select(id);
        if (streamProxy == null) {
            throw new ControllerException(ErrorCode.ERROR404.getCode(), "代理信息未找到");
        }
        if (record != null) {
            streamProxy.setEnableMp4(record);
        }
        StreamInfo streamInfo = startProxy(streamProxy);
        if (streamInfo != null && callback != null) {
            callback.run(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg(), streamInfo);
        }
        return streamInfo;
    }

    @Override
    public StreamInfo startProxy(StreamProxy streamProxy){
        if (!streamProxy.isEnable()) {
            return null;
        }
        MediaServer mediaServer;
        String mediaServerId = streamProxy.getRelatesMediaServerId();
        if (mediaServerId == null) {
            mediaServer = mediaServerService.getMediaServerForMinimumLoad(null);
        }else {
            mediaServer = mediaServerService.getOne(mediaServerId);
        }
        if (mediaServer == null) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), mediaServerId == null?"未找到可用的媒体节点":"未找到节点" + mediaServerId);
        }
        StreamInfo streamInfo = mediaServerService.startProxy(mediaServer, streamProxy);
        if (mediaServerId == null || !mediaServerId.equals(mediaServer.getId())) {
            streamProxy.setMediaServerId(mediaServer.getId());
            streamProxyMapper.addStream(streamProxy);
        }
        return streamInfo;
    }

    @Override
    public void stop(int id) {
        StreamProxy streamProxy = streamProxyMapper.select(id);
        if (streamProxy == null) {
            throw new ControllerException(ErrorCode.ERROR404.getCode(), "代理信息未找到");
        }
        stopProxy(streamProxy);
    }

    @Override
    public void stopProxy(StreamProxy streamProxy){

        String mediaServerId = streamProxy.getMediaServerId();
        Assert.notNull(mediaServerId, "代理节点不存在");

        MediaServer mediaServer = mediaServerService.getOne(mediaServerId);
        if (mediaServer == null) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "媒体节点不存在");
        }
        if (ObjectUtils.isEmpty(streamProxy.getStreamKey())) {
            mediaServerService.closeStreams(mediaServer, streamProxy.getApp(), streamProxy.getStream());
        }else {
            mediaServerService.stopProxy(mediaServer, streamProxy.getStreamKey());
        }
        streamProxyMapper.removeStream(streamProxy.getId());
    }

}
