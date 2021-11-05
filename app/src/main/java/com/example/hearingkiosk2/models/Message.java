package com.example.hearingkiosk2.models;

// 객체지향 프로그래밍에서는 메소드를 통해 데이터를 변경 혹은 받아온다
// set : 값을 저장하고
// get:을 통해 return 받게 한다.

public class Message {

    private String message;
    private boolean isReceived;

    public Message(String message, boolean isReceived) {
        this.message = message;
        this.isReceived = isReceived;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getIsReceived() {
        return isReceived;
    }

    public void setIsReceived(boolean isReceived) {
        this.isReceived = isReceived;
    }

}
