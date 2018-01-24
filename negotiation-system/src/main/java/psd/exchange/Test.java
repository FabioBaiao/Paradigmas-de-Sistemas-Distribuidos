package psd.exchange;

import psd.directory.api.Company;
import psd.directory.api.Data;
import psd.directory.client.DirectoryClient;

import java.util.List;

public class Test {

    public static void main(String[] args) {
        DirectoryClient client = new DirectoryClient("http://localhost:8080");

        List<String> cs = client.getCompanies();
        Company c = client.getCompany("Google");
        String e = client.getExchange("Google");
        Data curr = client.getCurrentDayPrices("Google");
        Double d = client.getCurrentDayClosePrice("Google");

        System.out.println((c == null) ? "Not working!" : cs);
        System.out.println((c == null) ? "Not working!" : c);
        System.out.println((e == null) ? "Not working!" : e);
        System.out.println((c == null) ? "Not working!" : curr);
        System.out.println((c == null) ? "Not working!" : d);
    }
}
