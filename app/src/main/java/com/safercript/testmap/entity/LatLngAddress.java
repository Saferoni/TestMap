package com.safercript.testmap.entity;

/**
 * Created by pavelsafronov on 19.10.17.
 */

public class LatLngAddress {
    private double latitude;
    private double longitude;
    private String fullAddress;

    public LatLngAddress(double latitude, double longitude, String fullAddress) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.fullAddress = fullAddress;
    }

    public LatLngAddress(LatLngAddress latLngAddress) {
        this.latitude = latLngAddress.getLatitude();
        this.longitude = latLngAddress.getLongitude();
        this.fullAddress = latLngAddress.getFullAddress();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LatLngAddress that = (LatLngAddress) o;

        return fullAddress != null ? fullAddress.equals(that.fullAddress) : that.fullAddress == null;

    }

    @Override
    public int hashCode() {
        return fullAddress != null ? fullAddress.hashCode() : 0;
    }
}
