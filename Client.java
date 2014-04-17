import java.io.*;
import java.net.Socket;
 
public class Client {
    private Socket socket = null;
    private boolean isConnected = false;
    private ObjectOutputStream outputStream = null;
    private String sourceDirectory = "E:/temp/songs";
    private String destinationDirectory = "C:/tmp/downloads/";
    private int fileCount = 0;
    private FileEvent fileEvent = null;
 
    public Client() {
 
    }
 
    public void connect() {
        while (!isConnected) {
            try {
                socket = new Socket("localHost", 4445);
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                isConnected = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
 
    public void locateFiles() {
        File srcDir = new File(sourceDirectory);
        if (!srcDir.isDirectory()) {
            System.out.println("Source directory is not valid ..Exiting the client");
            System.exit(0);
        }
        File[] files = srcDir.listFiles();
        fileCount = files.length;
        if (fileCount == 0) {
            System.out.println("Empty directory ..Exiting the client");
            System.exit(0);
        }
 
        for (int i = 0; i < fileCount; i++) {
            System.out.println("Sending " + files[i].getAbsolutePath());
            sendFile(files[i].getAbsolutePath(), fileCount - i - 1);
            System.out.println(files[i].getAbsolutePath());
        }
    }
 
    public void sendFile(String fileName, int index) {
        fileEvent = new FileEvent();
        fileEvent.setDestinationDirectory(destinationDirectory);
        fileEvent.setSourceDirectory(sourceDirectory);
        File file = new File(fileName);
        fileEvent.setFilename(file.getName());
        fileEvent.setRemainder(index);
        DataInputStream diStream = null;
        try {
            diStream = new DataInputStream(new FileInputStream(file));
            long len = (int) file.length();
            byte[] fileBytes = new byte[(int) len];
 
            int read = 0;
            int numRead = 0;
            while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read,
                    fileBytes.length - read)) >= 0) {
                read = read + numRead;
            }
            fileEvent.setFileData(fileBytes);
            fileEvent.setStatus("Success");
        } catch (Exception e) {
            e.printStackTrace();
            fileEvent.setStatus("Error");
 
        }
 
        try {
            outputStream.writeObject(fileEvent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public static void main(String[] args) {
        Client client = new Client();
        client.connect();
        client.locateFiles();
    }
}