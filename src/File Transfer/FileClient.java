import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class FileClient {

    private static final String SERVER_IP = "192.168.1.127"; // Replace with your server's IP address
    private static final int PORT = 8080;

    public static void main(String[] args) {

        System.out.print("Enter the path of the file : ");

        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();

        File file = new File(path); // Replace with the path to the file you want to send

        try (Socket socket = new Socket(SERVER_IP, PORT);
             FileInputStream fileInputStream = new FileInputStream(file);
             OutputStream outputStream = socket.getOutputStream()) {

            System.out.println("Connected to the server.");

            // Send the length of the filename
            byte[] fileNameBytes = file.getName().getBytes();
            outputStream.write(java.nio.ByteBuffer.allocate(4).putInt(fileNameBytes.length).array());
            outputStream.flush();

            // Send the filename
            outputStream.write(file.getName().getBytes());
            outputStream.flush(); // Ensure the filename is sent before the file data

            // Send the file size
            long fileSize = file.length();
            outputStream.write(java.nio.ByteBuffer.allocate(8).putLong(fileSize).array());
            outputStream.flush();

            // Send the file data
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("File sent successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}