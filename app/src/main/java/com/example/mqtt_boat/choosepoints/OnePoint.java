package com.example.mqtt_boat.choosepoints;

public class OnePoint {
    private String P_lon;
    private String P_lat;
    private String P_ID;

    public String getP_lon() {
        return P_lon;
    }

    public void setP_lon(String p_lon) {
        P_lon = p_lon;
    }

    public String getP_lat() {
        return P_lat;
    }

    public void setP_lat(String p_lat) {
        P_lat = p_lat;
    }

    public String getP_ID() {
        return P_ID;
    }

    public void setP_ID(String p_ID) {
        P_ID = p_ID;
    }

    @Override
    public String toString() {
        return "OnePoint{" +
                "P_lon='" + P_lon + '\'' +
                ", P_lat='" + P_lat + '\'' +
                ", P_ID='" + P_ID + '\'' +
                '}';
    }
}
