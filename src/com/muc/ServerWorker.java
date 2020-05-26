package com.muc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author hirakdhar
 *
 */

public class ServerWorker extends Thread {

	private final Socket clientSocket;
	public Socket getClientSocket() {
		return clientSocket;
	}

	private String login;
	public String getLogin() {
		return login;
	}

	private Server server;

	public ServerWorker(Socket clientSocket, Server server) {
		this.clientSocket = clientSocket;
		this.server = server;
	}

	@Override
	public void run() {
		try {
			handleClientSocket(clientSocket);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	private void handleClientSocket(Socket clientSocket) throws IOException, InterruptedException {
		InputStream inputStream = clientSocket.getInputStream();
		OutputStream outputStream = clientSocket.getOutputStream();
		BufferedReader bufferredReader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while((line = bufferredReader.readLine())  != null) {
			String[] tokens = StringUtils.split(line);
			
			if(null != tokens && tokens.length >0) {
			
				String cmd = tokens[0];
				if("quit".equalsIgnoreCase(cmd)) {
					handleLogoff( outputStream, tokens) ;
					break;
				}else if("login".equalsIgnoreCase(cmd)) {
					handleLogin(outputStream, tokens);
				}else if(isMsgToOtherUser(cmd)) {
					sendMessageToOtherUser( cmd, StringUtils.split(line, " ", 2)[1]);
				}
				else {
					String msg = "NO SUCH COMMAND : "+cmd+"\n";
					outputStream.write(msg.getBytes());
				}
			}
		}
		
		outputStream.close();
	}
	
	private boolean isMsgToOtherUser(String cmd) {
		for(ServerWorker serverWorker : server.getWorkerList()) {
			if(cmd.equalsIgnoreCase(serverWorker.getLogin())) {
				return true;
			}
		}
		return false;
	}

	private void sendMessageToOtherUser(String login, String msgBody) throws IOException {
		for(ServerWorker serverWorker : server.getWorkerList()) {
			if(login.equalsIgnoreCase(serverWorker.getLogin())) {
				serverWorker.getClientSocket().getOutputStream().write((this.getLogin() +" : "+ msgBody+ "\n").getBytes());
			}
		}
		
	}
	
	private void sendStatusMsgToOthers(String status) throws IOException {
		for(ServerWorker serverWorker : server.getWorkerList()) {
			if(!this.login.equalsIgnoreCase(serverWorker.getLogin()))
				serverWorker.getClientSocket().getOutputStream().write((status +" "+ this.getLogin() + "\n").getBytes());
		}
	}
	
	private void listLoggedInUsers(OutputStream outputStream) throws IOException {
		for(ServerWorker serverWorker : server.getWorkerList()) {
			if(!this.login.equalsIgnoreCase(serverWorker.getLogin()))
				outputStream.write(("Online "+ serverWorker.getLogin() + "\n").getBytes());
		}
	}
	
	private void handleLogoff(OutputStream outputStream, String[] tokens) throws IOException {
		server.removeLoggedOffWorker(this);
		String msg = "Ok Logoff " + login + "\n";
		outputStream.write(msg.getBytes());
		sendStatusMsgToOthers("LoggedOff");
		listLoggedInUsers( outputStream);
	}
	
	private boolean isAlreadyLoggedIn(String login) {
		for(ServerWorker serverWorker : server.getWorkerList()) {
			if(login.equalsIgnoreCase(serverWorker.getLogin())) {
				return true;
			}
		}
		return false;
	}
	
	private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
		String login = tokens[1];
		String password = tokens[2];
		
		String msg ;
		
		if(("guest".equalsIgnoreCase(login) && "guest".equalsIgnoreCase(password))
				|| ("hirak".equalsIgnoreCase(login) && "hirak".equalsIgnoreCase(password))) {
			if(isAlreadyLoggedIn(login)) {
				msg = "User "+login+" is already logged in \n";
				outputStream.write(msg.getBytes());
			}else {
				msg = "Ok Login " + login + "\n";
				this.login = login;
				// Add ServerWorker to the workerList 
				server.addLoggedInWorker(this);
				outputStream.write(msg.getBytes());
				System.out.println("User logged in successfully :" + this.login);
				sendStatusMsgToOthers("Online");
				listLoggedInUsers( outputStream);
			}
		}
		else {
			msg = "Error login \n";
			outputStream.write(msg.getBytes());
		}
		
		
		
	}
}