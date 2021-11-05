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
public class SideActivity extends Fragment{

    private View view;
    DatabaseReference mDatabase;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_side, container, false);

        Button side_button = (Button) v.findViewById(R.id.side_button);
        final EditText side_amount = (EditText) v.findViewById(R.id.side_amount);
        final EditText side_name = (EditText) v.findViewById(R.id.side_name);

        ImageButton salad = (ImageButton) v.findViewById(R.id.salad_menubutton);
        ImageButton burrito = (ImageButton) v.findViewById(R.id.burrito_menubutton);

        Button plus_button  = (Button) v.findViewById(R.id.plus_side_amount_button);
        Button minus_button  = (Button) v.findViewById(R.id.minus_side_amount_button);

        salad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                side_name.setText("");
                side_name.setText("샐러드");
                side_amount.setText("1");
            }
        });

        burrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                side_name.setText("");
                side_name.setText("부리또");
                side_amount.setText("1");
            }
        });

        plus_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Integer beverage_amount_before_cart = Integer.parseInt(side_amount.getText().toString());
                    Integer plus_amount = beverage_amount_before_cart + 1;
                    String plus_result = plus_amount.toString();
                    side_amount.setText(plus_result);
                }
                catch (Exception e){
                    Log.i("plus_button","plz select menu");
                }
            }
        });

        minus_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Integer beverage_amount_before_cart = Integer.parseInt(side_amount.getText().toString());
                    if (beverage_amount_before_cart == 1) {
                        beverage_amount_before_cart = 2;
                    }
                    Integer minus_amount = beverage_amount_before_cart - 1;
                    String minus_result = minus_amount.toString();
                    side_amount.setText(minus_result);
                }
                catch (Exception e){
                    Log.i("minus_button","plz select menu");
                }
            }
        });

        side_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer get_side_amount = Integer.parseInt(side_amount.getText().toString());
                String get_side_name = side_name.getText().toString();
                Integer get_side_price = get_price(get_side_name, get_side_amount);
                Integer get_side_per_price = get_side_price / get_side_amount;


                HashMap side_result = new HashMap<>();
                side_result.put("food_amount", get_side_amount);
                side_result.put("food_name", get_side_name);
                side_result.put("food_price", get_side_price);
                side_result.put("food_per_price", get_side_per_price);

                mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("order").push().setValue(side_result);
                Toast.makeText(getActivity(),"장바구니에 담겼습니다",Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }
    private int get_price(String get_burger_name, Integer get_side_amount) {
        if(get_burger_name == "샐러드"){
            return 3000 * get_side_amount;
        }
        else {
            return 2000 * get_side_amount;
        }
    }
}
