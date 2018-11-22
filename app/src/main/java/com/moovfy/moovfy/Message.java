package com.moovfy.moovfy;

public class Message {
    String message;
    User sender;
    long time;

    public Message(){}

    public Message(String message, User sender, long time) {
        this.message = message;
        this.sender = sender;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public User getSender() {
        return sender;
    }

    public long getTime() {
        return time;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
