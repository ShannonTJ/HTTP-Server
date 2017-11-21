
/**
 * WebServer Class
 * 
 * @author Shannon Tucker-Jones 10101385 (template code by Majid Ghaderi)
 * @version Oct 20, 2017
 * 
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class WebServer implements Runnable 
{
	//Initialize flag variable
	private volatile boolean flag = true;
	//Initialize port number variable
	private volatile int portNumber;
	//Initialize ExecutorService
	private ExecutorService ex;
	//Initialize default thread pool size
	private static final int POOL_SIZE = 8;

    /**
     * Default constructor to initialize the web server
     * 
     * @param port 	The server port at which the web server listens (> 1024 and < 65536)
     * 
     */
	public WebServer(int port) 
	{
		//If command line input is within an acceptable range, set port number to input 
		if(port > 1024 && port < 65536)
			portNumber = port;
		
		//Else print an error message
		//Exit program
		else
		{
			System.out.println("ERROR: Invalid Port Number. Value should be greater than 1024 and less than 65536.");
			System.exit(0);
		}
	}

	
    /**
     *   The main loop of the web server:
     *   Opens a server socket at the specified server port.
	 *   Remains in listening mode until shutdown signal.
	 * 
     */
	public void run() 
	{
		//Initialize variables
		ServerSocket server = null;
		Socket client = null;
		String input;
		
		//Create fixed thread pool 
		ex = Executors.newFixedThreadPool(POOL_SIZE);
		
		try
		{
			//Open server socket
			server = new ServerSocket(portNumber);
			
			//Set socket timeout (1 second)
			server.setSoTimeout(1000);
			
			while(flag)
			{
				try
				{
					//Accept new connection
					client = server.accept();
			
					//Create worker thread to handle a new connection
					ex.execute(new MultiThread(client));	
				}
				catch (SocketTimeoutException e)
				{
					//Do nothing
					//Check the flag variable
				}
			}
			
			//Shut down the executor
			try
			{
				//Do not accept any new tasks
				ex.shutdown();
				
				//Wait 5 seconds for existing tasks to terminate
				if(!ex.awaitTermination(1, TimeUnit.SECONDS))
					ex.shutdownNow();
			}
			catch (InterruptedException e)
			{
				//Cancel currently executing threads
				ex.shutdownNow();
			}
			
			//Close the server socket
			server.close();
			
		}
		catch (IOException e)
		{
			//Check where error is
			e.printStackTrace();
			e.getMessage();
		}
		
		return;
	}

	
    /**
     * Signals the server to shutdown.
	 *
     */
	public void shutdown() 
	{
		//Break while loop
		//Terminate main thread
		flag = false;
		
		return;
	}

	
	/**
	 * A simple driver.
	 */
	public static void main(String[] args) 
	{
		//Initialize port variable
		int serverPort = 0;
		
		//If there is no command line argument...
		//Set a default port number
		if(args.length == 0)
			serverPort = 2225;
		
		//Else there is a single command line argument...
		else if (args.length == 1) 
		{
			try
			{
				//Parse the argument for a port number
				serverPort = Integer.parseInt(args[0]);
			}
			catch(NumberFormatException e)
			{
				//Catch anything that isn't an integer
				System.out.println("ERROR: Invalid Command Line Argument. Value should be an integer.");
				System.out.println("USAGE: WebServer <integer>");
				
				//Exit program
				System.exit(0);
			}
		}
		
		//Else there are multiple command line arguments...
		else
		{
			//Print an error message
			System.out.println("ERROR: Wrong Number of Arguments.");
			System.out.println("USAGE: WebServer <port>");
			
			//Exit program
			System.exit(0);
		}
		
		System.out.println("Starting the server on port " + serverPort + "...");
		System.out.println("Server started. Type \"quit\" to stop.");
		System.out.println(".....................................");

		//Instantiate server in a main thread
		WebServer server = new WebServer(serverPort);
		Thread mainThread = new Thread(server);
		
		//Start main thread
		mainThread.start();
		
		//Keep main thread running while user input isn't "quit"
		Scanner keyboard = new Scanner(System.in);
		while(!keyboard.next().equals("quit"))
							
		//Print a shutdown message
		System.out.println();
		System.out.println("Shutting down the server...");
		
		//Shut down server
		server.shutdown();
		
		System.out.println("Server stopped.");
	}
}
