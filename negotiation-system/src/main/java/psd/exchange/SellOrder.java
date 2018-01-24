package psd.exchange;

import java.time.LocalDateTime;

// Note: this class has a natural ordering that is inconsistent with equals.
public class SellOrder implements Comparable<SellOrder> {
    int quantity;
    public final String company;
    public final String seller;
    public final double minUnitPrice;
    public final LocalDateTime dateTime;

    public SellOrder(String company, String seller, int quantity, double minUnitPrice) {
        this(company, seller, quantity, minUnitPrice, LocalDateTime.now());
    }

    public SellOrder(String company, String seller, int quantity,
                     double minUnitPrice, LocalDateTime dateTime)
    {
        this.company = company;
        this.quantity = quantity;
        this.seller = seller;
        this.minUnitPrice = minUnitPrice;
        this.dateTime = dateTime;
    }

    public SellOrder(SellOrder s) {
        this(s.company, s.seller, s.quantity, s.minUnitPrice, s.dateTime);
    }

    public SellOrder clone() { return new SellOrder(this); }

    // For debugging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SellOrder[");
        sb.append("company=").append(company);
        sb.append(", quantity=").append(quantity);
        sb.append(", seller=").append(seller);
        sb.append(", min=").append(minUnitPrice);
        sb.append(", dateTime=").append(dateTime).append("]");

        return sb.toString();
    }

    /**
     * 1. Ascending order by minUnitPrice;
     * 2. For equal minUnitPrices, ascending order by date and time;
     * 3. For equal date and time, lexicographical order by seller name.
     */
    @Override
    public int compareTo(SellOrder other) {
        if (minUnitPrice < other.minUnitPrice)
            return -1;
        if (minUnitPrice > other.minUnitPrice)
            return 1;

        int cmpDateTime = dateTime.compareTo(other.dateTime);
        return (cmpDateTime != 0) ? cmpDateTime : seller.compareTo(other.seller);
    }
}
