package psd.directory;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import psd.directory.core.Directory;
import psd.directory.resources.CompaniesResource;
import psd.directory.resources.CompanyResource;
import psd.directory.resources.CurrentDayPricesResource;

import java.util.HashSet;
import java.util.Set;

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
        Set<String> companyNames = new HashSet<>();

        companyNames.add("Facebook");
        companyNames.add("Google");
        Directory d = new Directory(companyNames);
        CompaniesResource companiesResource = new CompaniesResource(d);

        environment.jersey().register(companiesResource);
        environment.jersey().register(new CompanyResource(d));
        environment.jersey().register(new CurrentDayPricesResource(d));
    }
}
