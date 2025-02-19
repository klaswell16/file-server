import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.io.FileOutputStream;

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
            System.out.println("\nEnter a command (L to list, D to download, E to delete):");
            String command = keyboard.nextLine();

            switch (command) {
                case "L":
                    getList(channel);
                    keepGoing = false;
                    break;
                case "D":
                    downloadFile(channel);
                    keepGoing = false;
                    break;

                case "E":
                    deleteFile(channel);
                    keepGoing = false;
                    break;

                case "R":
                    renameFile(channel);
                    keepGoing = false;
                    break;

                default:
                    System.out.println("Not a correct command");
                    keepGoing = false;
                    break;
            }
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

    public static void downloadFile(SocketChannel channel) throws IOException {
        System.out.println("Enter the name of the file you want to download:");
        String fileName = keyboard.nextLine().trim();
        String message = "D" + fileName;
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        channel.write(buffer);

        FileOutputStream fs = new FileOutputStream("ClientFiles/" + fileName, true);
        FileChannel fc = fs.getChannel();
        ByteBuffer fileContent = ByteBuffer.allocate(1024);

        while (channel.read(fileContent) >= 0) {
            fileContent.flip();
            fc.write(fileContent);
            fileContent.clear();
        }
        fs.close();
        fc.close();
    }
    private static void deleteFile(SocketChannel channel) throws IOException {
        System.out.println("Enter the name of the file you want to delete:");
        String fileName = keyboard.nextLine().trim();
        String message = "E" + fileName;
        ByteBuffer buffer =ByteBuffer.wrap(message.getBytes());
        channel.write(buffer);

        ByteBuffer replyBuffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(replyBuffer);
        replyBuffer.flip();
        byte[] a = new byte[bytesRead];
        replyBuffer.get(a);
        String string = new String(a);
        if (string.equals("S")){
            System.out.println("File Deletion was successful");
        }else {
            System.out.println("File Deletion failed");
        }
    }

    private static void renameFile(SocketChannel channel) throws IOException {
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
        if (string.equals("S")){
            System.out.println("File exists what would you like to rename the file to");

        }else {
            System.out.println("File doesn't exist or can't be detected");
        }
    }
}
