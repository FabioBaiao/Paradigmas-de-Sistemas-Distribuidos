package exchange;

import exchange.BuyOrder;
import exchange.Trade;

import java.util.List;

public class BuyOrderResults {
    public final List<Trade> trades;
    public final BuyOrder remainingBuyOrder;

    /** 
     * Creates BuyOrderResults for a buy order that was completely fulfilled, 
     * producing a list of trades and no remaining buy order (i.e.: remainingBuyOrder = null).
     */
    public BuyOrderResults(List<Trade> trades) { this(trades, null); }

    /** Creates BuyOrderResults with the specified parameters. */
    public BuyOrderResults(List<Trade> trades, BuyOrder remainingBuyOrder) {
        this.trades = trades;
        this.remainingBuyOrder = remainingBuyOrder;
    }
}
