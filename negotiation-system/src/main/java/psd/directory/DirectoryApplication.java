package psd.directory;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.commons.io.FilenameUtils;
import psd.directory.core.Directory;
import psd.directory.resources.CompaniesResource;
import psd.directory.resources.CompanyResource;
import psd.directory.resources.CurrentDayPricesResource;
import psd.directory.resources.PreviousDayPricesResource;
import psd.exchange.ConfigFileReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, String> populate(List<String> exchangeConfigFiles) {
        Map<String, String> companyToExchange = new HashMap<>();

        for (String f : exchangeConfigFiles) {
            String exchange = FilenameUtils.getBaseName(f);
            List<String> companies = ConfigFileReader.getCompanies(f);

            for (String company : companies) {
                companyToExchange.put(company, exchange);
            }
        }
        return companyToExchange;
    }
}
