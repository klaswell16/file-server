import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
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
            if (bytesRead>0){
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

                    case "E":
                        deleteFile(serverChannel, argument);
                        break;

                    case "R":
                        renameFile(serverChannel, argument);
                        break;

                    case "U":
                        uploadFile(serverChannel, argument);
                        break;

                    default:
                        ByteBuffer errorBuffer = ByteBuffer.wrap("Invalid command".getBytes());
                        serverChannel.write(errorBuffer);
                        System.out.println("Invalid command received: " + header);
                        break;
                }
                serverChannel.close();
            }else {
                ByteBuffer errorBuffer = ByteBuffer.wrap("Invalid command".getBytes());
                serverChannel.write(errorBuffer);
                System.out.println("Invalid command received: ");
            }

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
            ByteBuffer errorBuffer = ByteBuffer.wrap("F".getBytes());
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

    public static void deleteFile(SocketChannel serverChannel, String fileName) throws IOException {
        File fileToDelete = new File("ServerFiles/" + fileName);

        if (fileToDelete.delete()){
            ByteBuffer successBuffer = ByteBuffer.wrap("S".getBytes());
            serverChannel.write(successBuffer);
            System.out.println("File deleted: " + fileName);
        }else{
            ByteBuffer errorBuffer = ByteBuffer.wrap("F".getBytes());
            serverChannel.write(errorBuffer);
            System.out.println("File failed to delete: " + fileName);
        }
    }

    public static void renameFile(SocketChannel serverChannel, String fileName) throws IOException {
        File fileToRename = new File("ServerFiles/" + fileName);

        if (!fileToRename.exists()) {
            ByteBuffer errorBuffer = ByteBuffer.wrap("F".getBytes());
            serverChannel.write(errorBuffer);
            System.out.println("File doesn't exist: " + fileName);
            return;
        }

        ByteBuffer successBuffer = ByteBuffer.wrap("S".getBytes());
        serverChannel.write(successBuffer);

        ByteBuffer renamedFileBuffer = ByteBuffer.allocate(1024);
        int bytesRead = serverChannel.read(renamedFileBuffer);
        renamedFileBuffer.flip();
        byte[] b = new byte[bytesRead];
        renamedFileBuffer.get(b);
        String renamed = new String(b).trim();

        File newFile = new File("ServerFiles/" + renamed);
        if (fileToRename.renameTo(newFile)) {
            ByteBuffer renamedBuffer = ByteBuffer.wrap("File was renamed".getBytes());
            serverChannel.write(renamedBuffer);
            System.out.println("File renamed to: " + renamed);
        } else {
            ByteBuffer errorBuffer = ByteBuffer.wrap("F".getBytes());
            serverChannel.write(errorBuffer);
            System.out.println("Failed to rename file: " + fileName);
        }
    }
    public static void uploadFile(SocketChannel serverChannel, String fileName) throws IOException {

        FileOutputStream fs = new FileOutputStream("ServerFiles/" + fileName, true);
        FileChannel fc = fs.getChannel();

        File uploadedFile = new File("ServerFiles/" + fileName);
        if (!uploadedFile.exists()){
            ByteBuffer errorBuffer = ByteBuffer.wrap("F".getBytes());
            serverChannel.write(errorBuffer);
            System.out.println("File doesn't exist: " + fileName);
        }else {
            ByteBuffer successBuffer = ByteBuffer.wrap("S".getBytes());
            serverChannel.write(successBuffer);

            ByteBuffer fileContent = ByteBuffer.allocate(1024);
            while (serverChannel.read(fileContent) >= 0) {
                fileContent.flip();
                fc.write(fileContent);
                fileContent.clear();
            }
            fs.close();
            fc.close();
        }
    }

}
