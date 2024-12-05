package com.genersoft.iot.vmp.gb28181.dao;

import com.genersoft.iot.vmp.gb28181.bean.Aton;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface AtonMapper {

    @Select(" <script>" +
            "SELECT " +
            "id, " +
            "name, " +
            "longitude, " +
            "latitude, " +
            "type, " +
            "address, " +
            "attribute," +
            "administer," +
            "maintenance," +
            "belong," +
            "waters," +
            "image" +
            " FROM aton de" +
            " where 1 = 1 " +
            " <if test='name != null'> AND de.name='${name}' </if> " +
            " <if test='type != null'> AND de.type='${type}' </if> " +
            " </script>")
    List<Aton> queryAtonList(@Param("name") String name,@Param("type") String type);
}
