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
            "(SELECT count(0) FROM aton as count " +
            " FROM aton de" +
            " where 1 = 1 " +
            " <if test='query != null'> AND de.name=${aton.name} AND de.type=${aton.type} AND de.maintenance=${aton.maintenance} " +
            "AND de.belong=${aton.belong} AND de.waters=${aton.waters} </if> " +
            " </script>")
    List<Aton> queryAtonList(@Param("aton") Aton aton);
}
