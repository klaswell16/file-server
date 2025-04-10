import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    public static Scanner keyboard = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Please provide <serverIP> and <serverPort>");
            return;
        }
        int serverPort = Integer.parseInt(args[1]);
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(args[0], serverPort));

        boolean keepGoing = true;

        while (keepGoing) {
            System.out.println("\nEnter a command (L to list, D to download, E to delete, U to upload, R to rename, Q to exit):");
            String command = keyboard.nextLine();

            switch (command) {
                case "L":
                    getList(channel);
                    break;
                case "D":
                    downloadFile(channel);
                    break;
                case "U":
                    uploadFile(channel);
                    break;
                case "E":
                    deleteFile(channel);
                    break;
                case "R":
                    renameFile(channel);
                    break;
                case "Q":
                    System.out.println("Exiting Client...");
                    keepGoing = false;
                    break;
                default:
                    System.out.println("Not a correct command");
                    break;
            }
        }

        channel.close();
    }

    public static void downloadFile(SocketChannel channel) throws IOException {
        System.out.println("Enter the name of the file you want to download:");
        String fileName = keyboard.nextLine().trim();
        String message = "D" + fileName;
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        channel.write(buffer);

        try {
            System.out.println("Simulating download delay...");
            Thread.sleep(5000); // Simulate network delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FileOutputStream fs = new FileOutputStream("ClientFiles/" + fileName);
        FileChannel fc = fs.getChannel();
        ByteBuffer fileContent = ByteBuffer.allocate(1024);

        while (channel.read(fileContent) >= 0) {
            fileContent.flip();
            fc.write(fileContent);
            fileContent.clear();
        }

        fs.close();
        fc.close();
        System.out.println("Download completed: " + fileName);
    }

    public static void uploadFile(SocketChannel channel) throws IOException {
        System.out.println("Enter the name of the file you want to upload:");
        String fileName = keyboard.nextLine();
        File fileToUpload = new File("ClientFiles/" + fileName);

        if (!fileToUpload.exists()) {
            System.out.println("File doesn't exist.");
            return;
        }

        String message = "U" + fileName;
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        channel.write(buffer);

        ByteBuffer replyBuffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(replyBuffer);
        replyBuffer.flip();
        byte[] replyBytes = new byte[bytesRead];
        replyBuffer.get(replyBytes);
        String serverResponse = new String(replyBytes);

        if (!serverResponse.equals("F")) {
            try {
                System.out.println("Simulating upload delay...");
                Thread.sleep(5000); // Simulate network delay
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try (FileInputStream fs = new FileInputStream(fileToUpload);
                 FileChannel fc = fs.getChannel()) {

                ByteBuffer fileContent = ByteBuffer.allocate(1024);
                int byteRead;
                do {
                    byteRead = fc.read(fileContent);
                    fileContent.flip();
                    channel.write(fileContent);
                    fileContent.clear();
                } while (byteRead > 0);
            }
            System.out.println("File uploaded: " + fileName);
        } else {
            System.out.println("File failed to upload");
        }
    }

    public static void getList(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap("L".getBytes());
        channel.write(buffer);

        ByteBuffer replyBuffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(replyBuffer);
        replyBuffer.flip();
        byte[] a = new byte[bytesRead];
        replyBuffer.get(a);
        System.out.println("Available Files:\n" + new String(a));
    }

    private static void deleteFile(SocketChannel channel) throws IOException {
        System.out.println("Enter the name of the file you want to delete:");
        String fileName = keyboard.nextLine().trim();
        String message = "E" + fileName;
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        channel.write(buffer);

        ByteBuffer replyBuffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(replyBuffer);
        replyBuffer.flip();
        byte[] a = new byte[bytesRead];
        replyBuffer.get(a);
        String string = new String(a);

        if (string.equals("S")) {
            System.out.println("File Deletion was successful");
        } else {
            System.out.println("File Deletion failed");
        }
    }

    private static void renameFile(SocketChannel channel) throws IOException {
        System.out.println("Enter the name of the file you want to rename:");
        String fileName = keyboard.nextLine().trim();
        String message = "R" + fileName;
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        channel.write(buffer);

        ByteBuffer replyBuffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(replyBuffer);
        replyBuffer.flip();
        byte[] a = new byte[bytesRead];
        replyBuffer.get(a);
        String string = new String(a);

        if (string.equals("S")) {
            System.out.println("File exists. Enter the new file name:");
            String renamedFile = keyboard.nextLine().trim();
            ByteBuffer renamedBuffer = ByteBuffer.wrap(renamedFile.getBytes());
            channel.write(renamedBuffer);

            ByteBuffer finalReplyBuffer = ByteBuffer.allocate(1024);
            int byteRead = channel.read(finalReplyBuffer);
            finalReplyBuffer.flip();
            byte[] b = new byte[byteRead];
            finalReplyBuffer.get(b);
            System.out.println(new String(b));
        } else {
            System.out.println("File doesn't exist or can't be detected.");
        }
    }
}
