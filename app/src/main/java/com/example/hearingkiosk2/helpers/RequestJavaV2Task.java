package com.example.hearingkiosk2.helpers;

import android.os.AsyncTask;
import android.util.Log;

import com.example.hearingkiosk2.interfaces.BotReply;
import com.example.hearingkiosk2.interfaces.BotReply;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;

import static android.content.ContentValues.TAG;

// 이게 asynchronous (비동기 ) 로 실행
// 필요에 따라 만들어진 스레드가 메인스레드와 상호 작용후 종료 하도록 만들어짐

public class RequestJavaV2Task extends AsyncTask<Void, Void, DetectIntentResponse>  {
    private BotReply mInterface; // bot reply를 mInterface라 이름붙임
    private SessionName session;
    private SessionsClient sessionsClient;
    private QueryInput queryInput;

    public RequestJavaV2Task(BotReply mInterface, SessionName session, SessionsClient sessionsClient,
                             QueryInput queryInput) {
        this.mInterface = mInterface;
        this.session = session;
        this.sessionsClient = sessionsClient;
        this.queryInput = queryInput;
    }

    // intent ( 질문->답변 dialogflow에 있는 것 )을 탐지하는 것을 백그라운드에서 실행한다
    @Override
    protected DetectIntentResponse doInBackground(Void... voids) {
        try {
            DetectIntentRequest detectIntentRequest =
                    DetectIntentRequest.newBuilder()
                            .setSession(session.toString())
                            .setQueryInput(queryInput)
                            .build();
            return sessionsClient.detectIntent(detectIntentRequest);
        } catch (Exception e) {
            Log.d(TAG, "doInBackground: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // 대답을 보여주도록 실행시킴
    @Override
    protected void onPostExecute(DetectIntentResponse response) {
        mInterface.callback(response);
    }
}
