package com.muc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author hirakdhar
 *
 */

public class Server extends Thread{

	private static int serverPort;
	private ArrayList<ServerWorker> workerList = new ArrayList<>();


	public Server(int port) {
		this.serverPort = port;
	}

	@Override
	public void run() {
		System.out.println(":::::ServerMain Started:::::");
		try {
			ServerSocket serverSocket = new ServerSocket(serverPort);
			while(true) {
				System.out.println("About to accept client connection....");
				Socket clientSocket = serverSocket.accept();
				System.out.println("Accepted client connection from client socket:"+ clientSocket);
				ServerWorker serverWorker = new ServerWorker(clientSocket, this);
//				workerList.add(serverWorker);
				serverWorker.start();
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<ServerWorker> getWorkerList() {
		return workerList;
	}
	
	public List<ServerWorker> addLoggedInWorker(ServerWorker serverWorker){
		workerList.add(serverWorker);
		return workerList;
	}

	public List<ServerWorker> removeLoggedOffWorker(ServerWorker serverWorker){
		workerList.remove(serverWorker);
		return workerList;
	}

}
