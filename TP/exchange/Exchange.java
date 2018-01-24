package exchange;

import static exchange.Update.Type.*;

import exchange.BuyOrder;
import exchange.ClosingStats;
import exchange.ExchangeClosedException;
//import exchange.MinMaxStats;
import exchange.NoSuchCompanyException;
import exchange.SellOrder;
import exchange.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.function.BiConsumer;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Exchange {
    private static final Logger logger = Logger.getLogger(Exchange.class.getName());
    
    private volatile boolean open;
    private final String name;
    private final ConcurrentMap<String, CompanyOrders> orderMap;
    private BiConsumer<String, Update> updateHandler;

    public Exchange(String name, Set<String> companies) {
        this(name, companies, (company,update) -> {});
    }

    public Exchange(String name, Set<String> companies, BiConsumer<String, Update> updateHandler) {
        this.open = false;
        this.name = name;
        this.orderMap = new ConcurrentHashMap<>((int) (companies.size() / .75f) + 1); // avoid rehashing
        for (String c : companies)
            this.orderMap.put(c, new CompanyOrders(c));
        
        this.updateHandler = updateHandler;
        logger.info("Exchange created successfully");
    }

    /** Opens this exchange for trading. */
    public void open() {
        for (CompanyOrders o : orderMap.values()) {
            o.currentUnitPrice = CompanyOrders.UNDEFINED_PRICE;
            o.minUnitPrice = Double.MAX_VALUE;
            o.maxUnitPrice = Double.MIN_VALUE;
        }
        open = true;
        logger.info("Exchange now open");
    }

    /** Returns whether this exchange is closed or not. */
    public boolean isOpen() { return open; }

    /** Returns the name of this exchange. */
    public String getName() { return name; }

    // Should return immediately to avoid blocking the processing of more company orders
    public void setUpdateHandler(BiConsumer<String, Update> updateHandler) {
        this.updateHandler = updateHandler;
    }

    /**
     * Places a buy order for a given number of a shares of a company, at a price per unit not greater
     * than a provided max value. This method tries to match the buy order with a compatible sell order
     * having the lowest minimum price per share. A compatible sell order is one whose minimum price per
     * share is not greater than the buy order's maximum price per share. When a compatible sell order exists,
     * a trade is executed. The number of shares traded is the minimum of the quantities in the paired orders;
     * the price per share is the average of the paired orders' price per share. If after a trade the buy order
     * still has more shares left to buy, the next compatible sell order with the minimum unit price possible is
     * looked up. If it exists, a new trade is executed, and so on. Trading stops when the buy order is completely
     * fulfilled or when there are no more compatible sell orders. In the second case, a buy order for the
     * remaining number of shares not bought is registered. The return value of this method is a BuyOrderResults instance.
     *
     * @param buyer username of the client placing the buy order.
     * @param company name of the company whose shares are to be bought.
     * @param quantity number of shares to buy.
     * @param maxUnitPrice maximum price per share.
     * @return list of trades executed in response to the buy order.
     * @throws ExchangeClosedException if this exchange is currently closed.
     * @throws NoSuchCompanyExchange if the provided company is not traded in this exchange.
     * @see BuyOrderResults
     */
    public BuyOrderResults buy(String buyer, String company, int quantity, double maxUnitPrice)
        throws ExchangeClosedException, NoSuchCompanyException
    {
        CompanyOrders companyOrders = orderMap.get(company);

        if (companyOrders == null)
            throw new NoSuchCompanyException("Company " + company + " isn't traded in this exchange.");

        return companyOrders.buy(buyer, quantity, maxUnitPrice);
    }

    /**
     * Places a sell order for a given number of a shares of a company, at a price per unit not less
     * than a provided max value. This method tries to match the sell order with a compatible buy order
     * having the highest maximum price per share. A compatible buy order is one whose maximum price per
     * share is not less than the sell order's minimum price per share. When a compatible buy order exists,
     * a trade is executed. The number of shares traded is the minimum of the quantities in the paired orders;
     * the price per share is the average of the paired orders' price per share. If after a trade the sell order
     * still has more shares left to sell, the next compatible buy order with the greatest possible unit price is
     * looked up. If it exists, a new trade is executed, and so on. Trading stops when the sell order is completely
     * fulfilled or when there are no more compatible buy orders. In the second case, a sell order for the
     * remaining number of shares not sold is registered. The return value of this method is a SellOrderResults instance.
     *
     * @param seller username of the client placing the sell order.
     * @param company name of the company whose shares are to be sold.
     * @param quantity number of shares to buy.
     * @param minUnitPrice minimum price per share.
     * @return sell order results: list of trades executed in response to the sell order.
     * @throws ExchangeClosedException if this exchange is currently closed.
     * @throws NoSuchCompanyExchange if the provided company is not traded in this exchange.
     * @see SellOrderResults
     */
    public SellOrderResults sell(String seller, String company, int quantity, double minUnitPrice)
        throws ExchangeClosedException, NoSuchCompanyException
    {
        CompanyOrders companyOrders = orderMap.get(company);

        if (companyOrders == null)
            throw new NoSuchCompanyException("Company " + company + " isn't traded in this exchange.");

        return companyOrders.sell(seller, quantity, minUnitPrice);
    }

    /**
     * Closes this exchange and returns closing statistics of each company.
     * Companies that sold no shares since the opening of the exchange have null closing statistics.
     * @return map of company name to its closing statistics.
     * @see ClosingStats
     */
    public Map<String, ClosingStats> close() {
        open = false;
        logger.info("The exchange is now closed");

        Map<String, ClosingStats> res = new HashMap<>((int) (orderMap.size() / .75f) + 1);
        for (CompanyOrders o : orderMap.values())
            res.put(o.company, o.getClosingStats());
        
        logger.info("Returning closing stats");
        return res;
        // Pre-conditions for this method working correctly as it is:
        //   1. no new companies are added to the order map after exchange construction;
        //   2. methods that modify CompanyOrder's stats don't allow changes when the exchange 
        //      is closed and synchronize on the same mutex as method getClosingStats().
        //   3. open = false is done before trying to get any closing stats (this, together
        //      with 2. ensures that no new orders start while gathering closing stats
        //      and change those stats afterwards)
    }

    private void requireNotClosed() throws ExchangeClosedException {
        if (!open)
            throw new ExchangeClosedException("Exchange currently closed");
    }

    /**
     * This class represents the buy and sell orders for a company's stock, together with
     * statistics such as the opening, minimum, maximum and current price per share.
     */
    private class CompanyOrders {
        static final double UNDEFINED_PRICE = -1.0;

        double openingUnitPrice;
        double currentUnitPrice;
        double minUnitPrice, maxUnitPrice;
        final String company;
        final TreeSet<BuyOrder> buyOrders;
        final TreeSet<SellOrder> sellOrders;

        CompanyOrders(String company) {
            this.company = company;
            this.buyOrders = new TreeSet<>();
            this.sellOrders = new TreeSet<>();
        }

        synchronized BuyOrderResults buy(String buyer, int quantity, double maxUnitPrice)
            throws ExchangeClosedException
        {
            Exchange.this.requireNotClosed();

            BuyOrder b = new BuyOrder(company, buyer, quantity, maxUnitPrice);
            List<Trade> trades = new ArrayList<>();
            Iterator<SellOrder> it = sellOrders.iterator();

            logger.info("Processing " + b);
            while (it.hasNext() && b.quantity > 0) {
                SellOrder s = it.next();

                if (b.maxUnitPrice >= s.minUnitPrice) {
                    logger.info("Trading with " + s);

                    trades.add(trade(b,s)); // trade(b,s) updates b.quantity and s.quantity
                    if (s.quantity == 0) {
                        logger.info("Removing sell order");
                        it.remove();
                    }
                } else {
                    logger.info("No more compatible orders");
                    break;
                }
            }
            if (b.quantity > 0) {
                buyOrders.add(b);
                logger.info("Added " + b + " to buy orders");
                return new BuyOrderResults(trades, b.clone());
            } else {
                return new BuyOrderResults(trades);
            }
        }

        synchronized SellOrderResults sell(String seller, int quantity, double minUnitPrice)
            throws ExchangeClosedException
        {
            Exchange.this.requireNotClosed();

            List<Trade> trades = new ArrayList<>();
            SellOrder s = new SellOrder(company, seller, quantity, minUnitPrice);
            Iterator<BuyOrder> it = buyOrders.iterator();

            logger.info("Processing " + s);
            while (it.hasNext() && s.quantity > 0) {
                BuyOrder b = it.next();

                if (b.maxUnitPrice >= s.minUnitPrice) {
                    logger.info("Trading with " + b);

                    trades.add(trade(b,s)); // trade(b,s) updates b.quantity and s.quantity
                    if (b.quantity == 0) {
                        logger.info("Removing buy order");
                        it.remove();
                    }
                } else {
                    logger.info("No more compatible orders");
                    break;
                }
            }
            if (s.quantity > 0) {
                sellOrders.add(s);
                logger.info("Added " + s + " to sale orders");
                return new SellOrderResults(trades, s.clone());
            } else {
                return new SellOrderResults(trades);
            }
        }
/*
        synchronized MinMaxStats getMinMaxStats() {
            return (currentUnitPrice == UNDEFINED) ? null : new MinMaxStats(minUnitPrice, maxUnitPrice);
        }
*/
        synchronized ClosingStats getClosingStats() {
            if (currentUnitPrice == UNDEFINED_PRICE)
                return null;
            else
                return new ClosingStats(openingUnitPrice, currentUnitPrice, minUnitPrice, maxUnitPrice);
        }

        // Doesn't need synchronization because this is only called inside synchronized methods.
        private Trade trade(BuyOrder b, SellOrder s) {
            int quantity = Math.min(b.quantity, s.quantity);
            double unitPrice = 0.5 * (b.maxUnitPrice + s.minUnitPrice);

            b.quantity -= quantity;
            s.quantity -= quantity;

            if (currentUnitPrice == UNDEFINED_PRICE) { // first trade of the day
                openingUnitPrice = unitPrice;
                minUnitPrice = maxUnitPrice = unitPrice;
                Exchange.this.updateHandler.accept(company, new Update(OPENING_UNIT_PRICE, unitPrice));
            } else if (unitPrice < minUnitPrice) {
                minUnitPrice = unitPrice;
                Exchange.this.updateHandler.accept(company, new Update(MIN_UNIT_PRICE, unitPrice));
            } else if (unitPrice > maxUnitPrice) {
                maxUnitPrice = unitPrice;
                Exchange.this.updateHandler.accept(company, new Update(MAX_UNIT_PRICE, unitPrice));
            }
            currentUnitPrice = unitPrice;

            return new Trade(company, s.seller, b.buyer, quantity, unitPrice);
        }
    }
}
