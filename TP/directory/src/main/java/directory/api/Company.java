package directory.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Company {
    private String name;
    private String exchange;
    private Data previousDay;
    private Data currentDay;

    public Company(){
    }

    public Company(String name, String exchange){
        this.name = name;
        this.exchange = exchange;
        this.previousDay = new Data();
        this.currentDay = new Data();
    }

    @JsonProperty
    public String getName(){
        return name;
    }

    @JsonProperty
    public String getExchange() { return exchange; }

    /*
    NOT USED

    @JsonProperty
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }*/

    @JsonProperty
    public synchronized void setPreviousDay(Data previousDay){
        this.previousDay = previousDay;
    }

    @JsonProperty
    public synchronized Data getPreviousDay(){
        return previousDay;
    }

    @JsonProperty
    public synchronized void setCurrentDay(Data currentDay){
        this.currentDay = currentDay;
    }

    @JsonProperty
    public synchronized Data getCurrentDay(){
        return currentDay;
    }
}
