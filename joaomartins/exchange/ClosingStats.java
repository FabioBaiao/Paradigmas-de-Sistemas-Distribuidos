package exchange;

public class ClosingStats {
    public final double openingPrice;
    public final double closingPrice;
    public final double minPrice;
    public final double maxPrice;

    public ClosingStats(double openingPrice, double closingPrice, double minPrice, double maxPrice) {
        this.openingPrice = openingPrice;
        this.closingPrice = closingPrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}
