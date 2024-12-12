package com.genersoft.iot.vmp.gb28181.dao;

import com.genersoft.iot.vmp.gb28181.bean.CheckDerivativeTask;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CheckDerivativeTaskMapper {

    @Insert("INSERT INTO check_derivative_task (" +
            "name," +
            "check_schedule_id," +
            "status," +
            "start_time," +
            "end_time," +
            "total_num," +
            "complete_num" +
            ") VALUES (" +
            "#{name}," +
            "#{checkScheduleId}," +
            "#{status}," +
            "#{startTime}," +
            "#{endTime}," +
            "#{totalNum}," +
            "#{completeNum}" +
            ")")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = Long.class)
    void add(CheckDerivativeTask checkDerivativeTask);

    @Update(value = {" <script>" +
            "UPDATE check_derivative_task " +
            "SET name=#{name}, check_schedule_id=#{checkScheduleId} , status=#{status}, start_time=#{startTime}" +
            ", end_time=#{endTime}, total_num=#{totalNum}, complete_num=#{completeNum}" +
            " WHERE id=#{id}" +
            " </script>"})
    void update(CheckDerivativeTask checkDerivativeTask);

    @Update(value = {" <script>" +
            "UPDATE check_derivative_task " +
            "SET complete_num=complete_num+1" +
            " WHERE id=#{id}" +
            " </script>"})
    void incrementCompleteNum(Long id);

    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();

    @Select(" <script>" +
            "SELECT " +
            "id," +
            "name," +
            "check_schedule_id," +
            "status," +
            "start_time," +
            "end_time," +
            "total_num," +
            "complete_num" +
            " FROM check_derivative_task de" +
            " where de.id='${id}' " +
            " </script>")
    List<CheckDerivativeTask> get(@Param("id") Long id);


    @Select(" <script>" +
            "SELECT " +
            "id," +
            "name," +
            "check_schedule_id," +
            "status," +
            "start_time," +
            "end_time," +
            "total_num," +
            "complete_num" +
            " FROM check_derivative_task de" +
            " where 1 = 1 " +
            " <if test='name != null'> AND de.name='${name}' </if> " +
            " </script>")
    List<CheckDerivativeTask> query(@Param("name") String name);
}
