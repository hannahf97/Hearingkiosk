package com.example.hearingkiosk2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class BurgerActivity extends Fragment {

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private View view;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_burger, container, false);

        ImageButton crab_burger = (ImageButton) v.findViewById(R.id.gsburger_menubutton);
        ImageButton pretty_burger = (ImageButton) v.findViewById(R.id.eburger_menubutton);
        ImageButton cheese_burger = (ImageButton) v.findViewById(R.id.cburger_menubutton);

        Button burger_button = (Button) v.findViewById(R.id.burger_button);
        final EditText burger_amount = (EditText) v.findViewById(R.id.burger_amount);
        final EditText burger_name = (EditText) v.findViewById(R.id.burger_name);

        Button plus_button  = (Button) v.findViewById(R.id.plus_burger_amount_button);
        Button minus_button  = (Button) v.findViewById(R.id.minus_burger_amount_button);




        crab_burger.setOnClickListener(v1 -> {
            burger_name.setText("");
            burger_name.setText("게살버거");
            burger_amount.setText("1");
        });

        pretty_burger.setOnClickListener(v12 -> {
            burger_name.setText("");
            burger_name.setText( "이쁜이버거");
            burger_amount.setText("1");
        });
        cheese_burger.setOnClickListener(v13 -> {
            burger_name.setText("");
            burger_name.setText("치즈버거");
            burger_amount.setText("1");
        });

        plus_button.setOnClickListener(v14 -> {
            try {
                Integer burger_amount_before_cart = Integer.parseInt(burger_amount.getText().toString());
                Integer plus_amount = burger_amount_before_cart + 1;
                String plus_result = plus_amount.toString();
                burger_amount.setText(plus_result);
            }
            catch(Exception e){
                Log.i("plus_button","plz select menu");
            }
        });



        minus_button.setOnClickListener(v15 -> {
            try {
                Integer burger_amount_before_cart = Integer.parseInt(burger_amount.getText().toString());
                if (burger_amount_before_cart == 1) {
                    burger_amount_before_cart = 2;
                }
                Integer minus_amount = burger_amount_before_cart - 1;
                String minus_result = minus_amount.toString();
                burger_amount.setText(minus_result);
            }
            catch (Exception e){
                Log.i("minus_button","plz select menu");
            }
        });

        burger_button.setOnClickListener(view -> {
            String get_burger_name = burger_name.getText().toString();
            Integer get_burger_amount = Integer.parseInt(burger_amount.getText().toString());
            Integer get_burger_price = get_price(get_burger_name, get_burger_amount);
            Integer get_burger_per_price = get_burger_price / get_burger_amount;


            HashMap<Object, Object> burger_result = new HashMap<>();
            burger_result.put("food_amount", get_burger_amount);
            burger_result.put("food_name", get_burger_name);
            burger_result.put("food_per_price", get_burger_per_price);
            burger_result.put("food_price", get_burger_price);


            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("order").push().setValue(burger_result);
            Toast.makeText(getActivity(),"장바구니에 담겼습니다",Toast.LENGTH_SHORT).show();


        }
        );
        return v;
    }


    private int get_price(String get_burger_name, int get_burger_amount) {
        if(get_burger_name == "게살버거"){
            return 3000 * get_burger_amount;
        }
        else if (get_burger_name == "이쁜이버거") {
            return 4000 * get_burger_amount;
        }
        else {
            return 3500 * get_burger_amount;
        }
    }
}