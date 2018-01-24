package psd.exchange;

import java.time.LocalDateTime;

// Note: this class has a natural ordering that is inconsistent with equals.
public class BuyOrder implements Comparable<BuyOrder> {
    int quantity;
    public final String company;
    public final String buyer;
    public final double maxUnitPrice;
    public final LocalDateTime dateTime;
    
    public BuyOrder(String company, String buyer, int quantity, double maxUnitPrice) {
        this(company, buyer, quantity, maxUnitPrice, LocalDateTime.now());
    }

    public BuyOrder(String company, String buyer, int quantity,
                    double maxUnitPrice, LocalDateTime dateTime)
    {
        this.company = company;
        this.quantity = quantity;
        this.buyer = buyer;
        this.maxUnitPrice = maxUnitPrice;
        this.dateTime = dateTime;
    }

    public BuyOrder(BuyOrder b) {
        this(b.company, b.buyer, b.quantity, b.maxUnitPrice, b.dateTime);
    }

    public BuyOrder clone() { return new BuyOrder(this); }

    // For debugging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BuyOrder[");
        sb.append("company=").append(company);
        sb.append(", quantity=").append(quantity);
        sb.append(", buyer=").append(buyer);
        sb.append(", max=").append(maxUnitPrice);
        sb.append(", dateTime=").append(dateTime).append("]");

        return sb.toString();
    }

    /**
     * 1. Descending order by maxUnitPrice;
     * 2. For equal maxUnitPrices, chronological order;
     * 3. For equal date and time, lexicographical order by buyer name.
     */
    @Override
    public int compareTo(BuyOrder other) {
        if (maxUnitPrice < other.maxUnitPrice)
            return 1;
        if (maxUnitPrice > other.maxUnitPrice)
            return -1;

        int cmpDateTime = dateTime.compareTo(other.dateTime);
        return (cmpDateTime != 0) ? cmpDateTime : buyer.compareTo(other.buyer);
    }
}
