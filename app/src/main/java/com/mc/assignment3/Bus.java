package com.mc.assignment3;

public class Bus {
    private String routeId;
    private double latitude;
    private double longitude;
    private String congestion;
    private String status;
    private String occupancy;
    private String scheduled;

    public Bus(String routeId, double latitude, double longitude, String congestion, String status, String occupancy, String scheduled) {
        this.routeId = routeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.congestion = congestion;
        this.status = status;
        this.occupancy = occupancy;
        this.scheduled = scheduled;
    }

    @Override
    public String toString() {
        return "Bus{" +
                "routeId='" + routeId + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", congestion='" + congestion + '\'' +
                ", status='" + status + '\'' +
                ", occupancy='" + occupancy + '\'' +
                ", scheduled='" + scheduled + '\'' +
                '}';
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
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

    public String getCongestion() {
        return congestion;
    }

    public void setCongestion(String congestion) {
        this.congestion = congestion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(String occupancy) {
        this.occupancy = occupancy;
    }

    public String getScheduled() {
        return scheduled;
    }

    public void setScheduled(String scheduled) {
        this.scheduled = scheduled;
    }
}
