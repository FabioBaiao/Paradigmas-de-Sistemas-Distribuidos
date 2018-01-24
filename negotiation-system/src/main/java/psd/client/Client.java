package psd.client;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import org.zeromq.ZMQ;
import psd.client.ClientSerializer.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Client {

  public static void main(String[] args) throws IOException, UnknownHostException {

    String host = args[0];
    int port = Integer.parseInt(args[1]);

    Socket socket = new Socket(host, port);

    String zmqHost = args[2];
    int zmqPort = Integer.parseInt(args[3]);

    Subscriber subscriber = new Subscriber(zmqHost, zmqPort);
    Thread requester = new Thread(new Requester(socket.getOutputStream(), subscriber));
    Thread receiver = new Thread(new Receiver(socket.getInputStream()));

    requester.start();
    receiver.start();

    subscriber.run();
  }

  private static class Requester implements Runnable {

    CodedOutputStream out;
    Subscriber subscriber;

    public Requester(OutputStream out, Subscriber subscriber) {
      this.out = CodedOutputStream.newInstance(out);
      this.subscriber = subscriber;
    }

    public void run() {
      while(true) {
        System.out.print("> ");

        String input = System.console().readLine();
        String[] options = input.split(" ");

        switch(options[0]) {
          case "auth":
            // auth {username} {password}
            authenticate(options[1], options[2]);
            break;
          case "sell":
            // sell {company} {quantity} {minPrice}
            sell(options[1], Integer.parseInt(options[2]), Double.parseDouble(options[3]));
            break;
          case "buy":
            // buy {company} {quantity} {maxPrice}
            buy(options[1], Integer.parseInt(options[2]), Double.parseDouble(options[3]));
            break;
          case "logout":
            // logout
            logout();
            break;
          case "sub":
            //sub {company}
            subscribe(options[1]);
            break;
          case "unsub":
            // unsub {company}
            unsubscribe(options[1]);
            break;
          case "companies":
            // companies
            companies();
            break;
          case "company":
            // company {company}
            company(options[1]);
            break;
          case "exchange":
            // exchange {company}
            exchange(options[1]);
            break;
          case "current":
            // current {company}
            current(options[1]);
            break;
          case "previous":
            // previous {company}
            previous(options[1]);
            break;
          case "openprice":
            // openprice (current|previous) {company}
            openPrice(options[1], options[2]);
            break;
          case "closeprice":
            // closeprice (current|previous) {company}
            closePrice(options[1], options[2]);
            break;
          case "maximumprice":
            // maximumprice (current|previous) {company}
            maximumPrice(options[1], options[2]);
            break;
          case "minimumprice":
            // minimumprice (current|previous) {company}
            minimumPrice(options[1], options[2]);
            break;
        }
      }
    }

    private void authenticate(String username, String password) {
      AuthReq authReq =
        AuthReq.newBuilder()
          .setUsername(username)
          .setPassword(password)
          .build();

      Request req = Request.newBuilder().setAuth(authReq).build();

      sendRequest(req);
    }

    private void sell(String company, int quantity, double minPrice) {
      String exchange = getExchange(company);

      OrderReq ordReq = createOrder(exchange, company, quantity, minPrice, OrderReq.Type.SELL);
      Request req = Request.newBuilder().setOrder(ordReq).build();
      sendRequest(req);
    }

    private void buy(String company, int quantity, double maxPrice) {
      String exchange = getExchange(company);

      OrderReq ordReq = createOrder(exchange, company, quantity, maxPrice, OrderReq.Type.BUY);
      Request req = Request.newBuilder().setOrder(ordReq).build();
      sendRequest(req);
    }

    private void logout() {
      Logout logoutReq = Logout.newBuilder().build();
      Request req = Request.newBuilder().setLogout(logoutReq).build();
      sendRequest(req);
    }

    private void subscribe(String company) {
      System.out.print("\r");
      switch (subscriber.subscribe(company)) {
        case MAX_SUBS:
          System.out.println("Can't subscribe more companies");
          break;
        case SUBSCRIBED:
          System.out.println("Company subscribed");
          break;
        case EXISTS:
          System.out.println("Company already subscribed");
          break;
      }
    }

    private void unsubscribe(String company) {
      System.out.print("\r");
      switch (subscriber.unsubscribe(company)) {
        case UNSUBSCRIBED:
          System.out.println("Company unsubscribed");
          break;
        case DOESNT_EXISTS:
          System.out.println("Company subscription doesn't exists");
          break;
      }
    }

    private void companies() {
      // TO DO
    }

    private void company(String company) {
      // TO DO
    }

    private void exchange(String company) {
      // TO DO
    }

    private void current(String company) {
      // TO DO
    }

    private void previous(String company) {
      // TO DO
    }

    private void openPrice(String day, String company) {
      // TO DO
      switch (day) {
        case "current":
          break;
        case "previous":
          break;
      }
    }

    private void closePrice(String day, String company) {
      // TO DO
      switch (day) {
        case "current":
          break;
        case "previous":
          break;
      }
    }

    private void minimumPrice(String day, String company) {
      // TO DO
      switch (day) {
        case "current":
          break;
        case "previous":
          break;
      }
    }

    private void maximumPrice(String day, String company) {
      // TO DO
      switch (day) {
        case "current":
          break;
        case "previous":
          break;
      }
    }

    private OrderReq createOrder(String exchange, String company, int quantity, double unitPrice, OrderReq.Type type) {
      return OrderReq.newBuilder()
        .setExchange(exchange)
        .setCompany(company)
        .setQuantity(quantity)
        .setUnitPrice(unitPrice)
        .setType(type)
        .build();
    }

    private void sendRequest(Request req) {
      byte[] packet = req.toByteArray();
      try {
        out.writeFixed32NoTag(Integer.reverseBytes(packet.length));
  	    out.writeRawBytes(packet);
        out.flush();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

    private String getExchange(String company) {
      // TO DO
      return "NASDAQ";
    }

  }

  private static class Receiver implements Runnable {

    CodedInputStream in;

    public Receiver(InputStream in) {
      this.in = CodedInputStream.newInstance(in);
    }

    public void run() {
      while (true) {
        try{
          int len = Integer.reverseBytes(in.readFixed32());
          byte[] packet = in.readRawBytes(len);
          Reply rep = Reply.parseFrom(packet);

          System.out.print("\r");
          switch (rep.getMsgCase()) {
            case AUTH:
              authentication(rep.getAuth());
              break;
            case INVREQ:
              invalidRequest(rep.getInvReq());
              break;
            case LOGOUT:
              logout(rep.getLogout());
              break;
            case ORDER:
              order(rep.getOrder());
              break;
            case TRADESREP:
              trades(rep.getTradesRep().getTradesList());
              break;
            case ERROR:
              System.out.println(rep.getError());
              break;
          }
          System.out.print("> ");
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    private void authentication(AuthRep authRep) {
      switch(authRep.getStatus()){
				case LOGIN:
					System.out.println("Logged in");
					break;
				case REGISTER:
					System.out.println("Registered");
					break;
				case WRONG_PASSWORD:
					System.out.println("Wrong password");
					break;
			}
    }

    private void invalidRequest(InvalidRequest invReq) {
      System.out.println("Invalid request");
    }

    private void logout(Logout logout) {
      System.out.println("Logged out");
    }

    private void order(OrderRep order) {
      System.out.print("This ");
      switch (order.getType()) {
        case SELL:
          System.out.print("sell");
          break;
        case BUY:
          System.out.print("buy");
          break;
      }
      System.out.println(" order was placed:");
      System.out.print("Company: " + order.getCompany() + "; Quantity: " + order.getQuantity() + "; ");
      switch (order.getType()) {
        case SELL:
          System.out.print("Minimum");
          break;
        case BUY:
          System.out.print("Maximum");
          break;
      }
      System.out.println(" Price: " + order.getUnitPrice() + "; ");
      trades(order.getTradesList());
    }

    private void trades(List<Trade> trades) {
      switch(trades.size()) {
        case 0:
          System.out.println("NO TRADES!!");
          break;
        case 1:
          System.out.println("The following trade was negotiated: ");
          break;
        default:
          System.out.println("The following trades were negotiated: ");
          break;
      }
      for (Trade t : trades) {
        System.out.println("Seller: " + t.getSeller() + "; Buyer: " + t.getBuyer() +
          "; Company: " + t.getCompany() + "; Quantity: " + t.getQuantity() +
          "; Unit Price: " + t.getUnitPrice());
      }
    }
  }

  private static class Subscriber implements Runnable {

    Set<String> subscribed;
    ZMQ.Socket socket;

    Subscriber(String host, int port) {
      ZMQ.Context context = ZMQ.context(1);
      socket = context.socket(ZMQ.SUB);
      socket.connect("tcp://" + host + ":" + port);
      this.subscribed = new HashSet<>();
    }

    public void run() {
      while (true) {
        byte[] b = socket.recv();
        System.out.print("\r");
        System.out.println(new String(b));
        System.out.print("> ");
      }
    }

    public SubResult subscribe(String company) {
      if (subscribed.size() == 10) {
        return SubResult.MAX_SUBS;
      }
      else if (subscribed.add(company)) {
        socket.subscribe((company + ":").getBytes());
        return SubResult.SUBSCRIBED;
      }
      else {
        return SubResult.EXISTS;
      }
    }

    public enum SubResult {
      MAX_SUBS, SUBSCRIBED, EXISTS
    }

    public UnsubResult unsubscribe(String company) {
      if (subscribed.remove(company)) {
        socket.unsubscribe((company + ":").getBytes());
        return UnsubResult.UNSUBSCRIBED;
      }
      else {
        return UnsubResult.DOESNT_EXISTS;
      }
    }

    public enum UnsubResult {
      UNSUBSCRIBED, DOESNT_EXISTS
    }
  }
}
