package psd.directory;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import psd.directory.core.Directory;
import psd.directory.resources.CompaniesResource;
import psd.directory.resources.CompanyResource;
import psd.directory.resources.CurrentDayPricesResource;
import psd.directory.resources.PreviousDayPricesResource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DirectoryApplication extends Application<DirectoryConfiguration> {

    public static void main(final String[] args) throws Exception {
        new DirectoryApplication().run(args);
    }

    @Override
    public String getName() {
        return "NegotiationSystem";
    }

    @Override
    public void initialize(final Bootstrap<DirectoryConfiguration> bootstrap) {}

    @Override
    public void run(final DirectoryConfiguration configuration,
                    final Environment environment) {
        List<String> exchangeConfigFiles = configuration.getExchangeConfigFiles();

        Map<String, String> companiesToExchange = populate(exchangeConfigFiles);

        Directory d = new Directory(companiesToExchange);

        environment.jersey().register(new CompaniesResource(d));
        environment.jersey().register(new CompanyResource(d));
        environment.jersey().register(new CurrentDayPricesResource(d));
        environment.jersey().register(new PreviousDayPricesResource(d));
    }

    private Map<String, String> populate(List<String> exchanges) {
        Map<String, String> companiesToExchange = new HashMap<>();
        for (String exchange : exchanges) {
            List<String> companies = getCompanies(exchange + ".txt");
            for (String company : companies) {
                companiesToExchange.put(company, exchange);
            }
        }
        return companiesToExchange;
    }

    private List<String> getCompanies(String file) {
        List<String> companies = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = br.readLine()) != null) {
                companies.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return companies;
    }
}
