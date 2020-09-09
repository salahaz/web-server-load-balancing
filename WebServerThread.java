import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.StringTokenizer;
import java.util.Properties;
import java.nio.file.*;
import java.util.Date;

public class WebServerThread extends Thread {
    private Socket connectionFromClient;

    public WebServerThread(Socket connectionFromClient) {
        this.connectionFromClient = connectionFromClient;
        start();
    }

    public void run() {

        try {
            Properties props = new Properties();
            props.load(new FileInputStream("./server.config"));
            String root = props.getProperty("root");

            InputStream in = connectionFromClient.getInputStream();
            OutputStream out = connectionFromClient.getOutputStream();
            BufferedReader headerReader = new BufferedReader(new InputStreamReader(in));
            BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(out));

            DataOutputStream dataOut = new DataOutputStream(out);

            String header = headerReader.readLine();

            StringTokenizer strk = new StringTokenizer(header, " ");

            String verb = strk.nextToken();

            if (verb.equals("GET") || verb.equals("HEAD")) {

                String location = strk.nextToken();

                String newLocation = props.getProperty(location);
                try {
                    if (newLocation == null) {
                        String path = root + location;
                        FileInputStream fileIn = new FileInputStream(path);
                        int fileSize = fileIn.available();
                        String fileType = Files.probeContentType(Paths.get(path));
                        header = "HTTP/1.1 200 OK\nContent-Length: " + fileSize + "\nConnection: Close\nContent-Type: "
                                + fileType + "\nDate: " + new Date() + "\n\n";
                        headerWriter.write(header, 0, header.length());
                        headerWriter.flush();

                        if (verb.equals("GET")) {

                            byte[] bytes = new byte[fileSize];
                            fileIn.read(bytes);
                            fileIn.close();
                            dataOut.write(bytes, 0, fileSize);
                        }

                    } else {
                        String path = root + newLocation;
                        FileInputStream fileIn = new FileInputStream(path);
                        int fileSize = fileIn.available();
                        String fileType = Files.probeContentType(Paths.get(path));
                        header = "HTTP/1.1 302 Found\nContent-Length: " + fileSize + "\nLocation: " + newLocation
                                + "\nConnection: Close\nContent-Type: " + fileType + "\nDate: " + new Date() + "\n\n";
                        headerWriter.write(header, 0, header.length());
                        headerWriter.flush();
                        if (verb.equals("GET")) {

                            byte[] bytes = new byte[fileSize];
                            fileIn.read(bytes);
                            fileIn.close();
                            dataOut.write(bytes, 0, fileSize);
                        }
                    }
                } catch (Exception ex) {
                    String path = root + "/error.html";
                    FileInputStream fileIn = new FileInputStream(path);
                    int fileSize = fileIn.available();
                    String fileType = Files.probeContentType(Paths.get(path));
                    header = "HTTP/1.1 404 NOT FOUND\nContent-Length: " + fileSize
                            + "\nConnection: Close\nContent-Type: " + fileType + "\nDate: " + new Date() + "\n\n";
                    headerWriter.write(header, 0, header.length());
                    headerWriter.flush();
                    if (verb.equals("GET")) {
                        byte[] bytes = new byte[fileSize];
                        fileIn.read(bytes);
                        fileIn.close();
                        dataOut.write(bytes, 0, fileSize);
                    }
                }

            } else {
                String path = root + "/notImplemented.html";
                FileInputStream fileIn = new FileInputStream(path);
                int fileSize = fileIn.available();
                String fileType = Files.probeContentType(Paths.get(path));
                header = "HTTP/1.1 501 NOT IMPLEMENTED\nContent-Length: " + fileSize
                        + "\nConnection: Close\nContent-Type: " + fileType + "\nDate: " + new Date() + "\n\n";
                headerWriter.write(header, 0, header.length());
                headerWriter.flush();
                byte[] bytes = new byte[fileSize];
                fileIn.read(bytes);
                fileIn.close();
                dataOut.write(bytes, 0, fileSize);
            }

            connectionFromClient.close();

        } catch (Exception ex) {
        }

    }

}