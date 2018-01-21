package com.example.tradingsystem.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Company {
    private String name;
    private String exchange;
    private Data previousDay;
    private Data currentDay;

    public Company(){
    }

    public Company(String name){
        this.name = name;
    }

    @JsonProperty
    public String getName(){
        return name;
    }

    @JsonProperty
    public String getExchange() { return exchange; }

    @JsonProperty
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    @JsonProperty
    public void setPreviousDay(Data previousDay){
        this.previousDay = previousDay;
    }

    @JsonProperty
    public Data getPreviousDay(){
        return previousDay;
    }

    @JsonProperty
    public void setCurrentDay(Data currentDay){
        this.currentDay = currentDay;
    }

    @JsonProperty
    public Data getCurrentDay(){
        return currentDay;
    }
}
