package com.genersoft.iot.vmp.gb28181.dao;

import com.genersoft.iot.vmp.gb28181.bean.CheckDerivativeTask;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

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
    Long add(CheckDerivativeTask checkDerivativeTask);

    @Update(value = {" <script>" +
            "UPDATE check_derivative_task " +
            "SET name=#{name}, check_schedule_id=#{checkScheduleId} , status=#{status}, start_time=#{startTime}" +
            ", end_time=#{endTime}, total_num=#{totalNum}, complete_num=#{completeNum}" +
            " WHERE id=#{id}" +
            " </script>"})
    void update(CheckDerivativeTask checkDerivativeTask);


}
