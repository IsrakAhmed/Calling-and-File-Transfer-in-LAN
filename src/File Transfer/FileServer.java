import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());

                // Handle file reception in a separate thread
                new Thread(() -> receiveFile(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void receiveFile(Socket socket) {
        try (InputStream inputStream = socket.getInputStream()) {

            // First, read the length of the filename
            byte[] lengthBuffer = new byte[4];
            inputStream.read(lengthBuffer);
            int fileNameLength = java.nio.ByteBuffer.wrap(lengthBuffer).getInt();

            // Read the filename
            byte[] fileNameBytes = new byte[fileNameLength];
            inputStream.read(fileNameBytes);
            String fileName = new String(fileNameBytes);

            // Create the "received_files" directory if it doesn't exist
            File directory = new File("received_files");
            if (!directory.exists()) {
                directory.mkdir();
            }

            // Log the filename to ensure it's valid
            System.out.println("Received file name: " + fileName);

            // Construct the full file path
            String filePath = "received_files/" + fileName;
            System.out.println("Saving file to: " + filePath);

            // Create a FileOutputStream for the file
            File file = new File(filePath);

            // Save the file with the received filename
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {

                // Track file size
                long fileSize = file.length();
                long totalBytesRead = 0;

                // Read and write file data with progress tracking
                byte[] buffer = new byte[4096];
                int bytesRead;

                // To update progress, we'll need to read the file size from the client
                long fileSizeFromClient = getFileSizeFromClient(inputStream);

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);

                    totalBytesRead += bytesRead;

                    // Calculate and print progress
                    int progress = (int) ((totalBytesRead * 100) / fileSizeFromClient);
                    System.out.print("\rProgress: " + progress + "%");
                }

                System.out.println("\nFile received successfully as " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static long getFileSizeFromClient(InputStream inputStream) throws Exception {
        byte[] lengthBuffer = new byte[8];
        inputStream.read(lengthBuffer);
        return java.nio.ByteBuffer.wrap(lengthBuffer).getLong();
    }
}