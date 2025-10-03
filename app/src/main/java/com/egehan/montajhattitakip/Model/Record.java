package com.egehan.montajhattitakip.Model;

public class Record {
    private String id; //Firestore ID
    private String username;
    private String type;
    private String barcode;
    private String hat;
    private String reason;
    private String timestamp;
    private String shift;
    public Record() {}
    public Record(String id,String username, String type, String barcode, String hat, String reason, String timestamp,String shift) {
        this.id = id;
        this.username = username;
        this.type = type;
        this.barcode = barcode;
        this.hat = hat;
        this.reason = reason;
        this.timestamp = timestamp;
        this.shift = shift;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() {
        return username;
    }

    public String getType() {
        return type;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getHat() {
        return hat;
    }

    public String getReason() {
        return reason;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setHat(String hat) {
        this.hat = hat;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getShift() {
        return shift;
    }
    public void setShift(String shift) {
        this.shift = shift;
    }
}
