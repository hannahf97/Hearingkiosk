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
    RecyclerView chatView; // 챗봇 화면
    ChatAdapter chatAdapter; // 어뎁터 정의
    List<Message> messageList = new ArrayList<>(); //배열의 형태로 받을 예정
    TextView editMessage; // 작성할 메세지 (여기선 텍스트로 보내는거)
    ImageButton startButton; //언니가 만든 스타트 버튼
//    ImageButton muteButton;
    ConstraintLayout constraintLayout; //젤 처음에 나와야할 화면
    ConstraintLayout constraintLayout2; //두번째 화면
    ImageButton btn1; // 햄버거
    ImageButton btn2; // 사이드메뉴
    ImageButton btn3; //음료
    Handler handler;
    Integer per_price = 0;
    Integer amount_price;
    String get_food_name = "";
    Integer get_food_amount = 0;
    int mic_status = 0; // 말할수있는지 상태 확인

    //dialogflow를 위한 것들
    private SessionsClient sessionsClient; //서버의 클라이언트
    private SessionName sessionName; //session name은 uuid와 project id를 사용해야한다
    private String uuid = UUID.randomUUID().toString(); // 유일한 식별자 생성 -> 문자열 표현 얻기 위해 to String()메소드 출력
    private String TAG = "MainActivity"; // 현재 사용하는 클래스에 tag 상수 선언 규칙을 하는 것
    SpeechRecognizer sttrec = SpeechRecognizer.createSpeechRecognizer(this); // stt

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabase = database.getReference();//db도 강 여기서해보쟈




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chatView = findViewById(R.id.chatView); //챗봇이 들어가는 화면
        editMessage = findViewById(R.id.editMessage); //메세지 작성하는곳
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
            Log.i(TAG, "얼굴인식 메세지 " + _getData);
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
            intent.putExtra("페이지",0);
            startActivity(intent);
        });

        btn2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("페이지",1);
            startActivity(intent);
        });
        btn3.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("페이지",2);
            startActivity(intent);
        });


        editMessage.setText("");
        //sttRecog();

    }

    private TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() //시작할떄 사용되는 listnenr
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

// 답변을 가져오는 부분
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
            sessionName = SessionName.of(projectId, uuid); //session 이름을 파일명 중복 방지 하기 위해

            Log.d(TAG, "projectId : " + projectId);
        } catch (Exception e) {
            Log.d(TAG, "setUpBot: " + e.getMessage());
        }
    }

    // message 를 봇에게 보내는 역할을 한다.
    private void sendMessageToBot(String message) {
        QueryInput input = QueryInput.newBuilder()
                .setText(TextInput.newBuilder().setText(message).setLanguageCode("ko-KR")).build();
        new SendMessageInBg((BotReply) this, sessionName, sessionsClient, input).execute();
    }




    //이건 아마 stt 음성인식 기능에서 제공해주는 것
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
//                    toast("음성입력종료");
                    Log.d("STTend", "end here");
                }

                @Override
                public void onError(int error) {
                    String message;
                    switch (error) {
                        case SpeechRecognizer.ERROR_AUDIO:
                            message = "오디오 에러";
                            break;
                        case SpeechRecognizer.ERROR_CLIENT:
                            message = "클라이언트 에러";
                            break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                            message = "퍼미션 없음";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK:
                            message = "네트워크 에러";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                            message = "네트웍 타임아웃";
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH:
                            message = "찾을 수 없음";
                            speakResponse("다시 한번 말씀해주세요 ");
                            mic_status = 0;
                            break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                            message = "RECOGNIZER가 바쁨";
                            break;
                        case SpeechRecognizer.ERROR_SERVER:
                            message = "서버가 이상함";
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            message = "말하는 시간초과";
                            break;
                        default:
                            message = "알 수 없는 오류임";
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
                    if(result.get(0).equals("네")){
                        startButton.performClick();
                    }
                    sendMessageToBot(result.get(0));
                    Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged(); //chatview에서 데이터 변환확인
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
            Log.e(TAG, e.toString() +" 에러메세지 ");
//            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }


    private void onSpeakingCompleted(String message){
        if (!message.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            if (message.trim().equals("네")){
                startButton.performClick();
            }
            else if (message.startsWith("햄버거")) {
                Toast.makeText(MainActivity.this, "햄버거 메뉴 입니다.", Toast.LENGTH_SHORT).show();
                intent.putExtra("페이지",0);
                startActivity(intent);
                //btn1.performClick(); // startButton을 강제로 누르도록 만듦


            } else if (message.startsWith("사이드")) {
                Toast.makeText(MainActivity.this, "사이드 메뉴 입니다.", Toast.LENGTH_SHORT).show();
                intent.putExtra("페이지",1);
                startActivity(intent);
                //btn2.performClick(); // startButton을 강제로 누르도록 만듦
            } else if (message.startsWith("음료")) {
                Toast.makeText(MainActivity.this, "음료 메뉴 입니다.", Toast.LENGTH_SHORT).show();
                btn3.performClick(); // startButton을 강제로 누르도록 만듦
            } else if (message.contains("장바구니")){
                mic_status = 1;
                intent.putExtra("페이지",3);
                startActivity(intent);

            }
            else {
                //Toast.makeText(MainActivity.this, "다시 시도해 주시기 바랍니다. ", Toast.LENGTH_SHORT).show();
            } //주문이 없다고 되었다면 메세지 받은것과 응답을 false로 message list에 추가
            messageList.add(new Message(message, false));
        } else {
            //Toast.makeText(MainActivity.this, "텍스트가 비어있습니다.", Toast.LENGTH_SHORT).show();
        }
    }





    // 한번 받은 말이 넘어가나 시험
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

    // initfirebase 를 먼저 사용해보자
    @SuppressLint("LongLogTag")
    private void selectFood(String response){
        int idx = 0;
        int idxs = response.indexOf("개");
        if(response.contains("종료합니다") || response.contains("넘어가겠습니다")){
            mic_status = 1;
            return;
        }
        else if(response.contains("을") && response.contains("개")){
            idx = response.indexOf("을");
            get_food_name = response.substring(0,idx);
            initFirebaseRealTimeDataBase(get_food_name);
            get_food_amount = Integer.parseInt(response.substring(idx+2,idxs));
            Log.i(TAG, "food_name, food_amount"+get_food_name+ get_food_amount);
            //addFoodorder(get_food_name,get_food_amount);
        }else if ( response.contains("를") && response.contains("개")){
            idx = response.indexOf("를");
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
// reference사용해보자
    private void initFirebaseRealTimeDataBase(String get_food_name){
        DatabaseReference reference = database.getReference("Menu");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DBdata data =  dataSnapshot.child(get_food_name).getValue(DBdata.class);
                Log.d("TAG ", "네번째 경우 start: "+ data);
                if(data == null){
                    speakResponse("다시 한번 말씀해주세요 ");

                }else{
                    per_price = data.per_price;
                }

                Log.d("TAG", "per_price 변경" + per_price);
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
        Log.i("TAG", "per_price확인 :" + per_price);
        Integer food_price = get_food_amount * per_price;
        Integer food_per_price = per_price;
        Toast.makeText(getApplicationContext(), get_food_name +" "+ get_food_amount+"개가 " + "장바구니에 저장", Toast.LENGTH_LONG).show();
        Order_info order_info = new Order_info(get_food_name, get_food_amount, food_per_price, food_price);

        mDatabase.child("order").child(get_food_name).setValue(order_info);


    }


}