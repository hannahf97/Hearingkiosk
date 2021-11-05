package com.example.hearingkiosk2.helpers;

import android.os.AsyncTask;
import android.util.Log;

import com.example.hearingkiosk2.interfaces.BotReply;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;


// 메세지를 background로 보내는 것
// 백그라운드란 : main activity.java 이런곳에서 안하고 다른곳에서 실행한다는뜻

public class SendMessageInBg extends AsyncTask<Void, Void, DetectIntentResponse> {
    private SessionName session;
    private SessionsClient sessionsClient;
    private QueryInput queryInput;
    private String TAG = "async";
    private BotReply botReply;

    //메세지를 보내는것
    public SendMessageInBg(BotReply botReply, SessionName session, SessionsClient sessionsClient,
                           QueryInput queryInput) {
        this.botReply = botReply;
        this.session = session;
        this.sessionsClient = sessionsClient;
        this.queryInput = queryInput;
    }

    //intent response를 탐지해서 do in background
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

    // 대답을 post 하는것
    @Override
    protected void onPostExecute(DetectIntentResponse response) {
        //handle return response here
        botReply.callback(response);
    }
}
