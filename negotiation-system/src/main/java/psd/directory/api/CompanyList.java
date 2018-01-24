package psd.directory.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

public class CompanyList {
    private List<String> companies;

    public CompanyList() {
    }

    public CompanyList(List<String> companies){
        this.companies = companies;
    }

    @JsonProperty
    public List<String> getCompanies(){ return companies; }

    @JsonProperty
    public void setCompanies(List<String> companies) { this.companies = companies; }
}
