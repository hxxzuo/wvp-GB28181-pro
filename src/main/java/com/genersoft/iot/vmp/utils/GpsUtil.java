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

    public static String calculateRelativeDirection(double lat1, double lng1, double lat2, double lng2) {
        double dLng = lng2 - lng1;
        double dLat = lat2 - lat1;

        double angle = Math.atan2(dLng, dLat) * 180.0 / Math.PI;

        // 将角度转换为0-360度
        angle = (angle + 360.0) % 360.0;

        // 确定方位
        String direction;
        if (angle >= 0 && angle < 22.5) {
            direction = "北";
        } else if (angle >= 22.5 && angle < 67.5) {
            direction = "东北";
        } else if (angle >= 67.5 && angle < 112.5) {
            direction = "东";
        } else if (angle >= 112.5 && angle < 157.5) {
            direction = "东南";
        } else if (angle >= 157.5 && angle < 202.5) {
            direction = "南";
        } else if (angle >= 202.5 && angle < 247.5) {
            direction = "西南";
        } else if (angle >= 247.5 && angle < 292.5) {
            direction = "西";
        } else if (angle >= 292.5 && angle < 337.5) {
            direction = "西北";
        } else {
            direction = "北";
        }

        return String.format("%s%d度", direction, (int) angle);
    }
}
