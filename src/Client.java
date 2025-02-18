import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.io.FileOutputStream;

public class Client {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Please provide <serverIP> and <serverPort>");
            return;
        }
        int serverPort = Integer.parseInt(args[1]);
        Scanner keyboard = new Scanner(System.in);
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(args[0], serverPort));
        boolean keepGoing = true;

        while (keepGoing) {
            System.out.println("\nEnter a command (L to list, D to download):");
            String command = keyboard.nextLine();

            switch (command) {
                case "L":
                    getList(channel);
                    keepGoing = false;
                    break;
                case "D":
                    System.out.println("Enter the name of the file you want to download:");
                    String fileName = keyboard.nextLine().trim();
                    downloadFile(channel, fileName);
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

    public static void downloadFile(SocketChannel channel, String fileName) throws IOException {
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
}
