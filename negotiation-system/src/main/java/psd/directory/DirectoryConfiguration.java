package psd.directory;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

public class DirectoryConfiguration extends Configuration {
    @NotEmpty
    private List<String> exchangeConfigFiles;

    @JsonProperty
    public List<String> getExchangeConfigFiles() { return exchangeConfigFiles; }

    @JsonProperty
    public void setExchangeConfigFiles(List<String> exchangeConfigFiles) {
        this.exchangeConfigFiles = exchangeConfigFiles;
    }
}
