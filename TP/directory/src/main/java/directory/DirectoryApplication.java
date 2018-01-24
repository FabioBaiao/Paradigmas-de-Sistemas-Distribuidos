package directory;

import directory.api.Company;
import directory.core.Directory;
import directory.resources.CompaniesResource;
import directory.resources.CompanyResource;
import directory.resources.CurrentDayPricesResource;
import directory.resources.PreviousDayPricesResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class DirectoryApplication extends Application<DirectoryConfiguration> {

    public static void main(final String[] args) throws Exception {
        new DirectoryApplication().run(args);
    }

    @Override
    public String getName() {
        return "Directory";
    }

    @Override
    public void initialize(final Bootstrap<DirectoryConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final DirectoryConfiguration configuration,
                    final Environment environment) {

        Directory directory = new Directory();
        populate(directory);

        final CompaniesResource companies = new CompaniesResource(directory);
        environment.jersey().register(companies);

        final CompanyResource company = new CompanyResource(directory);
        environment.jersey().register(company);

        final CurrentDayPricesResource current = new CurrentDayPricesResource(directory);
        environment.jersey().register(current);

        final PreviousDayPricesResource previous = new PreviousDayPricesResource(directory);
        environment.jersey().register(previous);
    }

    private void populate(Directory directory) {
        directory.put("CGD", new Company("CGD", "NASDAQ"));
    }

}
