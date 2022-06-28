import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.IOException;
import java.util.LinkedList;
import org.json.*;
import java.io.PrintWriter;
import netscape.javascript.JSObject;

/**
 * This is the main class for the peer2peer program.
 * It starts a client with a username and port. Next the peer can decide who to listen to. 
 * So this peer2peer application is basically a subscriber model, we can "blurt" out to anyone who wants to listen and 
 * we can decide who to listen to. We cannot limit in here who can listen to us. So we talk publicly but listen to only the other peers
 * we are interested in. 
 * 
 */

public class Peer {
	private String username;
	private BufferedReader bufferedReader;
	private ServerThread serverThread;
	private static Peer peer;
	private static boolean twoArgs = false;
	private static int port;
	private static int port4Listening;
	public static LinkedList<Integer> ports = new LinkedList<>();

	public Peer(BufferedReader bufReader, String username, ServerThread serverThread) {
		this.username = username;
		this.bufferedReader = bufReader;
		this.serverThread = serverThread;
	}

	/**
	 * Main method saying hi and also starting the Server thread where other peers can subscribe to listen
	 *
	 * @param args[0] username
	 * @param args[1] port for server
	 */
	public static void main(String[] args) throws Exception {

		port = Integer.parseInt(args[1]);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		String username = args[0];

		// starting the Server Thread, which waits for other peers to want to connect
		ServerThread serverThread = new ServerThread(args[1]);
		serverThread.start();
		peer = new Peer(bufferedReader, args[0], serverThread);

		if (args.length == 2) {
			peer.askForInput();
		}
		if (args.length >= 3) {
			twoArgs = true;
			try {
				System.out.println("args is greater than, equal to 2");
				port4Listening = Integer.parseInt(args[2]);
				ports.add(port4Listening);
				peer.updateListeningPeers(port);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Hello " + username + " and welcome! Your port will be " + port);

	}

	/**
	 * User is asked to define who they want to subscribe/listen to
	 * Per default we listen to no one
	 */
	public void updateListeningPeers(int incomingPort) throws Exception {
		Socket socket = null;

		try {
			socket = new Socket("localhost", port4Listening);

			JSONObject json = new JSONObject();
			json.put("enter", "1");
			json.put("data", port);

			PrintWriter sout = new PrintWriter(socket.getOutputStream(), true);
			sout.println(json.toString());

			new ClientThread(socket).start();

		} catch (Exception c) {
			if (socket != null) {
				socket.close();
			} else {
				System.out.println("Cannot connect, wrong input");
				System.out.println("Exiting: I know really user friendly");
				System.exit(0);
			}
		}

		askForInput();
	}

	/**
	 * Client waits for user to input their message or quit
	 *
	 * @param bufReader bufferedReader to listen for user entries
	 * @param username name of this peer
	 * @param serverThread server thread that is waiting for peers to sign up
	 */
	public void askForInput() throws Exception {
		try {
			System.out.println("You can now start chatting (exit to exit)");
			while (true) {
				String message = bufferedReader.readLine();
				if (message.equals("exit")) {
					System.out.println("bye, see you next time");
					break;
				} else {
					// we are sending the message to our server thread. this one is then responsible for sending it to listening peers
					serverThread.sendMessage("{'username': '" + username + "','message':'" + message + "'}");
				}
			}
			System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public boolean checkport_new(LinkedList<Integer> list) throws IOException {
		Socket socket;
		for (Integer s : list) {
			if (!ports.contains(s)) {
				ports.add(s);
				return true;
			}
		}
		return false;
	}
	public void retrieve(LinkedList<Integer> list) throws IOException {
		Socket socket;
		for (Integer s : ports) {
			list.add(s);
		}

	}

	public LinkedList<Integer> getList() {
		return ports;
	}
}