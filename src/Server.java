import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;


public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(3000));

        while (true) {
            SocketChannel serverChannel = listenChannel.accept();
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // Read the header
            int bytesRead = serverChannel.read(buffer);
            buffer.flip();
            byte[] a = new byte[bytesRead];
            buffer.get(a);
            String header = new String(a).trim();
            System.out.println("Header: " + header);

            switch (header) {
                case "L":
                    // List files
                    File directoryPath = new File("ServerFiles/");
                    File[] filesList = directoryPath.listFiles();
                    if (filesList != null) {
                        List<String> fileNames = new ArrayList<>();
                        for (File file : filesList) {
                            fileNames.add(file.getName());
                        }
                        String fileNamesString = String.join("\n", fileNames);
                        ByteBuffer replyBuffer = ByteBuffer.wrap(fileNamesString.getBytes());
                        serverChannel.write(replyBuffer);
                    } else {
                        ByteBuffer replyBuffer = ByteBuffer.wrap("No files found".getBytes());
                        serverChannel.write(replyBuffer);
                    }
                    break;

                case "D":
                    // Download file
                    buffer.clear(); // Clear buffer before reusing it
                    bytesRead = serverChannel.read(buffer);
                    buffer.flip();
                    byte[] b = new byte[bytesRead];
                    buffer.get(b);
                    String fileName = new String(b).trim();
                    File fileToDownload = new File("ServerFiles/" + fileName);

                    if (!fileToDownload.exists()) {
                        ByteBuffer errorBuffer = ByteBuffer.wrap("File doesn't exist".getBytes());
                        serverChannel.write(errorBuffer);
                        System.out.println("File doesn't exist: " + fileName);
                    } else {
                        try (FileInputStream fs = new FileInputStream(fileToDownload);
                             FileChannel fc = fs.getChannel()) {

                            ByteBuffer fileContent = ByteBuffer.allocate(1024);
                            int byteRead;
                            while ((byteRead = fc.read(fileContent)) > 0) {
                                fileContent.flip();
                                serverChannel.write(fileContent);
                                fileContent.clear();
                            }
                        }
                    }
                    break;

                default:
                    ByteBuffer errorBuffer = ByteBuffer.wrap("Invalid command".getBytes());
                    serverChannel.write(errorBuffer);
                    System.out.println("Invalid header received: " + header);
                    break;
            }
            serverChannel.close();
        }
    }
}
