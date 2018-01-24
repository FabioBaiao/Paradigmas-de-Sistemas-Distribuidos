package psd.exchange;

public final class Trade {
    public final String company;
    public final String seller;
    public final String buyer;
    public final int quantity;
    public final double unitPrice;

    public Trade(String company, String seller, String buyer, int quantity, double unitPrice) {
        this.company = company;
        this.seller = seller;
        this.buyer = buyer;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}
