package directory.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

public class CompanyList {
    private List<Company> companies;

    public CompanyList(){
    }

    public CompanyList(List<Company> companies){
        this.companies = companies;
    }

    @JsonProperty
    public List<String> getCompanies(){
        return companies.stream().map(c -> c.getName()).collect(Collectors.toList());
    }
}
