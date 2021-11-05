package com.example.hearingkiosk2;

public class Order_info {
    String food_name;
    Integer food_per_price;
    Integer food_price;
    Integer food_amount;

    public Order_info(){}

    public String getFood_name(){
        return food_name;
    }
    public void setFood_name(String food_name){
        this.food_name = food_name;
    }

    public Integer getFood_amount() {
        return food_amount;
    }

    public void setFood_amount(Integer food_amount) {
        this.food_amount = food_amount;
    }

    public Integer getFood_per_price() {
        return food_per_price;
    }

    public void setFood_per_price(Integer food_per_price) {
        this.food_per_price = food_per_price;
    }

    public Integer getFood_price() {
        return food_price;
    }

    public void setFood_price(Integer food_price) {
        this.food_price = food_price;
    }


    public Order_info(String food_name, Integer food_amount, Integer food_per_price, Integer food_price){
        this.food_name = food_name;
        this.food_amount = food_amount;
        this.food_per_price =food_per_price;
        this.food_price =food_price;
    }
}
