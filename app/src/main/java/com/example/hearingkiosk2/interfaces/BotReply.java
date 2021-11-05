package com.example.hearingkiosk2.interfaces;

import com.google.cloud.dialogflow.v2.DetectIntentResponse;
// 대략적인 틀만 만든후에 구체적인 내용은 하위클래스에서 구현한다.
// 나중에 mainactivity 를 보면 callback 함수가 존재한다.

public interface BotReply {
    void callback(DetectIntentResponse returnResponse);
}

