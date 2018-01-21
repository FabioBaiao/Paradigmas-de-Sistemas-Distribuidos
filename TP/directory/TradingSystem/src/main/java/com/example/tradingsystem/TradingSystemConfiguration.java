package com.example.tradingsystem;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class TradingSystemConfiguration extends Configuration{
    /*INFO: This should hold environment-specific parameters for the app
    * and it gets them from the trading-system.yml. What we could put in
    * this file is a list of the files where we store data */

    private int port;
    private int adminPort;

    @JsonProperty
    public int getPort(){return port;}
    @JsonProperty
    public int getAdminPort(){return adminPort;}
    @JsonProperty
    public void setPort(int port){this.port = port;}
    @JsonProperty
    public void setAdminPort(int adminPort){this.adminPort = adminPort;}
}
