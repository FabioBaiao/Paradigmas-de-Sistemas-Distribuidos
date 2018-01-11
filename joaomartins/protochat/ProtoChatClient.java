/**
 * Cliente poder saber:
 *     - nº de clientes autenticados;
 *	   - lista de clientes logged in.
 */
package protochat;

import java.io.Console;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.net.Socket;

import java.util.concurrent.atomic.AtomicBoolean;

import protochat.ConsoleMenu;
import protochat.ChatMessages.PreLoginRequest;
import protochat.ChatMessages.PreLoginResponse;
import protochat.ChatMessages.ChatMessage;
import protochat.ChatMessages.ChatResponse;

public class ProtoChatClient {

	public static final int DEFAULT_PORT = 5555;
	
	private String username;
	private final InputStream is;
	private final OutputStream os;
	private final Console console;
	private final ConsoleMenu loginRegMenu;
	private final AtomicBoolean loggedIn;

	public ProtoChatClient(Socket clientSocket, Console console) throws IOException {
		this.is = clientSocket.getInputStream();
		this.os = clientSocket.getOutputStream();
		this.console = console;
		this.loginRegMenu =
			new ConsoleMenu("Login/Register", new String[] {"Login", "Register"}, true);
		this.username = null;
		this.loggedIn = new AtomicBoolean(false);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Console console = System.console();

		if (console == null) { // no point in continuing without a console.
			System.err.println("No console.");
			System.exit(1);
		}

		String host = "127.0.0.1";
		int port = DEFAULT_PORT;
		switch (args.length) {
			case 0:
				break;
			case 1:
				host = args[0];
				break;
			case 2:
				host = args[0];
				try {
					port = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					System.err.println(e.getMessage());
					System.exit(1);
				}
				break;
			default:
				System.err.println("Usage: ProtoChatClient [host [port]]");
				System.exit(1);
		}

		try (Socket clientSocket = new Socket(host, port)) {
			ProtoChatClient chatClient = new ProtoChatClient(clientSocket, console);
			chatClient.loginLoop();
		} 
	}

	// Clears the screen on consoles that support ANSI escape codes.
	private void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	//=========================================================================
	// Pre-login
	//=========================================================================

	private void loginLoop() throws IOException, InterruptedException {
		boolean exit = false;
		
		do {
			loginRegMenu.run();
			int op = loginRegMenu.getOption();
			clearScreen();
			switch (op) {
				case 1: // Login
					if (authenticate()) {
						System.out.println("Authentication successful.");
						loggedIn.set(true);
						exit = mainLoop(); // true if EOF is read in mainLoop()
					} else {
						System.err.println("Authentication failed.");
					}
					break;
				case 2: // Register
					if (register() == true) {
						System.out.println("Registration successful.");
					} else {
						System.err.println("Registration failed.");
					}
					break;
				case ConsoleMenu.EOF:
				case ConsoleMenu.EXIT:
					exit = true;
					break;
			}
		} while (exit == false);
	}

	private boolean authenticate() throws IOException {
		PreLoginRequest preLoginRequest =
			createPreLoginRequest(PreLoginRequest.RequestType.AUTHENTICATION);
		preLoginRequest.writeDelimitedTo(os);
		
		PreLoginResponse preLoginResponse = PreLoginResponse.parseDelimitedFrom(is);
		boolean authorized = preLoginResponse.getAuthorized();
		String msg = preLoginResponse.getMessage();

		if (authorized) {
			username = preLoginRequest.getUsername();
			if (msg.length() > 0)
				System.out.println(msg);
		} else if (msg.length() > 0) {
			System.err.println(msg);
		}
		return authorized;
	}

	private boolean register() throws IOException {
		createPreLoginRequest(PreLoginRequest.RequestType.REGISTRATION).writeDelimitedTo(os);
			
		PreLoginResponse preLoginResponse = PreLoginResponse.parseDelimitedFrom(is);
		boolean authorized = preLoginResponse.getAuthorized();
		String msg = preLoginResponse.getMessage();

		if (msg.length() > 0) {
			if(authorized)
				System.out.println(msg);
			else
				System.err.println(msg);
		}
		return authorized;
	}

	private PreLoginRequest createPreLoginRequest(PreLoginRequest.RequestType type) {
		String username = console.readLine("Username: ");
		char[] password = console.readPassword("Password: ");
		PreLoginRequest.Builder preLoginRequest = PreLoginRequest.newBuilder();

		preLoginRequest.setUsername(username);
		preLoginRequest.setPassword(new String(password));
		preLoginRequest.setRequestType(type);
		return preLoginRequest.build();
	}

	//=========================================================================
	// Post-login
	//=========================================================================

	// Returns true if EOF was read; false otherwise.
	private boolean mainLoop() throws IOException, InterruptedException {
		boolean eof = false;
		Thread serverReadThread = new Thread(new ServerReadTask(is, loggedIn));

		serverReadThread.start();
		while (loggedIn.get()) {
			String line = console.readLine();

			if (line == null) { // EOF
				loggedIn.set(false);
				eof = true;
			} else if (line.length() > 0 && line.charAt(0) == '/') { // Command
				String lcLine = line.toLowerCase();

				if (lcLine.equals("/list")) {
					sendRequest(ChatMessage.RequestType.LIST_ONLINE_USERS);
				} else if (lcLine.equals("/count")) {
					sendRequest(ChatMessage.RequestType.N_ONLINE_USERS);
				} else if (lcLine.startsWith("/say ")) {
					sendMessage(line.replaceFirst("/say ", ""));
				} else if (lcLine.equals("/help")) {
					printHelp();
				} else if (lcLine.equals("/logout")) {
					loggedIn.set(false);
				} else {
					System.err.printf("Invalid command: %s. Try \\help.%n", line);
				}
			} else { // Normal message
				sendMessage(line);
			}
		}
		sendRequest(ChatMessage.RequestType.LOGOUT);
		serverReadThread.join();
		return eof;
	}

	private void sendRequest(ChatMessage.RequestType type) throws IOException {
		ChatMessage.newBuilder()
				   .setRequestType(type)
				   .build()
				   .writeDelimitedTo(os);
		os.flush();
	}

	private void sendMessage(String msg) throws IOException {
		ChatMessage.newBuilder()
		           .setUsername(username)
		           .setMessage(msg)
		           .build()
		           .writeDelimitedTo(os);
		os.flush();
	}

	private void printHelp() {
		System.out.printf(
			"Commands:%n" +
			"/list\t\tList online users%n" +
			"/count\t\tPrint number of online users%n" +
			"/say <msg>\tSend message <msg>%n" +
			"/logout\t\tLogout%n"
		);
	}

	// Task responsible for reading ChatResponses from server and printing their content.
	private static class ServerReadTask implements Runnable {

		private final InputStream is;
		private final AtomicBoolean loggedIn;

		public ServerReadTask(InputStream is, AtomicBoolean loggedIn) {
			this.is = is;
			this.loggedIn = loggedIn;
		}

		public void run() {
			try {
				boolean logoutRcvd = false;

				while (!logoutRcvd) {
					ChatResponse resp = ChatResponse.parseDelimitedFrom(is);

					if (resp == null) { // EOF (server closed its output stream)
						loggedIn.set(false);
						break;
					}

					switch (resp.getResponseType()) {
						case MESSAGE:
							System.out.println(resp.getMessage());
							break;
						case N_ONLINE_USERS:
							System.out.printf("%d users online.%n", resp.getNOnlineUsers());
							break;
						case LIST_ONLINE_USERS:
							System.out.println("Online users:");
							for (String usr : resp.getOnlineUsersList()) {
								System.out.printf("  - %s%n", usr);
							}
							break;
						case LOGOUT: // without a logout response, current thread could stay blocked in parseDelimiterFrom()
							logoutRcvd = true;
							break;
					}
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
				loggedIn.set(false);
			}
		}
	}
}
