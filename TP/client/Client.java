package client;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import client.ClientSerializer.*;

public class Client {

  public static void main(String[] args) throws IOException, UnknownHostException {

    String host = args[0];
    int port = Integer.parseInt(args[1]);

    Socket socket = new Socket(host, port);

    Thread requester = new Thread(new Requester(socket.getOutputStream()));
    Thread receiver = new Thread(new Receiver(socket.getInputStream()));

    requester.start();
    receiver.start();
  }

  private static class Requester implements Runnable {

    CodedOutputStream out;

    public Requester(OutputStream out) {
      this.out = CodedOutputStream.newInstance(out);
    }

    public void run() {
      while(true) {
        System.out.print("> ");

        String input = System.console().readLine();
        String[] options = input.split(" ");

        switch(options[0]) {
          case "auth":
            // auth username password
            authenticate(options[1], options[2]);
            break;
          case "sell":
            // sell company quantity minPrice
            sell(options[1], Integer.parseInt(options[2]), Double.parseDouble(options[3]));
            break;
          case "buy":
            // buy company quantity maxPrice
            buy(options[1], Integer.parseInt(options[2]), Double.parseDouble(options[3]));
            break;
          case "logout":
            // logout
            logout();
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
}
