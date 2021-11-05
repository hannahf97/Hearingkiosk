package com.example.hearingkiosk2;


public class DBdata {
    Integer per_price;


    public DBdata(){}

    public DBdata(Integer per_price){
        this.per_price = per_price;
    }

    public Integer getPer_price(){
        return per_price;
    }

    public void setPer_price(Integer per_price){
        this.per_price = per_price;
    }

}
