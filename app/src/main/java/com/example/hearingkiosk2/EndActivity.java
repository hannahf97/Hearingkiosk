package com.example.hearingkiosk2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class EndActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ChildEventListener mChild;

    private ListView listView;
    private ArrayAdapter<String> adapter;
    List<Object> Array = new ArrayList<Object>();
    String speakinglist = "";

    private TextToSpeech _tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);


        listView = (ListView) findViewById(R.id.listview);


        initDatabase();
        _tts = new TextToSpeech(this, ttsInitListener);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        listView.setAdapter(adapter);


        Button initial_button = (Button) findViewById(R.id.initial_button);

        // 처음으로 버튼
        initial_button.setOnClickListener(v -> {
            Intent intent = new Intent(EndActivity.this, MainActivity.class);
            startActivity(intent);

        });


        //주문번호 + 합계금액
        mReference = mDatabase.getReference("order");
        mReference.addValueEventListener(new ValueEventListener() {
            int total_price = 0;
            int order_num = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();


                for (DataSnapshot messageData : snapshot.getChildren()) {

                    //총 합계금액
                    int price = messageData.child("food_price").getValue(Integer.class);
                    total_price = total_price + price;


                }

                //주문번호
                Random random = new Random();
                int order_num = random.nextInt(200);
                adapter.add(Integer.toString(order_num));


                String total = Integer.toString(total_price);
                adapter.add(total);

                adapter.notifyDataSetChanged();
                listView.setSelection(adapter.getCount() - 1);
                speakinglist = "주문 완료 ! , 대기 번호는 " + order_num + "번 이고," + "총 " + total_price + "원 입니다. 이용해주셔서 감사합니다. " ;
                handler();
                // speakResponse();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //     speak(speakinglist);

    }




    private void handler(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                speakResponse();
            }
        }, 600);
    }





    private void initDatabase() {

        mDatabase = FirebaseDatabase.getInstance();

        mReference = mDatabase.getReference("log");
        mReference.child("log").setValue("check");

        mChild = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mReference.addChildEventListener(mChild);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReference.removeEventListener(mChild);
    }


    private TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() //시작할떄 사용되는 listnenr
    {
        @Override
        public void onInit(int status) {
            if (status != TextToSpeech.SUCCESS)
                return;

            _tts.setOnUtteranceProgressListener(completedListener);

        }
    };
    private UtteranceProgressListener completedListener = new UtteranceProgressListener() {
        @SuppressLint("LongLogTag")
        @Override
        public void onStart(String utteranceId) {
            Log.i("EndActivity.java | UtteranceProgressListener", "|" + "new system TTS speak start" + "|");

        }

        @SuppressLint("LongLogTag")
        @Override
        public void onDone(String utteranceId) {

            Log.i("EndActivity.java | UtteranceProgressListener", "|" + "new system TTS speak complete" + "|");

        }

        @Override
        public void onError(String utteranceId) {

        }


    };

    @SuppressLint("LongLogTag")
    private void speakResponse() {
        Log.i("EndActivity.java | speak", "|System TTS speak|" + speakinglist + "|");
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "messageID");
        try {
            _tts.speak(speakinglist, TextToSpeech.QUEUE_FLUSH, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}