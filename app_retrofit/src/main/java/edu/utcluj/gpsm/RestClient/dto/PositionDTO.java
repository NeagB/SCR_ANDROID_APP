package edu.utcluj.gpsm.RestClient.dto;

public class PositionDTO {

    private String longitude;
    private String latitude;
    private String terminalId;

    public PositionDTO() {
    }

    public PositionDTO(String longitude, String latitude, String terminalId) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.terminalId = terminalId;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
}
