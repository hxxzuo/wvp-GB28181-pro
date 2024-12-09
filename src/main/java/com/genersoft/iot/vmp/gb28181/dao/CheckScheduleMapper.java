package com.genersoft.iot.vmp.gb28181.dao;

import com.genersoft.iot.vmp.gb28181.bean.CheckSchedule;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CheckScheduleMapper {

    @Select(" <script>" +
            "SELECT " +
            "id, " +
            "name, " +
            "administrators, " +
            "aton_list_id, " +
            "period, " +
            "status, " +
            "priority," +
            "check_light," +
            "check_color," +
            "check_structure," +
            "create_time," +
            "update_time" +
            " FROM check_schedule de" +
            " where 1 = 1 " +
            " <if test='name != null'> AND de.name='${name}' </if> " +
            " </script>")
    List<CheckSchedule> query(@Param("name") String name);

    @Delete("DELETE FROM check_schedule WHERE id=#{id}")
    int delete(Long id);

    @Insert("INSERT INTO check_schedule (" +
            "name," +
            "administrators," +
            "aton_list_id," +
            "period," +
            "status," +
            "priority," +
            "check_light," +
            "check_color," +
            "check_structure" +
            ") VALUES (" +
            "#{name}," +
            "#{administrators}," +
            "#{atonListId}," +
            "#{period}," +
            "#{status}," +
            "#{priority}," +
            "#{checkLight}," +
            "#{checkColor}," +
            "#{checkStructure}" +
            ")")
    void add(CheckSchedule checkSchedule);

    @Update(value = {" <script>" +
            "UPDATE check_schedule " +
            "SET name=#{name}, administrators=#{administrators} , aton_list_id=#{atonListId}, period=#{period}" +
            ", status=#{status}, priority=#{priority}, check_light=#{checkLight}, check_color=#{checkColor}, check_structure=#{checkStructure}" +
            " WHERE id=#{id}" +
            " </script>"})
    void update(CheckSchedule checkSchedule);
}
