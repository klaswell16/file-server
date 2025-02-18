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

            int bytesRead = serverChannel.read(buffer);
            buffer.flip();
            byte[] a = new byte[bytesRead];
            buffer.get(a);
            String header = new String(a).trim();
            System.out.println("Header: " + header);

            String command = header.substring(0, 1);
            String argument = header.substring(1).trim();

            switch (command) {
                case "L":
                    listFiles(serverChannel);
                    break;

                case "D":
                    downloadFile(serverChannel, argument);
                    break;

                default:
                    ByteBuffer errorBuffer = ByteBuffer.wrap("Invalid command".getBytes());
                    serverChannel.write(errorBuffer);
                    System.out.println("Invalid command received: " + header);
                    break;
            }
            serverChannel.close();
        }
    }

    public static void listFiles(SocketChannel serverChannel) throws IOException {
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
    }

    public static void downloadFile(SocketChannel serverChannel, String fileName) throws IOException {
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
}
