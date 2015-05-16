import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server implements Runnable {
	public final String[] commands = { ":GET_ADDRESS", ":GET_PORT", ":SPEAK",
			":QUIT", ":REVERSE" };
	protected ServerSocket serverSocket;
	protected Thread currentThread;
	protected boolean isRunning = true;
	protected final int TIMEOUT = 300000;
	protected int port;

	public Server(int port) throws IOException {
		this.port = port;
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(TIMEOUT);
	}

	public void run() {
		synchronized (this) {
			this.currentThread = Thread.currentThread();
		}
		while (isRunning) {
			Socket connection;
			try {
				connection = serverSocket.accept();
				processRequest(connection);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Client connected");

		}
	}

	public void processRequest(Socket connection) {
		try {
			DataInputStream inputData = new DataInputStream(
					connection.getInputStream());
			DataOutputStream outputData = new DataOutputStream(
					connection.getOutputStream());
			String input = inputData.readUTF();
			if (input.equals(commands[0])) {
				outputData.writeUTF("The server address is "
						+ connection.getInetAddress().getHostAddress());
			} else if (input.equals(commands[1])) {
				System.out
						.println("The server port is " + connection.getPort());
			} else if (input.equals(commands[2])) {
				System.out.println("Here's Johnny!!!");
			} else if (input.equals(commands[3])) {
				System.out.println("Goodbye!");
				connection.close();
			} else if (input.equals(commands[4])) {
				String s = inputData.readUTF();
				StringBuilder builder = new StringBuilder(s);
				builder.reverse();
				String output = builder.toString();
				outputData.writeUTF(output);
			} else if (input.contains("-c")) {
				String output = commands.toString();
				outputData.writeUTF(output);
			} else {
				outputData.writeUTF("Invalid Command");
			}
			return;
		} catch (SocketTimeoutException e) {
			System.out.println("Client Timed Out");
		} catch (IOException e) {
			System.out.println("IO Exception.");
			return;
		}
	}
	
	private synchronized boolean isRunning() {
        return this.isRunning;
    }

    public synchronized void stop(){
        this.isRunning = false;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

	public static void main(String[] args) throws Exception {
		Server server = new Server(Integer.parseInt(args[0]));
		new Thread(server).start();
		Thread.sleep(40000);
		server.stop();
	}

}
