package com.example.hearingkiosk2.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.airbnb.lottie.LottieAnimationView;
import com.example.hearingkiosk2.models.Message;
import com.example.hearingkiosk2.R;

import java.util.List;
// 챗봇 형태로 만드는거시당

// Recycler view 라는 곳에 우리가 만든 메세지를 붙이는 작업을 하는 곳 !
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private List<Message> messageList;
    private Activity activity;

    public ChatAdapter(List<Message> messageList, Activity activity) {
        this.messageList = messageList;
        this.activity = activity;
    }

    // view holder : 각 뷰를 보관하는 holder 객체이다.
    // 매번 findViewByid 를 호출 할때 성능저하가 일어나기 때문에 item View의 각요소를 바로 엑세스 할 수 있도록
    // 저장해두고 사용하기 위한 객체이다.

    @NonNull @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.adapter_message_one, parent, false);
        return new MyViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String message = messageList.get(position).getMessage(); // 이 뷰홀더에 메세지를 받는다.
        boolean isReceived = messageList.get(position).getIsReceived(); // 만약에 메세지를 받았을 경우를 true or false로 표현한다.
        if(isReceived){
            holder.messageReceive.setVisibility(View.VISIBLE); // 메세지 보이게 하고  : 여기서 receive 는 dialogflow로 만든대답
            holder.messageSend.setVisibility(View.GONE);// 메세지 안보이게함 : send는 사람이 말한거
            holder.animationView.setVisibility(View.VISIBLE);
            holder.messageReceive.setText(message);
          // 메세지 안보이게함
            holder.messageReceive.setText(message);
        }else {
            holder.animationView.setVisibility(View.GONE);
            holder.messageSend.setVisibility(View.VISIBLE); // 못받았을 경우에 상황
            holder.messageReceive.setVisibility(View.GONE);
            holder.messageSend.setText(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    } //size얻어서 나중에 리스트 길이 늘릴려고 사용

    public class MyViewHolder extends RecyclerView.ViewHolder{
        LottieAnimationView animationView;
        TextView messageSend;
        TextView messageReceive;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            animationView = itemView.findViewById(R.id.animationView);
            messageSend = itemView.findViewById(R.id.message_send); // 리사이클뷰에 보여줄 testview 두개 하나는 말하는거
            messageReceive = itemView.findViewById(R.id.message_receive); // 하나는 dialogflow에서 만든 대답
        }
    }
}
