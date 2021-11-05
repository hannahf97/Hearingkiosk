package com.example.hearingkiosk2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.MediaRouteButton;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.View;

 import com.example.hearingkiosk2.BurgerActivity;
 import com.example.hearingkiosk2.CartActivity;
 import com.example.hearingkiosk2.ColaActivity;
 import com.example.hearingkiosk2.EndActivity;
 import com.example.hearingkiosk2.MainActivity;
 import com.example.hearingkiosk2.R;
 import com.example.hearingkiosk2.SideActivity;
 import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class MainActivity2 extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private BurgerActivity burgerActivity;
    private SideActivity sideActivity;
    private ColaActivity colaActivity;
    private CartActivity cartActivity;
    private LinearLayout orderButtonlayout;
    private Button moveButton;
    private Button orderCancelButton;
    SpeechRecognizer sttrec = SpeechRecognizer.createSpeechRecognizer(this); // stt
    int mic_status = 0; // 말할수있는지 상태 확인


    String message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        orderButtonlayout = findViewById(R.id.orderButtonlayout);
        orderButtonlayout.setVisibility(View.GONE);

        moveButton = findViewById(R.id.orderconfirm_button);
        orderCancelButton = findViewById(R.id.ordercancel_button);

        Integer page_num = getIntent().getIntExtra("페이지",0);
        Log.i("MainActivity2","현재 페이지는 "+ page_num);


        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, EndActivity.class);
                startActivityForResult(intent, 100);
            }

        });
        //취소버튼 누르면 DB 데이터 초기화
        orderCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("order").removeValue();
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivityForResult(intent, 100);
            }

        });

        bottomNavigationView=findViewById(R.id.bottomNavi);
        //네비게이션 버튼이 눌리면 -> 프레임을 씌운다
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.action_burger:
                    orderButtonlayout.setVisibility(View.GONE);
                    setFrag(0);
                    break;
                case R.id.action_side:
                    orderButtonlayout.setVisibility(View.GONE);
                    setFrag(1);
                    break;
                case R.id.action_cola:
                    orderButtonlayout.setVisibility(View.GONE);
                    setFrag(2);
                    break;
                case R.id.action_finish:
                    setFrag(3);
                    orderButtonlayout.setVisibility(View.VISIBLE);
                    break;



            }
            return true;
        });

       // Intent secondIntent = getIntent();

        burgerActivity=new BurgerActivity();
        sideActivity= new SideActivity();
        colaActivity=new ColaActivity();
        cartActivity=new CartActivity();


        switch (page_num){
            case 0:
                orderButtonlayout.setVisibility(View.GONE);
                bottomNavigationView.setSelectedItemId(R.id.action_burger);
                setFrag(0);
                break;
            case 1:
                setFrag(1);
                orderButtonlayout.setVisibility(View.GONE);
                bottomNavigationView.setSelectedItemId(R.id.action_side);
                break;

            case 2:
                setFrag(2);
                orderButtonlayout.setVisibility(View.GONE);
                bottomNavigationView.setSelectedItemId(R.id.action_cola);
                break;
            case 3:
                setFrag(3);
                orderButtonlayout.setVisibility(View.VISIBLE);
                bottomNavigationView.setSelectedItemId(R.id.action_finish);
                break;
        }


    }

    public void setFrag(int n){
        fm=getSupportFragmentManager();
        ft=fm.beginTransaction();
        switch (n){
            case 0:
                ft.replace(R.id.main_frame,burgerActivity);
                ft.commit();
                break;
            case 1:
                ft.replace(R.id.main_frame,sideActivity);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.main_frame,colaActivity);
                ft.commit();
                break;
            case 3:
                ft.replace(R.id.main_frame,cartActivity);
                ft.commit();
                break;

        }


    }
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


                    Log.d("onError", "error : " + message);
                    mic_status = 1;
                    sttrec.destroy();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result = (ArrayList<String>) results.get(sttrec.RESULTS_RECOGNITION);
                    Log.v("MainActivity2.java", "result: " + result.get(0));
                    message = result.get(0);
                    mic_status = 2;
                    onSpeakingCompleted(message);
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
            new CountDownTimer(3000, 1000) {
                public void onTick(long m) {
                }

                public void onFinish() {
                    sttrec.stopListening();
                }
            }.start();
        }
        catch (Exception e) {
            Log.e("MainActivity2.java", e.toString() +" 에러메세지 ");
//            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }





    private void onSpeakingCompleted(String message) {
    if (!message.isEmpty()) {
        if (message.contains("완료")) {
            moveButton.performClick(); // 다음단계 이동버튼 강제로 누르도록 만듦

        } else if (message.contains("취소")) {
            orderCancelButton.performClick(); // 주문 취소 강제로 누르도록 만듦

        }
    }


    }



}