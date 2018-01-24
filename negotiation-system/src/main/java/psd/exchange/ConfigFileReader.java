package psd.exchange;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigFileReader {

    public static List<String> getCompanies(String fileName) {
        List<String> companies = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                line = line.toLowerCase().replaceAll(" +", "-");
                companies.add(line);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return companies;
    }
}
