package com.genersoft.iot.vmp.utils;

import com.genersoft.iot.vmp.gb28181.bean.BaiduPoint;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

@Slf4j
public class GpsUtil {


    public static BaiduPoint Wgs84ToBd09(String xx, String yy) {


        double lng = Double.parseDouble(xx);
        double lat = Double.parseDouble(yy);
        Double[] gcj02 = Coordtransform.WGS84ToGCJ02(lng, lat);
        Double[] doubles = Coordtransform.GCJ02ToBD09(gcj02[0], gcj02[1]);
        BaiduPoint bdPoint = new BaiduPoint();
        bdPoint.setBdLng(doubles[0] + "");
        bdPoint.setBdLat(doubles[1] + "");
        return bdPoint;
    }

    /**
     * BASE64解码
     *
     * @param str
     * @return string
     */
    public static byte[] decode(String str) {
        byte[] bt = null;
        final Base64.Decoder decoder = Base64.getDecoder();
        bt = decoder.decode(str); // .decodeBuffer(str);
        return bt;
    }


    private static final int EARTH_RADIUS = 6371; // Radius of the earth in km

    public static double calculateDistance(double startLat, double startLong, double endLat, double endLong) {
        double dLat = Math.toRadians(endLat - startLat);
        double dLong = Math.toRadians(endLong - startLong);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) *
                        Math.sin(dLong / 2) * Math.sin(dLong / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c * 1000;
    }
}
