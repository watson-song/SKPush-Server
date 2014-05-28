package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestTcpServer {

	public static void main(String[] args) {
		for (int i = 0; i < 2000; i++) {
			final int j = i;
			new Thread("Thread-" + i) {
				public void run() {
					new TestTcpServer().startNewClient(j);
				};
			}.start();
		}
	}

	private void startNewClient(int i) {
		try{	
			Socket socket=new Socket("172.30.86.29",8080);
			PrintWriter os=new PrintWriter(socket.getOutputStream());
			BufferedReader is=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String readline = "hello world "+this+", ->"+i;
			os.println(readline);
			os.flush();
			System.out.println("Client:"+readline);
			while(!readline.equals("bye")){
				readline=is.readLine();
				System.out.println("Server:"+readline);
			}
			os.close();
			is.close();
			socket.close();
		}catch(Exception e){
			System.out.println("error "+e.toString());
		}
	}
}
