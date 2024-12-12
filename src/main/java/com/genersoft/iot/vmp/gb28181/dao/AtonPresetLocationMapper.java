package com.genersoft.iot.vmp.gb28181.dao;

import com.genersoft.iot.vmp.gb28181.bean.AtonPresetLocation;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface AtonPresetLocationMapper {
    @Select(" <script>" +
            "SELECT " +
            "id, " +
            "aton_id, " +
            "device_id, " +
            "channel_id, " +
            "preset_location" +
            " FROM aton_preset_location de" +
            " where 1 = 1 " +
            " <if test='atonId != null'> AND de.aton_id='${atonId}' </if> " +
            " <if test='deviceId != null'> AND de.device_id='${deviceId}' </if> " +
            " <if test='channelId != null'> AND de.channel_id='${channelId}' </if> " +
            " </script>")
    List<AtonPresetLocation> query(@Param("atonId") Long atonId, @Param("deviceId") String deviceId, @Param("channelId") String channelId);

    @Insert("INSERT INTO aton_preset_location (" +
            "aton_id," +
            "device_id," +
            "channel_id," +
            "preset_location" +
            ") VALUES (" +
            "#{atonId}," +
            "#{deviceId}," +
            "#{channelId}," +
            "#{presetLocation}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void add(AtonPresetLocation atonPresetLocation);

    @Update(value = {" <script>" +
            "UPDATE aton_preset_location " +
            "SET aton_id=#{atonId}, device_id=#{deviceId} , channel_id=#{channelId}, preset_location=#{presetLocation}" +
            " WHERE id=#{id}" +
            " </script>"})
    void update(AtonPresetLocation atonPresetLocation);

    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();
}
