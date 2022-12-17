package com.example.mqtt_boat.choosepoints;

import com.baidu.mapapi.model.LatLng;

public class Utils {
    public static boolean isLatlngEqual(LatLng latLng0, LatLng latLng1) {
        if (latLng0.latitude == latLng1.latitude
                && latLng0.longitude == latLng1.longitude) {
            return true;
        }

        return false;
    }
}
