package com.genersoft.iot.vmp.gb28181.dao;

import com.genersoft.iot.vmp.gb28181.bean.CheckResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CheckResultMapper {

    @Insert("INSERT INTO check_result (" +
            "check_task_id," +
            "structure_result," +
            "color_result," +
            "light_result," +
            "start_time," +
            "end_time," +
            "status," +
            "aton_id" +
            ") VALUES (" +
            "#{checkTaskId}," +
            "#{structureResult}," +
            "#{colorResult}," +
            "#{lightResult}," +
            "#{startTime}," +
            "#{endTime}," +
            "#{status}," +
            "#{atonId}" +
            ")")
    void add(CheckResult checkResult);

    @Insert({
            "<script>",
            "INSERT INTO check_result (check_task_id, structure_result, color_result, light_result, start_time, end_time, status, aton_id) VALUES ",
            "<foreach collection='list' item='item' index='index' separator=','>",
            "(#{item.checkTaskId}, #{item.structureResult}, #{item.colorResult}, #{item.lightResult}, #{item.startTime}, #{item.endTime}, #{item.status}, #{item.atonId})",
            "</foreach>",
            "</script>"
    })
    void batchAdd(List<CheckResult> checkResults);

    @Update(value = {" <script>" +
            "UPDATE check_result " +
            "SET check_task_id=#{checkTaskId}, structure_result=#{structureResult} , color_result=#{colorResult}, light_result=#{lightResult}" +
            ", start_time=#{startTime}, end_time=#{endTime}, status=#{status}, aton_id=#{atonId}" +
            " WHERE id=#{id}" +
            " </script>"})
    void update(CheckResult checkResult);
}