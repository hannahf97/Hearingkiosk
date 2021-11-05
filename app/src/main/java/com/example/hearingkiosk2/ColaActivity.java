package com.example.hearingkiosk2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
public class ColaActivity extends Fragment{
    DatabaseReference mDatabase;
    private View view;
    String receive;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_cola, container, false);

        Button beverage_button = (Button) v.findViewById(R.id.beverage_button);
        final EditText beverage_amount = (EditText) v.findViewById(R.id.beverage_amount);
        final EditText beverage_name = (EditText) v.findViewById(R.id.beverage_name);

        ImageButton cola = (ImageButton) v.findViewById(R.id.cola_menubutton);
        ImageButton cider = (ImageButton) v.findViewById(R.id.cider_menubutton);

        Button plus_button  = (Button) v.findViewById(R.id.plus_beverage_amount_button);
        Button minus_button  = (Button) v.findViewById(R.id.minus_beverage_amount_button);


        cola.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beverage_name.setText("");
                beverage_name.setText("콜라");
                beverage_amount.setText("1");
            }
        });

        cider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beverage_name.setText("");

                beverage_name.setText("사이다");
                beverage_amount.setText("1");
            }
        });

        plus_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Integer beverage_amount_before_cart = Integer.parseInt(beverage_amount.getText().toString());
                    Integer plus_amount = beverage_amount_before_cart + 1;
                    String plus_result = plus_amount.toString();
                    beverage_amount.setText(plus_result);
                }
                catch(Exception e){
                    Log.i("plus_button","plz select menu");
                }
            }
        });

        minus_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Integer beverage_amount_before_cart = Integer.parseInt(beverage_amount.getText().toString());
                    if (beverage_amount_before_cart == 1) {
                        beverage_amount_before_cart = 2;
                    }
                    Integer minus_amount = beverage_amount_before_cart - 1;
                    String minus_result = minus_amount.toString();
                    beverage_amount.setText(minus_result);
                }
                catch(Exception e){
                    Log.i("minus_button","plz select menu");
                }
            }
        });

        beverage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer get_beverage_amount = Integer.parseInt(beverage_amount.getText().toString());
                String get_beverage_name = beverage_name.getText().toString();
                Integer get_beverage_price = get_price(get_beverage_name, get_beverage_amount);
                Integer get_beverage_per_price = get_beverage_price / get_beverage_amount;


                HashMap beverage_result = new HashMap<>();
                beverage_result.put("food_amount", get_beverage_amount);
                beverage_result.put("food_name", get_beverage_name);
                beverage_result.put("food_price", get_beverage_price);
                beverage_result.put("food_per_price", get_beverage_per_price);



                mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("order").push().setValue(beverage_result);
                Toast.makeText(getActivity(),"장바구니에 담겼습니다",Toast.LENGTH_SHORT).show();


            }
        });
        return v;
    }
    private int get_price(String get_beverage_name, int get_beverage_amount) {
        if(get_beverage_name.equals( "사이다")){
            return 1000 * get_beverage_amount;
        }
        else {
            return 1000 * get_beverage_amount;
        }
    }
}