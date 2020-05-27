package networking.club;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;


public class HttpWebServer implements Runnable{ 
	
	static final File WEB_ROOT = new File(".");
	static final String DEFAULT_FILE = "index.html";
	static final String FILE_NOT_FOUND = "404.html";
	static final String METHOD_NOT_SUPPORTED = "not_supported.html";
	static final int PORT = 8083;
	
	static final boolean conn = true;
	private Socket connect;
	
	public HttpWebServer(Socket c) {
		connect = c;
	}
	
	public static void main(String[] args) {
		try {
			ServerSocket connectionEstab = new ServerSocket(PORT);
			System.out.println("\nListening for connections from port : " + PORT + " ...\n");
			
			while (true) {
				HttpWebServer myServer = new HttpWebServer(connectionEstab.accept());
				
				if (conn) {
					// Prints the time when the program was compiled
					System.out.println("Connecton established. (" + new Date() + ")");
				}

				Thread thread = new Thread(myServer);
				thread.start();
			}
			
		} catch (IOException e) {
			System.err.println("Error : " + e.getMessage());
		}
	}

	@Override
	public void run() {
	
		BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataExport = null;
		String desiredFile = null;
		
		try {
			
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			
			out = new PrintWriter(connect.getOutputStream());

			dataExport = new BufferedOutputStream(connect.getOutputStream());
			

			String input = in.readLine();

			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase(); 
			desiredFile = parse.nextToken().toLowerCase();
			
			if (!method.equals("GET")  &&  !method.equals("HEAD")) {
				if (conn) {
					System.out.println("501 Not Implemented : " + method + " method.");
				}
				

				File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
				int Content = (int) file.length();
				String contentMimeType = "text/html";
				byte[] fileData = readFileData(file, Content);
					

				out.println("HTTP/1.1 501 Not Implemented");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + Content);
				out.println(); 
				out.flush();
				dataExport.write(fileData, 0, Content);
				dataExport.flush();
				
			} else {
				if (desiredFile.endsWith("/")) {
					desiredFile += DEFAULT_FILE;
				}
				
				File file = new File(WEB_ROOT, desiredFile);
				int Content = (int) file.length();
				String content = getContentType(desiredFile);
				
				if (method.equals("GET")) { // GET method so we return content
					byte[] fileData = readFileData(file, Content);
					
					// sending the HTTP Headers (again, might not do anything)
					out.println("HTTP/1.1 200 Implement success");
					out.println("Date: " + new Date());
					out.println("Content-type: " + content);
					out.println("Content-length: " + Content);
					out.println(); // blank line between headers and content
					out.flush(); // flush character output stream buffer
					
					dataExport.write(fileData, 0, Content);
					dataExport.flush();
				}
				
				if (conn) {
					System.out.println("File " + desiredFile + " of type " + content + " returned");
				}
				
			}
			
		} catch (FileNotFoundException misplacedFile) {
			try {
				fileNotFound(out, dataExport, desiredFile);
			} catch (IOException ioe) {
				System.err.println("Error with file not found exception : " + ioe.getMessage());
			}
			
		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				dataExport.close();
				connect.close(); 
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			} 
			
			if (conn) {
				System.out.println("Connection closed.\n");
			}
		}
		
		
	}
	
	private byte[] readFileData(File file, int Content) throws IOException {
		FileInputStream fileImport= null;
		byte[] fileData = new byte[Content];
		
		try {
			fileImport= new FileInputStream(file);
			fileImport.read(fileData);
		} finally {
			if (fileImport!= null) 
				fileImport.close();
		}
		
		return fileData;
	}
	
	// return supported contentTypes
	private String getContentType(String desiredFile) {
		if (desiredFile.endsWith(".htm")  ||  desiredFile.endsWith(".html"))
			return "text/html";
		else
			return "text/plain";
	}
	
	private void fileNotFound(PrintWriter out, OutputStream dataExport, String desiredFile) throws IOException {
		File file = new File(WEB_ROOT, FILE_NOT_FOUND);
		int Content = (int) file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, Content);
		
		// Tbh IDK if this even does something, but my porgram doesn't work without it so I left it in
		out.println("404 File Not Found, Implement failed");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + Content);
		out.println(); // blank line between headers and content
		out.flush(); // flush character output stream buffer
		
		dataExport.write(fileData, 0, Content);
		dataExport.flush();
		
		if (conn) {
			System.out.println("File " + desiredFile + " not found");
		}
	}
	
}