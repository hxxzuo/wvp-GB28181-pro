package com.genersoft.iot.vmp.utils;

import com.genersoft.iot.vmp.gb28181.bean.BaiduPoint;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

@Slf4j
public class GpsUtil {


    public static BaiduPoint Wgs84ToBd09(double lng, double lat) {
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

    private static final double X_PI = 3.14159265358979324 * 3000.0 / 180.0;
    private static final double PI = 3.1415926535897932384626;
    private static final double A = 6378245.0;
    private static final double EE = 0.00669342162296594323;


    /**
     * WGS84坐标系转换成腾讯地图坐标系(GCJ02)
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return double[]
     */
    public static double[] wgs84ToGcj02(double longitude, double latitude) {
        if (outOfChina(longitude, latitude)) {
            return new double[]{longitude, latitude};
        }
        double dLat = transformLat(longitude - 105.0, latitude - 35.0);
        double dLon = transformLng(longitude - 105.0, latitude - 35.0);
        double radLat = latitude / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((A / sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / ((A / (magic * sqrtMagic)) * Math.PI);
        double mgLat = latitude + dLat;
        double mgLon = longitude + dLon;
        return new double[]{mgLon, mgLat};
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    private static boolean outOfChina(double lng, double lat) {
        return (lng < 72.004 || lng > 137.8347) || (lat < 0.8293 || lat > 55.8271);
    }
}
