package directory.client;

import directory.api.Company;
import directory.api.Data;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.dropwizard.setup.Environment;
import io.dropwizard.client.JerseyClientBuilder;

import directory.client.DirectoryClient;

public class DirectoryClient {
    private static final String NAME = "DirectoryClient";
    private static final String BASE_PATH = "companies";

    private Client client;
    private WebTarget target;

    public DirectoryClient(DirectoryClientConfiguration config, Environment environment) {
        JerseyClientBuilder builder = new JerseyClientBuilder(environment);

        client = builder.using(config.getJerseyClientConfiguration()).build(NAME);
        target = client.target(config.getUrl()).path(BASE_PATH);
    }

    // Utility method to check if a HTTP's response status is OK
    private boolean responseIsOk(Response response) {
        return response.getStatusInfo().equals(Response.Status.OK);
    }

    // Operations - Methods to call the remote http service
    public List<String> getCompanies() {
        Response response = target.request(MediaType.APPLICATION_JSON)
                                  .get();

        return responseIsOk(response) ? response.readEntity(new GenericType<List<String>>(){}) : null;
    }

    public Company getCompany(String company) {
        Response response = target.path(company)
                                  .request(MediaType.APPLICATION_JSON)
                                  .get();

        return responseIsOk(response) ? response.readEntity(Company.class) : null;
    } 

/*
    public boolean putCompany(String company) {
        Response response = target.path(company)
                                  .request(MediaType.APPLICATION_JSON)
                                  .put(Entity.json("")); // the name is in the path. Check if an empty entity is ok

        return responseIsOk(response);
    }
*/

    public String getExchange(String company) {
        Response response = target.path(company + "/exchange")
                                  .request(MediaType.APPLICATION_JSON)
                                  .get();

        return responseIsOk(response) ? response.readEntity(String.class) : null;
    }
/*
    public boolean putExchange(String company, String exchange) {
        String path = company + "/exchange/" + exchange;

        Response response = target.path(path)
                                  .request(MediaType.APPLICATION_JSON)
                                  .put(Entity.json(""));

        return responseIsOk(response);
    }
*/

    public Data getCurrentDayPrices(String company) {
        Response response = target.path(company + "/current")
                                  .request(MediaType.APPLICATION_JSON)
                                  .get();

        return responseIsOk(response) ? response.readEntity(Data.class) : null;
    }

    public boolean deleteCurrentDayPrices(String company) {
        Response response = target.path(company + "/current")
                                  .request(MediaType.APPLICATION_JSON)
                                  .delete();

        return responseIsOk(response);
    }

/*
    public boolean setCurrentDayPrices(String company, Data currentData) {
        Response response = target.path(company + "/current")
                                  .request(MediaType.APPLICATION_JSON)
                                  .put(Entity.json(currentData));

        return responseIsOk(response);
    }
*/

    public Double getCurrentDayOpenPrice(String company) {
        Response response = target.path(company + "/current/open")
                                  .request(MediaType.APPLICATION_JSON)
                                  .get();

        return responseIsOk(response) ? response.readEntity(Double.class) : null;
    }

    public Double getCurrentDayMinPrice(String company) {
        Response response = target.path(company + "/current/min")
                                  .request(MediaType.APPLICATION_JSON)
                                  .get();

        return responseIsOk(response) ? response.readEntity(Double.class) : null;
    }

    public Double getCurrentDayMaxPrice(String company) {
        Response response = target.path(company + "/current/max")
                                  .request(MediaType.APPLICATION_JSON)
                                  .get();

        return responseIsOk(response) ? response.readEntity(Double.class) : null;
    }

    public Double getCurrentDayClosePrice(String company) {
        Response response = target.path(company + "/current/close")
                                  .request(MediaType.APPLICATION_JSON)
                                  .get();

        return responseIsOk(response) ? response.readEntity(Double.class) : null;
    }

    public boolean setCurrentDayOpenPrice(String company, Double openPrice) {
        Response response = target.path(company + "/current/open")
                                  .request(MediaType.APPLICATION_JSON)
                                  .put(Entity.json(openPrice));

        return responseIsOk(response);
    }

    public boolean setCurrentDayMinPrice(String company, Double minPrice) {
        Response response = target.path(company + "/current/min")
                                  .request(MediaType.APPLICATION_JSON)
                                  .put(Entity.json(minPrice));

        return responseIsOk(response);
    }

    public boolean setCurrentDayMaxPrice(String company, Double maxPrice) {
        Response response = target.path(company + "/current/max")
                                  .request(MediaType.APPLICATION_JSON)
                                  .put(Entity.json(maxPrice));

        return responseIsOk(response);
    }

    public boolean setCurrentDayClosePrice(String company, Double closePrice) {
        Response response = target.path(company + "/current/close")
                                  .request(MediaType.APPLICATION_JSON)
                                  .put(Entity.json(closePrice));

        return responseIsOk(response);        
    }

    // Opening, min, max and closing price from previous day
    public Data getPreviousDayPrices(String company) {
        Response response = target.path(company + "/previous")
                                  .request(MediaType.APPLICATION_JSON)
                                  .get();

        return responseIsOk(response) ? response.readEntity(Data.class) : null;
    }

    public boolean setPreviousDayPrices(String company, Data previousData) {
        Response response = target.path(company + "/previous")
                                  .request(MediaType.APPLICATION_JSON)
                                  .put(Entity.json(previousData));

        return responseIsOk(response);
    }
}
