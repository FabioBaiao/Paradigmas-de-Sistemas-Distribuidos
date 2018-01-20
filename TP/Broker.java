import org.zeromq.ZMQ;

public class Broker {

  public static void main(String[] args) {
    ZMQ.Context context = ZMQ.context(1);
    ZMQ.Socket exchanges = context.socket(ZMQ.XSUB);
		ZMQ.Socket clients = context.socket(ZMQ.XPUB);

    int exchangesPort = Integer.parseInt(args[0]);
    int clientsPort = Integer.parseInt(args[1]);

    exchanges.bind("tcp://*:"+exchangesPort);
    clients.bind("tcp://*:"+clientsPort);

    ZMQ.proxy(exchanges, clients, null);
  }
}
