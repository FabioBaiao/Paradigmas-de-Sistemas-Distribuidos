package client;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

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

    OutputStream out;

    public Requester(OutputStream out) {
      this.out = out;
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

      Request req = Request.newBuilder().setAuthReq(authReq).build();

      sendRequest(req);
    }

    private void sell(String company, int quantity, double minPrice) {
      String exchange = getExchange(company);

      OrderReq ordReq = createOrder(exchange, company, quantity, minPrice, OrderReq.Type.SELL);
      Request req = Request.newBuilder().setOrdReq(ordReq).build();
      sendRequest(req);
    }

    private void buy(String company, int quantity, double maxPrice) {
      String exchange = getExchange(company);

      OrderReq ordReq = createOrder(exchange, company, quantity, maxPrice, OrderReq.Type.BUY);
      Request req = Request.newBuilder().setOrdReq(ordReq).build();
      sendRequest(req);
    }

    private void logout() {
      Logout logoutReq = Logout.newBuilder().build();
      Request req = Request.newBuilder().setLogoutReq(logoutReq).build();
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
      Integer size = req.getSerializedSize();

      try{
        out.write(size.byteValue());
        req.writeTo(out);
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

    InputStream in;

    public Receiver(InputStream in) {
      this.in = in;
    }

    public void run() {
      while (true) {
        try{
          int size = in.read();
          byte[] packet = new byte[size];
          in.read(packet, 0, size);
          Reply rep = Reply.parseFrom(packet);

          System.out.print("\r");
          switch (rep.getMsgCase()) {
            case AUTHREP:
              authentication(rep.getAuthRep());
              break;
            case INVREQ:
              invalidRequest(rep.getInvReq());
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
  }
}
