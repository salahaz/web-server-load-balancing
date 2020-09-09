import java.net.*;
import java.util.Properties;
import java.io.*;

public class WebServer {
    public static void main(String args[]) throws Exception {

        Properties props = new Properties();
        props.load(new FileInputStream("./server.config"));
        int port = Integer.parseInt(props.getProperty("port"));
        ServerSocket ss = new ServerSocket(port);
        while (true) {
            System.out.println("Server Waiting...");
            Socket connectionFromClient = ss.accept();
            new WebServerThread(connectionFromClient);
        }

    }

}