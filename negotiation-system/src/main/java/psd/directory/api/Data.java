package psd.directory.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Data {
    private double openingPrice;
    private double closingPrice;
    private double minPrice;
    private double maxPrice;

    public Data(){
        this.openingPrice = -1.0;
        this.closingPrice = -1.0;
        this.minPrice = -1.0;
        this.maxPrice = -1.0;
    }

    @JsonProperty
    public synchronized void setOpeningPrice(double openingPrice){
        this.openingPrice = openingPrice;
    }

    @JsonProperty
    public synchronized double getOpeningPrice(){
        return openingPrice;
    }

    @JsonProperty
    public synchronized void setClosingPrice(double closingPrice){
        this.closingPrice = closingPrice;
    }

    @JsonProperty
    public synchronized double getClosingPrice(){
        return closingPrice;
    }

    @JsonProperty
    public synchronized void setMinPrice(double minPrice){
        if (this.minPrice == -1.0 || minPrice < this.minPrice)
            this.minPrice = minPrice;
        else
            System.out.println("Trying to update bigger minimum price");
    }

    @JsonProperty
    public synchronized double getMinPrice(){
        return minPrice;
    }

    @JsonProperty
    public synchronized void setMaxPrice(double maxPrice){
        if (this.maxPrice == -1.0 || maxPrice > this.maxPrice)
            this.maxPrice = maxPrice;
        else
            System.out.println("Trying to update smaller maximum price");
    }

    @JsonProperty
    public synchronized double getMaxPrice(){
        return maxPrice;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");

        sb.append("open = ").append((openingPrice == -1.0) ? "N/A" : openingPrice).append("; ");
        sb.append("close = ").append((closingPrice == -1.0) ? "N/A" : closingPrice).append("; ");
        sb.append("min = ").append((minPrice == -1.0) ? "N/A" : minPrice).append("; ");
        sb.append("max = ").append((maxPrice == -1.0) ? "N/A" : maxPrice).append(')');

        return sb.toString();
    }
}
