package directory.core;

import directory.api.Company;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Directory {

    private Map<String, Company> companies;

    public Directory() {
        this.companies = new HashMap<>();
    }


    public Collection<Company> values() {
        return companies.values();
    }

    public Company get(String company) {
        return companies.get(company);
    }

    public boolean containsKey(String company) {
        return companies.containsKey(company);
    }

    public void put(String name, Company company) {
        companies.put(name, company);
    }
}
