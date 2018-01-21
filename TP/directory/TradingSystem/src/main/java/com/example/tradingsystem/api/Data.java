package com.example.tradingsystem.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Data {
    private float openingPrice;
    private float closingPrice;
    private float minPrice;
    private float maxPrice;

    public Data(){}

    @JsonProperty
    public void setOpeningPrice(float openingPrice){
        this.openingPrice = openingPrice;
    }
    @JsonProperty
    public float getOpeningPrice(){
        return openingPrice;
    }
    @JsonProperty
    public void setClosingPrice(float closingPrice){
        this.closingPrice = closingPrice;
    }
    @JsonProperty
    public float getClosingPrice(){
        return closingPrice;
    }
    @JsonProperty
    public void setMinPrice(float minPrice){
        this.minPrice = minPrice;
    }
    @JsonProperty
    public float getMinPrice(){
        return minPrice;
    }
    @JsonProperty
    public void setMaxPrice(float maxPrice){
        this.maxPrice = maxPrice;
    }
    @JsonProperty
    public float getMaxPrice(){
        return maxPrice;
    }
}
