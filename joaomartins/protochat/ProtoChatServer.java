/**
 * Cliente poder saber:
 *     - nº de clientes autenticados;
 *     - lista de clientes logged in.
 */
package protochat;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import protochat.ChatMessages.PreLoginRequest;
import protochat.ChatMessages.PreLoginResponse;
import protochat.ChatMessages.ChatMessage;
import protochat.ChatMessages.ChatResponse;

import com.google.protobuf.InvalidProtocolBufferException;

public class ProtoChatServer {

	public static final int DEFAULT_PORT = 5555;
	
	private final ServerSocket serverSocket;
	private final ConcurrentMap<String,String> users;
	private final Set<String> loggedIn;
	private final MessageList msgs;

	public ProtoChatServer(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
		this.users = new ConcurrentHashMap<>();
		this.loggedIn = ConcurrentHashMap.newKeySet();
		this.msgs = new MessageList();
	}

	public static void main(String[] args) throws IOException {
		int port = DEFAULT_PORT;

		if (args.length == 1) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		} else if (args.length > 1) {
			System.err.println("Usage: ProtoChatServer [port]");
			System.exit(1);
		}

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			ProtoChatServer chatServer = new ProtoChatServer(serverSocket);
			System.out.println("Listening at port " + port);
			
			chatServer.acceptLoop();
		} 
	}

	public void acceptLoop() {
		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				LoginTask loginTask = new LoginTask(clientSocket, this);

				(new Thread(loginTask)).start();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				// NOTE: Consider rethrowing if exception was thrown on 1st accept.
			}
		}
	}

	//============================================================
	// Methods used by LoginTask and ClientWriteTask
	//============================================================

	private boolean register(String username, String password) {
		return users.putIfAbsent(username, password) == null;
	}

	// In case of success, returns the number of next message to be read; othwerise, returns -1.
	private int authenticate(String username, String password) {
		String p = users.get(username);

		if (p != null && p.equals(password)) {
			loggedIn.add(username);
			return msgs.newClient();
		} else {
			return -1;
		}
	}

	private void addMessage(String msg) {
		msgs.add(msg);
	}

	private String getMessage(int nextMsg) throws InterruptedException {
		return msgs.get(nextMsg);
	}

	private void logout(String username, int nextMsg) {
		loggedIn.remove(username);
		if (nextMsg >= 0)
			msgs.leave(nextMsg);
	}

	//============================================================
	// Tasks
	//============================================================

	private static class LoginTask implements Runnable {

		private String username;
		private final InputStream is;
		private final OutputStream os;
		private final Socket clientSocket;
		private final ProtoChatServer chatServer;

		public LoginTask(Socket clientSocket, ProtoChatServer chatServer)
			throws IOException
		{
			this.is = clientSocket.getInputStream();
			this.os = clientSocket.getOutputStream();
			this.clientSocket = clientSocket;
			this.chatServer = chatServer;
			this.username = null;
		}

		@Override
		public void run() {
			int nextMsg = -1;
			boolean cont = true; // continue: true unless client sends EOF.
			PreLoginRequest plr;

			try {
				while (cont && (plr = PreLoginRequest.parseDelimitedFrom(is)) != null) {
					String username = plr.getUsername();
					String password = plr.getPassword();

					switch (plr.getRequestType()) {
						case REGISTRATION:
							if (chatServer.register(username, password))
								sendPreLoginResponse(true, "");
							else
								sendPreLoginResponse(false, "Username already exists.");
							break;
						case AUTHENTICATION:
							nextMsg = chatServer.authenticate(username, password);
							
							if (nextMsg >= 0) { // successful login
								this.username = username;
								sendPreLoginResponse(true, "");
								cont = loggedInLoop(nextMsg);
								this.username = null;
							} else {
								sendPreLoginResponse(false, "Invalid username and/or password.");
							}
							break;
					}
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
				if (this.username != null)
					chatServer.logout(this.username, nextMsg);
			}

			try {
				// ClientWriteTask may have closed the socket
				// to get LoginTask out of blocking Socket read().
				if (!clientSocket.isClosed())
					clientSocket.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}

		private void sendPreLoginResponse(boolean authorized, String msg) throws IOException {
			PreLoginResponse.newBuilder()
							.setAuthorized(authorized)
							.setMessage(msg)
							.build()
							.writeDelimitedTo(os);
			os.flush();
		}

		// Returns true if client logged out through LOGOUT command; false if otherwise.
		// (this.run() will only continue reading from client if loggedInLoop() returns true)
		private boolean loggedInLoop(int nextMsg) {
			Thread clientWriteThread = new Thread(new ClientWriteTask(username, nextMsg, os, chatServer));
			clientWriteThread.start();

			ChatMessage clientMsg;
			boolean logout = false;
			try {
				while (!logout && (clientMsg = ChatMessage.parseDelimitedFrom(is)) != null) {
					switch (clientMsg.getRequestType()) {
						case LOGOUT:
							clientWriteThread.interrupt();
							logout = true;
							sendLogoutResponse();
							break;
						case MESSAGE:
							chatServer.addMessage(clientMsg.getUsername() + ": " + clientMsg.getMessage());
							break;
						case N_ONLINE_USERS:
							sendNOnlineUsers(chatServer.loggedIn.size());
							break;
						case LIST_ONLINE_USERS:
							sendOnlineUsersList(chatServer.loggedIn);
							break;
					}
				}
			} catch (IOException e) {
				clientWriteThread.interrupt();
				System.err.println(e.getMessage());
			}
			return logout;
		}

		private void sendNOnlineUsers(int nOnlineUsers) throws IOException {
			ChatResponse.newBuilder()
						.setNOnlineUsers(nOnlineUsers)
						.setResponseType(ChatResponse.ResponseType.N_ONLINE_USERS)
						.build()
						.writeDelimitedTo(os);
			os.flush();
		}

		private void sendOnlineUsersList(Set<String> loggedIn) throws IOException {
			ChatResponse.Builder respBuilder;

			synchronized (loggedIn) {
				respBuilder = ChatResponse.newBuilder().addAllOnlineUsers(loggedIn);
			}
			respBuilder.setResponseType(ChatResponse.ResponseType.LIST_ONLINE_USERS)
					   .build()
					   .writeDelimitedTo(os);
			os.flush();
		}

		private void sendLogoutResponse() throws IOException {
			ChatResponse.newBuilder()
						.setResponseType(ChatResponse.ResponseType.LOGOUT)
						.build()
						.writeDelimitedTo(os);
			os.flush();
		}
	}

	private static class ClientWriteTask implements Runnable {

		private int nextMsg;
		private final String username;
		private final OutputStream os;
		private final ProtoChatServer chatServer;

		public ClientWriteTask(String username, int nextMsg, OutputStream os, ProtoChatServer chatServer) {
			this.username = username;
			this.nextMsg = nextMsg;
			this.os = os;
			this.chatServer = chatServer;
		}

		@Override
		public void run() {
			Thread currThread = Thread.currentThread();

			try {
				while (currThread.interrupted() == false) {
					String msg = chatServer.getMessage(nextMsg);
					
					if (msg != null) {
						ChatResponse.newBuilder()
									.setMessage(msg)
									.setResponseType(ChatResponse.ResponseType.MESSAGE)
									.build()
									.writeDelimitedTo(os);
						os.flush();
					}
					++nextMsg;
				}
			} catch (InterruptedException e) {
				currThread.interrupt();
			} catch (IOException e1) {
				System.err.println(e1.getMessage());
				try {
					os.close();
				} catch (IOException e2) {
					System.err.println(e2.getMessage());
				}
			}
			chatServer.logout(username, nextMsg);
		}
	}
}
