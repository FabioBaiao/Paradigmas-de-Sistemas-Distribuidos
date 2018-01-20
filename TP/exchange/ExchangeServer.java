package exchange;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import org.zeromq.ZMQ;

import exchange.BuyOrder;
import exchange.Exchange;
import exchange.ExchangeClosedException;
import exchange.NoSuchCompanyException;
import exchange.SellOrder;

import exchange.ExchangeSerializer.ErrorMsg;
import exchange.ExchangeSerializer.Order;
import exchange.ExchangeSerializer.Reply;
import exchange.ExchangeSerializer.Request;
import exchange.ExchangeSerializer.TradeMsg;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.io.IOException;

import static exchange.Update.Type.*;

/**
 * TODO:
 *   - send opening, minimum, maximum and closing price updates to directory through
 *     a singleThreadExecutor used solely for contacting the directory
 *   - Resolve name conflict between the protobuff Trade and Exchange Trade OR 
 *     remove Exchange trade and always use protobuff Trade
 */

public class ExchangeServer {
    // Ports
//  private static final int SRV_PORT = 5555; // ServerSocket port
    private static final int PUB_PORT = 5556; // ZMQ pub socket port
    // Opening and closing time
    private static final LocalTime openingTime = LocalTime.of(9,0); // 9:00
    private static final LocalTime closingTime = LocalTime.of(17,0); // 17:00
    // Executors
    private static final ExecutorService pool = Executors.newFixedThreadPool(8);
    private static final ExecutorService singleThreadExec = Executors.newSingleThreadExecutor();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final Logger logger = Logger.getLogger(ExchangeServer.class.getName());

    public static void main(String[] args) throws IOException {      
        // Usage for testing (also applys to the case where the exchange registers itself in directory)
        if (args.length < 4) {
            System.err.println("Usage: ExchangeServer frontend_port xsub_port exchange_name company1 [company2 ...]");
            System.exit(1);
        }
/*
        // Usage for the exchange requesting its companies from the directory
        if (args.length != 2) {
            System.err.println("Usage: ExchangeServer port exchange_name");
            System.exit(1);
        }
*/

        Socket frontendConn = null;
        try (
             ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
             ZMQ.Context context = ZMQ.context(1);
             ZMQ.Socket pubSocket = context.socket(ZMQ.PUB)
             // open connection to directory???
        ) {
            logger.info("Server socket bound to " + Integer.parseInt(args[0]));
            final Exchange exchange = createExchange(args);

//          registerExchange(exchange);
//          scheduleOpenAndClose(exchange); // uncomment to schedule opening and closing
            exchange.open();

            pubSocket.connect("tcp://localhost:" + Integer.parseInt(args[1]));
            logger.info("Pub socket is bound to " + Integer.parseInt(args[1]));
            frontendConn = serverSocket.accept();
            CodedInputStream cis = CodedInputStream.newInstance(frontendConn.getInputStream());
            CodedOutputStream cos = CodedOutputStream.newInstance(frontendConn.getOutputStream());

            SendReplyTask.setCodedOutputStream(cos);
            PublishTask.setPubSocket(pubSocket);
            while (true) {
                Request req = readRequest(cis);
                pool.execute(new RequestProcessingTask(req, exchange));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
            cleanup();
            frontendConn.close();
            throw e;
        }
    }

    // TODO: After testing, get exchange companies from directory instead of reading from args!
    private static Exchange createExchange(String[] args) {
        String exchangeName = args[2]; // arg[0..1] are ports
        Set<String> companies = new HashSet<>((int) (args.length / .75f));

        for (int i = 3; i < args.length; i++)
            companies.add(args[i]);

        return new Exchange(exchangeName, companies, ExchangeServer::handleUpdate);
    }

/*
    private static void registerExchange(Exchange exchange) {
        logger.info("Registering exchange in directory");
    }
*/

    private static void scheduleOpenAndClose(Exchange exchange) {
        OpenExchangeTask openExchangeTask = new OpenExchangeTask(exchange);
        CloseExchangeTask closeExchangeTask = new CloseExchangeTask(exchange);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fstScheduledOpen = now.with(openingTime);
        LocalDateTime fstScheduledClose = now.with(closingTime);

        if (now.isAfter(fstScheduledOpen)) {
            fstScheduledOpen = fstScheduledOpen.plusDays(1);
            logger.info("It's past today's opening time");

            if (now.isBefore(fstScheduledClose)) { // open now, as we are between opening and closing time!
                logger.info("Still haven't reached closing time. Opening now!");
                scheduler.execute(openExchangeTask);
            } else {
                logger.info("It's past today's closing time. Will open tomorrow");
                fstScheduledClose = fstScheduledClose.plusDays(1);
            }
        }
        // getSeconds() shall give enough precision for our use case.
        // If more precision is required, we can use toMillis()
        long openDelay = Duration.between(now, fstScheduledOpen).getSeconds();
        long closeDelay = Duration.between(now, fstScheduledClose).getSeconds();

        scheduler.scheduleAtFixedRate(openExchangeTask, openDelay, 24 * 60 * 60, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(closeExchangeTask, closeDelay, 24 * 60 * 60, TimeUnit.SECONDS);

        logger.info("Scheduled first open for " + fstScheduledOpen);
        logger.info("Scheduled first close for " + fstScheduledClose);
    }

    private static Request readRequest(CodedInputStream cis) throws IOException {
        int len = Integer.reverseBytes(cis.readFixed32());
        byte[] ba = cis.readRawBytes(len);

        return Request.parseFrom(ba);
    }

    private static void handleUpdate(String company, Update update) {
        switch (update.type) {
            case OPENING_UNIT_PRICE:
                logger.info(company + " opening unit price is " + update.newValue + "€");
                // send opening, min and max price to directory
                // (the opening unit price is also the starting min and max price)
                break;
            case MAX_UNIT_PRICE:
                logger.info(company + " maximum unit price is now " + update.newValue + "€");
                // send new max unit price to directory
                break;
            case MIN_UNIT_PRICE:
                logger.info(company + " minimum unit price is now " + update.newValue + "€");
                // send new min unit price to directory
                break;
            case CLOSING_UNIT_PRICE:
                // NOTE: since Exchange.close returns the closing stats of all companies,
                //       it may be better to send them all in a single message.
        }
    }

    private static void cleanup() {
        pool.shutdownNow();
        singleThreadExec.shutdownNow();
        scheduler.shutdownNow();
    }

    //================================================================================
    // Tasks
    //================================================================================
    
    /* 
     * Task responsible for processing an order and submitting a PublishTask
     * (if any trades occurred) and a SendReplyTask to the single thread executor.
     */
    private static class RequestProcessingTask implements Runnable {
        private final Request request;
        private final Exchange exchange;

        public RequestProcessingTask(Request request, Exchange exchange) {
            this.request = request;
            this.exchange = exchange;
        }

        @Override
        public void run() {
            Order o = request.getOrder();

            switch (o.getType()) {
                case BUY:
                    buy(o.getUser(), o.getCompany(), o.getQuantity(), o.getUnitPrice());
                    break;
                case SELL:
                    sell(o.getUser(), o.getCompany(), o.getQuantity(), o.getUnitPrice());
                    break;
                default:
                    logger.severe("This is impossible!");
            }
        }

        private void buy(String buyer, String company, int quantity, double maxUnitPrice) {
            Reply reply;
            BuyOrderResults results = null;

            try {
                results = exchange.buy(buyer, company, quantity, maxUnitPrice);
                reply = buildReply(results);
                // TODO: After removing debug messages, use multi-catch to reduce clutter
            } catch (ExchangeClosedException e) {
                logger.info("Received request from " + buyer + " but the exchange is closed");
                reply = buildReply(buyer, e.getMessage());
            } catch (NoSuchCompanyException e) {
                logger.info("Company " + company + " requested by " + buyer + " isn't traded in this exchange");
                reply = buildReply(buyer, e.getMessage());
            }
            singleThreadExec.execute(new SendReplyTask(reply));
            logger.info("Submitted SendReplyTask");
            
            if (results != null && !results.trades.isEmpty()) {
                String notification = buildNotification(results.trades);
                singleThreadExec.execute(new PublishTask(notification));
                logger.info("Submitted PublishTask");
            }
        }

        private void sell(String seller, String company, int quantity, double minUnitPrice) {
            Reply reply;
            SellOrderResults results = null;

            try {
                results = exchange.sell(seller, company, quantity, minUnitPrice);
                reply = buildReply(results);
                // TODO: After removing debug messages, use multi-catch to reduce clutter
            } catch (ExchangeClosedException e) {
                logger.info("Received request from " + seller + " but the exchange is closed");
                reply = buildReply(seller, e.getMessage());
            } catch (NoSuchCompanyException e) {
                logger.info("Company " + company + " requested by " + seller + " isn't traded in this exchange");
                reply = buildReply(seller, e.getMessage());
            }
            singleThreadExec.execute(new SendReplyTask(reply));
            logger.info("Submitted SendReplyTask");

            if (results != null && !results.trades.isEmpty()) {
                String notification = buildNotification(results.trades);
                singleThreadExec.execute(new PublishTask(notification));
                logger.info("Submitted PublishTask");
            }
        }

        //--------------------------------------------------------------------------------
        // Protobuff message builders
        //--------------------------------------------------------------------------------

        private Reply buildReply(BuyOrderResults results) {
            Reply.Builder builder = Reply.newBuilder();
            BuyOrder remainingBuyOrder = results.remainingBuyOrder;
            
            if (remainingBuyOrder != null) {
                builder.setOrder(
                    Order.newBuilder()
                         .setUser(remainingBuyOrder.buyer)
                         .setCompany(remainingBuyOrder.company)
                         .setQuantity(remainingBuyOrder.quantity)
                         .setUnitPrice(remainingBuyOrder.maxUnitPrice)
                         .setType(Order.Type.BUY)
                         .build()
                );
            }
            for (Trade t : results.trades) // trades may be empty but never null
                builder.addTrades(buildTrade(t));
            
            return builder.build();
            
        }

        private Reply buildReply(SellOrderResults results) {
            Reply.Builder builder = Reply.newBuilder();
            SellOrder remainingSellOrder = results.remainingSellOrder;
            
            if (remainingSellOrder != null) {
                builder.setOrder(
                    Order.newBuilder()
                         .setUser(remainingSellOrder.seller)
                         .setCompany(remainingSellOrder.company)
                         .setQuantity(remainingSellOrder.quantity)
                         .setUnitPrice(remainingSellOrder.minUnitPrice)
                         .setType(Order.Type.SELL)
                         .build()
                );
            }
            for (Trade t : results.trades) // trades may be empty but never null
                builder.addTrades(buildTrade(t));
            
            return builder.build();
        }

        private TradeMsg buildTrade(Trade t) {
            return TradeMsg.newBuilder()
                           .setSeller(t.seller)
                           .setBuyer(t.buyer)
                           .setCompany(t.company)
                           .setQuantity(t.quantity)
                           .setUnitPrice(t.unitPrice)
                           .build();
        }

        private Reply buildReply(String user, String error) {
            return Reply.newBuilder()
                        .setError(
                            ErrorMsg.newBuilder()
                                    .setUser(user)
                                    .setError(error)
                                    .build()
                        ).build();
        }

        private String buildNotification(List<Trade> trades) {
            StringBuilder sb = new StringBuilder();

            for (Trade t : trades) {
                sb.append(t.company).append(':'); // all trades are from the same company
                
                sb.append(t.buyer).append(" bought ");
                sb.append(t.quantity).append(" shares from ");
                sb.append(t.seller).append(" for ");
                sb.append(String.format("%.2f€ each%n", t.unitPrice));
            }
            return sb.toString();
        }
    }

    /*
     * Task responsible sending a reply to a buy or sell order. There's only one connection to the
     * front-end server and, therefore, only one CodedOutputStream. To avoid sharing the output stream 
     * and causing concurrency problems, each new SendReplyTask MUST run inside a single thread executor.
     */
    private static class SendReplyTask implements Runnable {
        // Having the CodedOutputStream in a static variable allows each new Runnable to have access to it.
        // Running inside a single thread executor ensures that at any time, only one thread uses the stream.
        private static CodedOutputStream cos;
        private static final Logger logger = Logger.getLogger(SendReplyTask.class.getName());

        private final Reply rep;

        public SendReplyTask(Reply rep) { this.rep = rep; }

        public static void setCodedOutputStream(CodedOutputStream cos) { SendReplyTask.cos = cos; }

        @Override
        public void run() {
            byte[] ba = rep.toByteArray();

            try {
                SendReplyTask.cos.writeFixed32NoTag(Integer.reverseBytes(ba.length));
                SendReplyTask.cos.writeRawBytes(ba);
                SendReplyTask.cos.flush();
                logger.info("Sent reply");
            } catch (IOException e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
    }

    /*
     * Task responsible for publishing a notification through a ZMQ.Socket of type ZMQ.PUB.
     * There's only one PUB socket, so each new PublishTask MUST run inside a single thread executor
     * to avoid sharing the socket and causing concurrency problems.
     */
    private static class PublishTask implements Runnable {
        // pubSocket is a static variable for the same reasons as the CodedOutputStream in SendReplyTask is.
        private static ZMQ.Socket pubSocket;
        private static final Logger logger = Logger.getLogger(PublishTask.class.getName());

        private final String notification;

        public PublishTask(String notification) { this.notification = notification; }

        public static void setPubSocket(ZMQ.Socket pubSocket) { PublishTask.pubSocket = pubSocket; }
        
        @Override
        public void run() {
            PublishTask.pubSocket.send(notification);
            logger.info("Published\n" + notification);
        }
    }

    private static class OpenExchangeTask implements Runnable {
        private static final Logger logger = Logger.getLogger(OpenExchangeTask.class.getName());

        private final Exchange exchange;

        public OpenExchangeTask(Exchange exchange) { this.exchange = exchange; }

        @Override
        public void run() {
            logger.info("Opening the exchange");
            exchange.open();
        }
    }

    private static class CloseExchangeTask implements Runnable {
        private static final Logger logger = Logger.getLogger(CloseExchangeTask.class.getName());

        private final Exchange exchange;

        public CloseExchangeTask(Exchange exchange) { this.exchange = exchange; }

        @Override
        public void run() {
            logger.info("Closing the exchange");

            Map<String, ClosingStats> closingStats = exchange.close();

            logger.info("TODO: Send closing stats to directory server");
        }
    }
}
