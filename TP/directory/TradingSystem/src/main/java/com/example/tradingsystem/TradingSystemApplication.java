package com.example.tradingsystem;

import com.example.tradingsystem.api.Company;
import com.example.tradingsystem.resources.CompaniesResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.ArrayList;

public class TradingSystemApplication extends Application<TradingSystemConfiguration> {
    public static void main(String[] args) throws Exception{
        new TradingSystemApplication().run(args);
    }

    public String getName(){
        return "trading-system_1.0";
    }

    public void initialize(Bootstrap<TradingSystemConfiguration> bootstrap){
        // nada a fazer por agora
    }

    public void run(TradingSystemConfiguration tradingSystemConfiguration, Environment environment) throws Exception {
        final CompaniesResource companiesResource = new CompaniesResource(new ArrayList<Company>());
        environment.jersey().register(companiesResource);
    }
}
