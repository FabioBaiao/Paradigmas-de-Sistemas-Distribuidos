package psd.directory.client;

// Based on: https://github.com/bszeti/dropwizard-dwexample/blob/master/dwexample-client/src/main/java/bszeti/dw/example/client/ServiceClient.java

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import psd.directory.api.Company;
import psd.directory.api.CompanyList;
import psd.directory.api.Data;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.dropwizard.setup.Environment;
import io.dropwizard.client.JerseyClientBuilder;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

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

    public DirectoryClient(String url) {
    	ClientConfig clientConfig = new ClientConfig();
		
		// Connection settings
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(1, TimeUnit.HOURS); // Keep idle connection in pool
		connectionManager.setMaxTotal(1024); // Default is 20
		connectionManager.setDefaultMaxPerRoute(1024); // Default is 2 only, not OK for production use
		connectionManager.setValidateAfterInactivity(0); // Disable connection validation period (if it's closed on the server side). Might make sense with keepalive
		clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);
	
		// Socket/connection timeout
		// For additional details use connectionManager.setDefaultSocketConfig(SocketConfig.custom()...) 
		// and clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, RequestConfig.custom()...)
		clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 500);
		clientConfig.property(ClientProperties.READ_TIMEOUT, 500);
		
		// Use Apache Http client 
		ApacheConnectorProvider apacheConnectorProvider = new  ApacheConnectorProvider();
		clientConfig.connectorProvider(apacheConnectorProvider);
		
		// TODO: How to configure keep alive and connection reuse strategy with Jersey?
		// By default every connection is closed after the request which makes pooling not effective
		// The "Connection: keep-alive" header is added, but the client closes the connection afterwards
		
		// TODO: ExecutorService for async calls must be reviewed for Jersey 2.26
		// ThreadPoolExecutor with given core/max threadcount and an unbounded task queue 
		clientConfig.property(ClientProperties.ASYNC_THREADPOOL_SIZE, 5);
		
		// To disabled chunked encoding and use "Content-Length: ..." instead of "Transfer-Encoding: chunked"
		clientConfig.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED);
		
		// To Accept-Encoding: gzip,deflate (added by default if EncodingFilter is not used)
		clientConfig.register(GZipEncoder.class);
		clientConfig.register(DeflateEncoder.class);
		
		// To force gzip request encoding for POST
		clientConfig.register(EncodingFilter.class);
		clientConfig.property(ClientProperties.USE_ENCODING, "gzip");

		// ClientBuilder uses org.glassfish.jersey.client.JerseyClientBuilder
		client = ClientBuilder.newClient().register(JacksonJsonProvider.class);

		// Create webtarget
		target = client.target(url).path(BASE_PATH);
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
                                  .request(MediaType.APPLICATION_JSON_TYPE)
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

    public Double getPreviousDayOpenPrice(String company) {
        Response response = target.path(company + "/current/open")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        return responseIsOk(response) ? response.readEntity(Double.class) : null;
    }

    public Double getPreviousDayMinPrice(String company) {
        Response response = target.path(company + "/current/min")
                .request(MediaType.APPLICATION_JSON)
                .get();

        return responseIsOk(response) ? response.readEntity(Double.class) : null;
    }

    public Double getPreviousDayMaxPrice(String company) {
        Response response = target.path(company + "/current/max")
                .request(MediaType.APPLICATION_JSON)
                .get();

        return responseIsOk(response) ? response.readEntity(Double.class) : null;
    }

    public Double getPreviousDayClosePrice(String company) {
        Response response = target.path(company + "/current/close")
                .request(MediaType.APPLICATION_JSON)
                .get();

        return responseIsOk(response) ? response.readEntity(Double.class) : null;
    }
}
