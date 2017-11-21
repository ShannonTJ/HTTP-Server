/**
 * MultiThread Class
 * 
 * @author Shannon Tucker-Jones 10101385
 * @version Oct 20, 2017
 * 
 */

import java.io.*;
import java.nio.file.*;
import java.nio.file.spi.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class MultiThread implements Runnable 
{
	
	//Initialize client socket for each thread
	private Socket client;
	
	 /**
     * Default constructor to initialize the worker thread
     * 
     * @param socket 	A thread's socket connection to a user
     * 
     */
	public MultiThread(Socket socket)
	{
		this.client = socket;
	}
	
	 /**
     *   The main functionality of every worker thread:
     *   Reads a client's HTTP request and parses the request to ensure it is formatted properly.
     *   Sends an appropriate HTTP response to the client. If the requested file exists, it is sent to the client as well.
     *   Closes the connection.
	 * 
     */
	public void run()
	{
		//Initialize variables
		InputStream input = null;
		OutputStream output = null;
		
		String url = null;
		String header = null;
		String serverName = "CPSC 441 A2 Server";
		DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		String dateResponse = getCurrentTime(dateFormat);
		
		try
		{
			//Get the client socket's input/output streams
			input = client.getInputStream();
			output = client.getOutputStream();
					
			//Read the HTTP request character by character
			String request = "";
			while(!request.contains("\r\n\r\n"))
				request = request + (char) input.read();
			
				//Split HTTP request into different lines
				String[] parse = request.split("\r\n");					
				
				//Print the client's GET request
				System.out.println("YOUR HTTP REQUEST: ");
				System.out.print(parse[0] + "\n");
				System.out.println();

				//Check GET line format (for HTTP/1.1 and HTTP/1.0)
				Pattern pattern = Pattern.compile("GET (.*) HTTP/1.1");
				Matcher matcher = pattern.matcher(parse[0]);
				Pattern pattern1 = Pattern.compile("GET (.*) HTTP/1.0");
				Matcher matcher1 = pattern1.matcher(parse[0]);
						
				//If GET line has correct format...
				//Extract the url
				if(matcher.matches())
						url = matcher.group(1);
				else if(matcher1.matches())
						url = matcher1.group(1);
					
			//If request is improperly formatted...
			//Return error
			if(url == null)
			{
				//Create 400 Bad Request header
				header = "HTTP/1.1 400 Bad Request\r\n" +  "Date: " + dateResponse + "\r\n";
				header += "Server: " + serverName + "\r\n" + "Connection: close\r\n\r\n";
						
				//Print header
				System.out.println(header);	
						
				//Add body after the header
				header += "ERROR 400: Bad Request. The request could not be understood by the server due to malformed syntax.\n";
						
				//Convert header/body to bytes
				byte[] badRequest = header.getBytes("US-ASCII");
						
				//Send header/body to client
				output.write(badRequest);
				output.flush();
			}
					
			//Else request is properly formatted
			else
			{			
				//Get the url's pathname
				String path = getPathName(url);
				
				//If pathname is properly formatted
				if(path != null)
				{		
					//Create a path with the url's pathname
					Path p = Paths.get(path);
						
					//Create a file object with that path						
					File f = new File(path);
											
					//Check if file exists
					if(f.exists())
					{
						//Get length of file
						long lengthResponse = f.length();
						
						//Get type of file
						String typeResponse = Files.probeContentType(p); 
								
						//Get last modified date in GMT
						long millis = f.lastModified();
						Date mDate = new Date(millis);
						String lmdResponse = dateFormat.format(mDate);
								
						//Create 200 OK header
						header = "HTTP/1.1 200 OK\r\n" + "Date: " + dateResponse + "\r\n" + "Server: " + serverName + "\r\n"; 
						header += "Last-Modified: " + lmdResponse + "\r\n" + "Content-Length: " + lengthResponse + "\r\n";
						header += "Content-Type: " + typeResponse + "\r\n" + "Connection: close\r\n\r\n";
		
						//Print header
						System.out.println(header);		
													
						//Convert header to bytes
						byte[] ok = header.getBytes("US-ASCII");
							
						//Convert file to bytes
						byte[] bFile = new byte[(int) f.length()];
						FileInputStream fis = null;
							
						try
						{
							//Read file to the byte array
							fis = new FileInputStream(f);
							fis.read(bFile);
						}
						catch (Exception e)
						{
							//Check where error is
							e.printStackTrace();
							e.getMessage();
						}
						finally
						{
							//Close stream
							if(fis != null)
								fis.close();
						}
								
						//Concatenate body and header byte arrays
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						baos.write(ok);
						baos.write(bFile);
						byte[] headerBody = baos.toByteArray(); 
							
						//Send to client
						output.write(headerBody);
						output.flush();						
					}
						
					//Else file does not exist
					//Return error
					else
					{
						//Create 404 Not Found header
						header = "HTTP/1.1 404 Not Found" + "\r\n" + "Date: " + dateResponse + "\r\n";
						header += "Server: " + serverName + "\r\n" + "Connection: close" + "\r\n\r\n";
								
						//Print header
						System.out.println(header);
								
						//Add body after the header
						header += "ERROR 404: Page Not Found. The requested URL " + url + " was not found on this server.";
								
						//Convert header/body to bytes
						byte[] notFound = header.getBytes("US-ASCII");
								
						//Send header/body to client
						output.write(notFound);
						output.flush();
					}
					
				}
						
				//Else pathname is improperly formatted
				//Return error
				else
				{
					//Create 404 Not Found header
					header = "HTTP/1.1 404 Not Found" + "\r\n" + "Date: " + dateResponse + "\r\n";
					header += "Server: " + serverName + "\r\n" + "Connection: close" + "\r\n\r\n";
							
					//Print header
					System.out.println(header);
							
					//Add body after the header
					header += "ERROR 404: Page Not Found. The requested URL " + url + " was not found on this server.";
							
					//Convert header/body to bytes
					byte[] notFound = header.getBytes("US-ASCII");
							
					//Send header/body to client
					output.write(notFound);
					output.flush();
				}
			}
					
			//Close the connection	
			client.close();
			input.close();
			output.close();
					
		}
		catch (Exception e)
		{
			//Check where error is
			e.printStackTrace();
			e.getMessage();
		}	
		return;
	}
	
	 /**
     * Gets the current date and time (GMT) in a user-specified format
     * 
     * @param DateFormat specified by the user
     * @return String containing the current date and time in GMT
     * 
     */
	public String getCurrentTime(DateFormat dateFormat)
	{
		//Initialize date format in GMT
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date();
						
		//Return current date in GMT
		return(dateFormat.format(date));		
	}
	
	
	 /**
     * Parses an URL for its pathname
     * 
     * @param  String containing an URL
     * @return String containing the URL's pathname
     * 
     */
	public String getPathName(String url)
	{
		String path = null;
		
		//Error check for strings that do not contain "/" or strings which contain multiple "/"
		if(url.contains("/") && !url.contains("//"))
		{
			//Split pathname by "/"
			String[] halve = url.split("/");

			//After the split, pathname should have more than one component
			if(halve.length > 1)
			{
				//Format pathname properly
				path = halve[1];
				for(int i = 2; i < halve.length; i++)
					path = path + "/" + halve[i];
			}
		}
		
		//Return path name or null string
		return(path);
	}
}
