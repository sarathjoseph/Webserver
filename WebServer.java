package httpserver;

import java.net.*;
import java.io.*;

public class WebServer {
	public static void main(String[] args) {
		new WebServer();
	}

	public WebServer() {
		ServerSocket ss = null;

		try {

			ss = new ServerSocket(9000);
			Socket cs = null;
			while (true) {
				cs = ss.accept();
				ThreadedServer ths = new ThreadedServer(cs);
				ths.start();
			}
		} catch (BindException be) {
			System.out
					.println("Server already running on this computer, stopping.");
		} catch (IOException ioe) {
			System.out.println("IO Error");
			ioe.printStackTrace();
		}

	}
}

class ThreadedServer extends Thread {
	Socket cs;

	public ThreadedServer(Socket cs) {
		this.cs = cs;
	}

	public void run() {

		try {

			System.out.println("New connection");

			String requestline;
			StringBuilder HttpRequest = new StringBuilder();
			StringBuilder PostData = new StringBuilder();
			int contentlength = 0;
			String mime = "Content-Type: text/html\r\n";
			FileInputStream requestedfile = null;
			BufferedReader br;
			PrintWriter pw;

			String index = "index.php";
			String dir = "/home/joseph/web/";

			br = new BufferedReader(new InputStreamReader(cs.getInputStream()));
			pw = new PrintWriter(new OutputStreamWriter(cs.getOutputStream()));

			do {

				requestline = br.readLine();
				if (requestline != null
						&& requestline.contains("Content-Length"))
					contentlength = Integer.parseInt(requestline.split(":")[1]
							.trim());

				HttpRequest.append(requestline + "\n");

			} while (requestline.length() > 0);

			requestline = HttpRequest.toString();
			System.out.println(requestline);

			String[] request = requestline.split("\\n")[0].split(" ");
			String method = request[0];
			String url = request[1];

			if (method.equals("POST")) {

				for (int i = 0; i < contentlength; i++) {
					int a = br.read();
					PostData.append((char) a);

				}
				
				String poststring=new String(PostData.toString());
				String []postdata=poststring.split("&");
				StringBuilder data=new StringBuilder();
				
				for(String p:postdata){
					data.append(p+" ");
					
				}
				PostData=data;
				System.out.println(PostData);
			}
			
		
			
			
			
			
			if(url.endsWith("favicon.ico")){
				pw.flush();
				cs.close();
				System.out.println("connection closed");
				return;
			}
				

			// Keep adding Mime types here

			if (url.endsWith(".js"))
				mime = "Content-Type: application/javascript\r\n";
			if (url.endsWith(".css"))
				mime = "Content-Type: text/css\r\n";

			if ((url.endsWith(".php") || url.endsWith(".py"))
					&& method.equals("POST")) {

				String scripttype = url.split("\\.")[1];

				String line;
				StringBuilder output = new StringBuilder();
				Process p = Runtime.getRuntime().exec(
						scripttype + " " + dir + url + " "
								+ PostData.toString());
				p.waitFor();
				BufferedReader input = new BufferedReader(
						new InputStreamReader(p.getInputStream()));

				while ((line = input.readLine()) != null) {
					output.append(line);
				}
				
				input.close();
				String s = "HTTP/1.0 200 OK \r\n" + mime + "\r\n\r\n";
				String str = output.toString();
				pw.write(s + str);
				System.out.println(s + str);
				pw.flush();
				cs.close();
				System.out.println("connection closed");
				return;

			}

			if (method.equals("GET")) {

				if (url.equals("/"))
					url = index;

				requestedfile = new FileInputStream(dir + url);
				String s = "HTTP/1.0 200 OK \r\n" + mime + "\r\n";

				pw.write(s);
				int b = 0;
				while (b != -1) {

					b = requestedfile.read();
					if (b != -1)
						pw.write(b);
				}
			}

			pw.flush();
			cs.close();
			requestedfile.close();
			System.out.println("connection closed");

		} catch (IOException e) {
			System.out.println("Something went wrong.");
			e.printStackTrace();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
	}

}
