package com.example.hearingkiosk2;

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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.hearingkiosk2.models.Message;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//

public class CartActivity extends Fragment {

    private Button moveButton;
    private Button orderCancleButton;
    private View view;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ChildEventListener mChild;
    private TextToSpeech _tts;
    int mic_status = 0; // 말할수있는지 상태 확인
    private TextView message;

    private TableLayout tableLayout;
    private ArrayAdapter<String> adapter;
    List<Object> Array = new ArrayList<Object>(); //어댑터 나부랭이를 쓴다~
    String spakinglist = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_cart, container, false);
        TableLayout tableLayout = (TableLayout) view.findViewById(R.id.tableviewmsg);
        ArrayList<String> arrayList = new ArrayList<>();
        initDatabase();


        mReference = mDatabase.getReference("order");
        _tts = new TextToSpeech(getActivity(), ttsInitListener);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageData : dataSnapshot.getChildren()) {
                    TableRow tableRow = new TableRow(getContext()); //새로운 행 만듦
                    tableRow.setLayoutParams(new TableRow.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )); //행의 parameter설정

                    ArrayList<String> array = new ArrayList<>();
                    // get_food_name, get_food_amount, food_per_price, food_price, order_num
                    array.add(messageData.child("food_name").getValue(String.class));
                    //DBdata data =  dataSnapshot.child(get_food_name).getValue(DBdata.class);
                    array.add(messageData.child("food_per_price").getValue(Integer.class).toString());
                    array.add(messageData.child("food_amount").getValue(Integer.class).toString());
                    array.add(messageData.child("food_price").getValue(Integer.class).toString());
                    spakinglist += array.get(0) + array.get(2) + "개에 " + array.get(3) + "원 입니다.";
                    speakResponse();
                    for (int i = 0; i < array.size(); i++) {
                        TextView textView = new TextView(getContext());
                        textView.setText(array.get(i));
                        Log.i("TAG: value is ", array.get(i));
                        textView.setGravity(Gravity.CENTER);
                        textView.setTextSize(20);
                        tableRow.addView(textView);


                    }
                    tableLayout.addView(tableRow);

                }
            }


            @SuppressLint("LongLogTag")
            private void speakResponse() {

                Log.i("CartActivity.java | speak", "|System TTS speak|" + spakinglist + "|");
                HashMap<String, String> map = new HashMap<>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "messageID");
                try {
                    if (spakinglist.isEmpty()) {
                        _tts.speak("주문하신 내역이 없습니다."
                                , TextToSpeech.QUEUE_FLUSH, map);
                    } else {
                        _tts.speak(spakinglist + "주문 종료를 원하시면 완료를 외쳐주시고, 취소를 원하시면 취소라고 외쳐주세요"
                                , TextToSpeech.QUEUE_FLUSH, map);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    //데이터 불러오는 함수
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
    public void onDestroy() {
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

    public void setHandler(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((MainActivity2)getActivity()).inputVoice();
            }
        }, 30);
    }

    private UtteranceProgressListener completedListener = new UtteranceProgressListener() {
        @SuppressLint("LongLogTag")
        @Override
        public void onStart(String utteranceId) {
            Log.i(" UtteranceProgressListener", "|" + "new system TTS speak start" + "|");

        }

        @SuppressLint("LongLogTag")
        @Override
        public void onDone(String utteranceId) {

            Log.i("UtteranceProgressListener", "|" + "new system TTS speak complete" + "|");

            setHandler();


        }

        @Override
        public void onError(String utteranceId) {

        }
    };






}