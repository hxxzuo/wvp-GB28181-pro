package com.genersoft.iot.vmp.gb28181.dao;

import com.genersoft.iot.vmp.gb28181.bean.CheckResult;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Mapper
@Repository
public interface CheckResultMapper {

    @Insert("INSERT INTO check_result (" +
            "check_task_id," +
            "structure_result," +
            "color_result," +
            "light_result," +
            "record_start_time," +
            "record_end_time," +
            "start_time," +
            "end_time," +
            "status," +
            "aton_id" +
            ") VALUES (" +
            "#{checkTaskId}," +
            "#{structureResult}," +
            "#{colorResult}," +
            "#{lightResult}," +
            "#{recordStartTime}," +
            "#{recordEndTime}," +
            "#{startTime}," +
            "#{endTime}," +
            "#{status}," +
            "#{atonId}" +
            ")")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = Long.class)
    void add(CheckResult checkResult);

//    @Insert({
//            "<script>",
//            "INSERT INTO check_result (check_task_id, structure_result, color_result, light_result, start_time, end_time, status, aton_id) VALUES ",
//            "<foreach collection='list' item='item' index='index' separator=','>",
//            "(#{item.checkTaskId}, #{item.structureResult}, #{item.colorResult}, #{item.lightResult}, #{item.startTime}, #{item.endTime}, #{item.status}, #{item.atonId})",
//            "</foreach>",
//            "</script>"
//    })
//    void batchAdd(List<CheckResult> checkResults);

//    @Update(value = {" <script>" +
//            "UPDATE check_result " +
//            "SET check_task_id=#{checkTaskId}, structure_result=#{structureResult} , color_result=#{colorResult}, light_result=#{lightResult}" +
//            ", start_time=#{startTime}, end_time=#{endTime}, status=#{status}, aton_id=#{atonId}" +
//            " WHERE id=#{id}" +
//            " </script>"})
//    void update(CheckResult checkResult);

    @Update(value = {" <script>" +
            "UPDATE check_result " +
            "SET  status=#{status}" +
            " WHERE id=#{id}" +
            " </script>"})
    void updateStatus(Long id, Integer status);

    @Update(value = {" <script>" +
            "UPDATE check_result " +
            "SET  status=#{status}, endTime=#{endTime}, video_url=#{videoUrl}, imgs_url={imgsUrl}" +
            " WHERE id=#{id}" +
            " </script>"})
    void updateStatusTimeUrl(Long id, Integer status, Timestamp endTime,String videoUrl,String imgsUrl);

    @Select(" <script>" +
            "SELECT " +
            "id," +
            "check_task_id," +
            "structure_result," +
            "color_result," +
            "light_result," +
            "record_start_time," +
            "record_end_time," +
            "start_time," +
            "end_time," +
            "status," +
            "aton_id" +
            " FROM check_result de" +
            " where de.id='${id}' " +
            " </script>")
    List<CheckResult> get(@Param("id") Long id);

    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();

    @Select(" <script>" +
            "SELECT " +
            "id," +
            "check_task_id," +
            "structure_result," +
            "color_result," +
            "light_result," +
            "record_start_time," +
            "record_end_time," +
            "start_time," +
            "end_time," +
            "status," +
            "aton_id" +
            " FROM check_result de" +
            " where 1 = 1 " +
            " <if test='name != null'> AND de.name='${name}' </if> " +
            " </script>")
    List<CheckResult> query(@Param("name") String name);
}