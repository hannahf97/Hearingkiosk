package com.example.hearingkiosk2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hearingkiosk2.Adapter.ChatAdapter;
import com.example.hearingkiosk2.DBdata;
import com.example.hearingkiosk2.helpers.SendMessageInBg;
import com.example.hearingkiosk2.interfaces.BotReply;
import com.example.hearingkiosk2.models.Message;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.common.collect.Lists;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements BotReply {

    TextToSpeech _tts;// tts
    RecyclerView chatView; // ?????? ??????
    ChatAdapter chatAdapter; // ????????? ??????
    List<Message> messageList = new ArrayList<>(); //????????? ????????? ?????? ??????
    TextView editMessage; // ????????? ????????? (????????? ???????????? ????????????)
    ImageButton startButton; //????????? ?????? ????????? ??????
//    ImageButton muteButton;
    ConstraintLayout constraintLayout; //??? ????????? ???????????? ??????
    ConstraintLayout constraintLayout2; //????????? ??????
    ImageButton btn1; // ?????????
    ImageButton btn2; // ???????????????
    ImageButton btn3; //??????
    Handler handler;
    Integer per_price = 0;
    Integer amount_price;
    String get_food_name = "";
    Integer get_food_amount = 0;
    int mic_status = 0; // ?????????????????? ?????? ??????

    //dialogflow??? ?????? ??????
    private SessionsClient sessionsClient; //????????? ???????????????
    private SessionName sessionName; //session name??? uuid??? project id??? ??????????????????
    private String uuid = UUID.randomUUID().toString(); // ????????? ????????? ?????? -> ????????? ?????? ?????? ?????? to String()????????? ??????
    private String TAG = "MainActivity"; // ?????? ???????????? ???????????? tag ?????? ?????? ????????? ?????? ???
    SpeechRecognizer sttrec = SpeechRecognizer.createSpeechRecognizer(this); // stt

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabase = database.getReference();//db??? ??? ??????????????????




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chatView = findViewById(R.id.chatView); //????????? ???????????? ??????
        editMessage = findViewById(R.id.editMessage); //????????? ???????????????
        startButton = findViewById(R.id.startbutton);
        constraintLayout = findViewById(R.id.constraintLayout);
        constraintLayout2 = findViewById(R.id.constraintLayout2);

        constraintLayout2.setVisibility(View.GONE);
        constraintLayout.setVisibility(View.VISIBLE);
        editMessage.setVisibility(View.GONE);
        _tts = new TextToSpeech(this, ttsInitListener);

        Bundle extras = this.getIntent().getExtras();
        String _getData = "";
        if(extras != null ) {
            _getData = extras.getString("face-detect");
            Log.i(TAG, "???????????? ????????? " + _getData);
        }

        setUpBot();

        chatAdapter = new ChatAdapter(messageList, this);
        chatView.setAdapter(chatAdapter);

        if(!_getData.isEmpty()){
            Handler mhandler = new Handler();
            String final_getData = _getData;
            mhandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendMessageToBot(final_getData);
                }
            }, 500);

        }
        startButton.setOnClickListener(v -> {
            constraintLayout.setVisibility(View.GONE);
            constraintLayout2.setVisibility(View.VISIBLE);
            mDatabase.child("order").removeValue();
        });



        //onListeningCompleted();

        btn1 = findViewById(R.id.burgerbutton);
        btn2 = findViewById(R.id.saladbutton);
        btn3 = findViewById(R.id.colabutton);

        btn1.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("?????????",0);
            startActivity(intent);
        });

        btn2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("?????????",1);
            startActivity(intent);
        });
        btn3.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("?????????",2);
            startActivity(intent);
        });


        editMessage.setText("");
        //sttRecog();

    }

    private TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() //???????????? ???????????? listnenr
    {
        @Override
        public void onInit(int status)
        {
            if (status == TextToSpeech.SUCCESS) {
                _tts.setOnUtteranceProgressListener(completedListener);
            }
            else{
                return;
            }


        }
    };
    private UtteranceProgressListener completedListener = new UtteranceProgressListener() {
        @SuppressLint("LongLogTag")
        @Override
        public void onStart(String utteranceId) {
            Log.i("MainActivity.java | UtteranceProgressListener", "|" + "new system TTS speak start" + "|");

        }

        @SuppressLint("LongLogTag")
        @Override
        public void onDone(String utteranceId) {

            Log.i("MainActivity.java | UtteranceProgressListener", "|" + "new system TTS speak complete" + "|");

            setHandler();
        }

        @Override
        public void onError(String utteranceId) {

        }
    };

// ????????? ???????????? ??????
    @Override
    public void callback (DetectIntentResponse returnResponse){
        if (returnResponse != null) {
            String botReply = returnResponse.getQueryResult().getFulfillmentText();
            onSpeakingCompleted(botReply);
            if (!botReply.isEmpty()) {
                messageList.add(new Message(botReply, true));
                chatAdapter.notifyDataSetChanged();
                Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
                //speakResponse(botReply);
                selectFood(botReply);


            } else {
                Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "failed to connect!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setHandler(){
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sttRecog();
            }
        }, 30);
    }

    public void sttRecog(){
        switch (mic_status){
            case 0:
            case 2:
                mic_status = 1;
                inputVoice();
                break;
            case 1:
                mic_status = 0;
                sttrec.destroy();
                break;
        }

    }


    private void setUpBot() {
        try {
            InputStream stream = this.getResources().openRawResource(R.raw.credential);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            String projectId = ((ServiceAccountCredentials) credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(
                    FixedCredentialsProvider.create(credentials)).build();
            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(projectId, uuid); //session ????????? ????????? ?????? ?????? ?????? ??????

            Log.d(TAG, "projectId : " + projectId);
        } catch (Exception e) {
            Log.d(TAG, "setUpBot: " + e.getMessage());
        }
    }

    // message ??? ????????? ????????? ????????? ??????.
    private void sendMessageToBot(String message) {
        QueryInput input = QueryInput.newBuilder()
                .setText(TextInput.newBuilder().setText(message).setLanguageCode("ko-KR")).build();
        new SendMessageInBg((BotReply) this, sessionName, sessionsClient, input).execute();
    }




    //?????? ?????? stt ???????????? ???????????? ??????????????? ???
    public void inputVoice() {
        Log.d("inputVoice", "in function");
        try {

            Intent intentSTT = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intentSTT.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            intentSTT.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            sttrec = SpeechRecognizer.createSpeechRecognizer(this);
            sttrec.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d("STTstart", "start here");
                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {
//                    toast("??????????????????");
                    Log.d("STTend", "end here");
                }

                @Override
                public void onError(int error) {
                    String message;
                    switch (error) {
                        case SpeechRecognizer.ERROR_AUDIO:
                            message = "????????? ??????";
                            break;
                        case SpeechRecognizer.ERROR_CLIENT:
                            message = "??????????????? ??????";
                            break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                            message = "????????? ??????";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK:
                            message = "???????????? ??????";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                            message = "????????? ????????????";
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH:
                            message = "?????? ??? ??????";
                            speakResponse("?????? ?????? ?????????????????? ");
                            mic_status = 0;
                            break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                            message = "RECOGNIZER??? ??????";
                            break;
                        case SpeechRecognizer.ERROR_SERVER:
                            message = "????????? ?????????";
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            message = "????????? ????????????";
                            break;
                        default:
                            message = "??? ??? ?????? ?????????";
                            break;
                    }


                    Log.d("onError", "error : "+message);
                    mic_status = 1;
                    sttrec.destroy();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result = (ArrayList<String>) results.get(sttrec.RESULTS_RECOGNITION);
                    editMessage.setText( result.get(0) + "\n");
                    messageList.add(new Message(result.get(0).toString(),false));
                    Log.v(TAG, "result: "+result.get(0));
                    if(result.get(0).equals("???")){
                        startButton.performClick();
                    }
                    sendMessageToBot(result.get(0));
                    Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged(); //chatview?????? ????????? ????????????
                    Objects.requireNonNull(chatView.getLayoutManager())
                            .scrollToPosition(messageList.size() - 1);
                    mic_status = 2;
                    sttrec.destroy();
                    //onListeningCompleted();
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
            sttrec.startListening(intentSTT);
            new CountDownTimer(3000,1000){
                public void onTick(long m){
                }
                public void onFinish(){
                    sttrec.stopListening();
                }
            }.start();


        }
        catch (Exception e) {
            Log.e(TAG, e.toString() +" ??????????????? ");
//            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }


    private void onSpeakingCompleted(String message){
        if (!message.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            if (message.trim().equals("???")){
                startButton.performClick();
            }
            else if (message.startsWith("?????????")) {
                Toast.makeText(MainActivity.this, "????????? ?????? ?????????.", Toast.LENGTH_SHORT).show();
                intent.putExtra("?????????",0);
                startActivity(intent);
                //btn1.performClick(); // startButton??? ????????? ???????????? ??????


            } else if (message.startsWith("?????????")) {
                Toast.makeText(MainActivity.this, "????????? ?????? ?????????.", Toast.LENGTH_SHORT).show();
                intent.putExtra("?????????",1);
                startActivity(intent);
                //btn2.performClick(); // startButton??? ????????? ???????????? ??????
            } else if (message.startsWith("??????")) {
                Toast.makeText(MainActivity.this, "?????? ?????? ?????????.", Toast.LENGTH_SHORT).show();
                btn3.performClick(); // startButton??? ????????? ???????????? ??????
            } else if (message.contains("????????????")){
                mic_status = 1;
                intent.putExtra("?????????",3);
                startActivity(intent);

            }
            else {
                //Toast.makeText(MainActivity.this, "?????? ????????? ????????? ????????????. ", Toast.LENGTH_SHORT).show();
            } //????????? ????????? ???????????? ????????? ???????????? ????????? false??? message list??? ??????
            messageList.add(new Message(message, false));
        } else {
            //Toast.makeText(MainActivity.this, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
        }
    }





    // ?????? ?????? ?????? ???????????? ??????
    @SuppressLint("LongLogTag")
    private void speakResponse(String botReply) {
        mic_status = 0;
        Log.i("MainActivity.java | speak", "|System TTS speak|" + botReply + "|");
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "messageID");
        try {
            _tts.speak(botReply, TextToSpeech.QUEUE_FLUSH, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sttRecog();
    }

    // initfirebase ??? ?????? ???????????????
    @SuppressLint("LongLogTag")
    private void selectFood(String response){
        int idx = 0;
        int idxs = response.indexOf("???");
        if(response.contains("???????????????") || response.contains("?????????????????????")){
            mic_status = 1;
            return;
        }
        else if(response.contains("???") && response.contains("???")){
            idx = response.indexOf("???");
            get_food_name = response.substring(0,idx);
            initFirebaseRealTimeDataBase(get_food_name);
            get_food_amount = Integer.parseInt(response.substring(idx+2,idxs));
            Log.i(TAG, "food_name, food_amount"+get_food_name+ get_food_amount);
            //addFoodorder(get_food_name,get_food_amount);
        }else if ( response.contains("???") && response.contains("???")){
            idx = response.indexOf("???");
            get_food_name = response.substring(0,idx);
            initFirebaseRealTimeDataBase(get_food_name);
            get_food_amount = Integer.parseInt(response.substring(idx+2,idxs));
            Log.i(TAG, "food_name, food_amount"+get_food_name+ get_food_amount);
            //addFoodorder(get_food_name,get_food_amount);
        }
        else{
            mic_status = 0;

        }
        Log.i("MainActivity.java | selectFood", "|System TTS speak|" + response + "|");
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "messageID");
        try {
            _tts.speak(response, TextToSpeech.QUEUE_FLUSH, map);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
// reference???????????????
    private void initFirebaseRealTimeDataBase(String get_food_name){
        DatabaseReference reference = database.getReference("Menu");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DBdata data =  dataSnapshot.child(get_food_name).getValue(DBdata.class);
                Log.d("TAG ", "????????? ?????? start: "+ data);
                if(data == null){
                    speakResponse("?????? ?????? ?????????????????? ");

                }else{
                    per_price = data.per_price;
                }

                Log.d("TAG", "per_price ??????" + per_price);
                addFoodorder();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    private void addFoodorder(){
        Log.i("TAG", "per_price?????? :" + per_price);
        Integer food_price = get_food_amount * per_price;
        Integer food_per_price = per_price;
        Toast.makeText(getApplicationContext(), get_food_name +" "+ get_food_amount+"?????? " + "??????????????? ??????", Toast.LENGTH_LONG).show();
        Order_info order_info = new Order_info(get_food_name, get_food_amount, food_per_price, food_price);

        mDatabase.child("order").child(get_food_name).setValue(order_info);


    }


}