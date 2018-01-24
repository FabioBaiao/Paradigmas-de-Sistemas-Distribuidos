package psd.directory.core;

import psd.directory.api.Company;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Directory {

    private Map<String, Company> companies;

    public Directory() {
        this.companies = new HashMap<>();
    }

    public Directory(Map<String, String> companyToExchange) {
        companies = new HashMap<>((int) (companyToExchange.size() / .75f) + 1);
        companyToExchange.forEach((c,e) -> companies.put(c, new Company(c,e)));
    }

    public Collection<Company> values() {
        return companies.values();
    }

    public Company get(String company) {
        return companies.get(company);
    }

    public boolean containsKey(String company) { return companies.containsKey(company); }

    public void put(String name, Company company) {
        companies.put(name, company);
    }
}
