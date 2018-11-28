package com.moovfy.moovfy;

public class Message {
    String message;
    String nomDest;
    String senderUid;
    long time;

    public Message(){}

    public Message(String message, String nomDest, String senderUid, long time) {
        this.message = message;
        this.senderUid = senderUid;
        this.nomDest = nomDest;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public String getNomDest() {
        return nomDest;
    }

    public void setNomDest(String nomDest) {
        this.nomDest = nomDest;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public long getTime() {
        return time;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public void setTime(long time) {
        this.time = time;
    }
}
