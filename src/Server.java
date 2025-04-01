import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Scanner;

public class Server {
    private static volatile boolean running = true;

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(3000));

        System.out.println("Server started on port " + 3000);

        Thread acceptThread = new Thread(() -> {
            try {
                while (running) {
                    SocketChannel serverChannel = listenChannel.accept();
                    if (serverChannel != null) {
                        executor.submit(new ClientHandler(serverChannel));
                    }
                }
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        });

        acceptThread.start();

        // Monitor admin input
        Scanner scanner = new Scanner(System.in);
        while (running) {
            String command = scanner.nextLine().trim();
            if (command.equalsIgnoreCase("Q")) {
                System.out.println("Shutting down server...");
                running = false;
                try {
                    listenChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                executor.shutdown();
                System.out.println("Server shut down successfully.");
                break;
            }
        }
        scanner.close();
    }
}

class ClientHandler implements Runnable {
    private final SocketChannel serverChannel;
    private static final int BUFFER_SIZE = 1024;

    public ClientHandler(SocketChannel serverChannel) {
        this.serverChannel = serverChannel;
    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            int bytesRead = serverChannel.read(buffer);
            buffer.flip();

            if (bytesRead > 0) {
                byte[] a = new byte[bytesRead];
                buffer.get(a);
                String header = new String(a).trim();
                System.out.println("Header: " + header);

                String command = header.substring(0, 1);
                String argument = header.substring(1).trim();

                switch (command) {
                    case "L":
                        listFiles();
                        break;
                    case "D":
                        downloadFile(argument);
                        break;
                    case "E":
                        deleteFile(argument);
                        break;
                    case "R":
                        renameFile(argument);
                        break;
                    case "U":
                        uploadFile(argument);
                        break;
                    default:
                        sendMessage("Invalid command");
                        System.out.println("Invalid command received: " + header);
                        break;
                }
            } else {
                sendMessage("Invalid command");
                System.out.println("Invalid command received.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void listFiles() throws IOException {
        File directoryPath = new File("ServerFiles/");
        File[] filesList = directoryPath.listFiles();
        if (filesList != null) {
            List<String> fileNames = new ArrayList<>();
            for (File file : filesList) {
                fileNames.add(file.getName());
            }
            sendMessage(String.join("\n", fileNames));
        } else {
            sendMessage("No files found");
        }
    }

    private void downloadFile(String fileName) throws IOException {
        File fileToDownload = new File("ServerFiles/" + fileName);
        if (!fileToDownload.exists()) {
            sendMessage("F");
            System.out.println("File doesn't exist: " + fileName);
        } else {
            try (FileInputStream fs = new FileInputStream(fileToDownload);
                 FileChannel fc = fs.getChannel()) {

                ByteBuffer fileContent = ByteBuffer.allocate(BUFFER_SIZE);
                int byteRead;
                do {
                    byteRead = fc.read(fileContent);
                    fileContent.flip();
                    serverChannel.write(fileContent);
                    fileContent.clear();
                } while (byteRead > 0);
            }
            System.out.println("File sent successfully: " + fileName);
        }
    }

    private void deleteFile(String fileName) throws IOException {
        File fileToDelete = new File("ServerFiles/" + fileName);
        if (fileToDelete.delete()) {
            sendMessage("S");
            System.out.println("File deleted: " + fileName);
        } else {
            sendMessage("F");
            System.out.println("File failed to delete: " + fileName);
        }
    }

    private void renameFile(String fileName) throws IOException {
        File fileToRename = new File("ServerFiles/" + fileName);
        if (!fileToRename.exists()) {
            sendMessage("F");
            System.out.println("File doesn't exist: " + fileName);
            return;
        }

        sendMessage("S"); // Acknowledge file exists
        ByteBuffer renamedFileBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        int bytesRead = serverChannel.read(renamedFileBuffer);
        renamedFileBuffer.flip();
        byte[] b = new byte[bytesRead];
        renamedFileBuffer.get(b);
        String renamed = new String(b).trim();

        File newFile = new File("ServerFiles/" + renamed);
        if (fileToRename.renameTo(newFile)) {
            sendMessage("File was renamed");
            System.out.println("File renamed to: " + renamed);
        } else {
            sendMessage("F");
            System.out.println("Failed to rename file: " + fileName);
        }
    }

    private void uploadFile(String fileName) throws IOException {
        FileOutputStream fs = new FileOutputStream("ServerFiles/" + fileName, true);
        FileChannel fc = fs.getChannel();

        File uploadedFile = new File("ServerFiles/" + fileName);
        if (!uploadedFile.exists()) {
            sendMessage("F");
            System.out.println("File doesn't exist: " + fileName);
        } else {
            sendMessage("S"); // Acknowledge upload start

            ByteBuffer fileContent = ByteBuffer.allocate(BUFFER_SIZE);
            while (serverChannel.read(fileContent) >= 0) {
                fileContent.flip();
                fc.write(fileContent);
                fileContent.clear();
            }
            fs.close();
            fc.close();
        }
    }

    private void sendMessage(String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        serverChannel.write(buffer);
    }
}
