import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Please provide <serverIP> and <serverPort>");
            return;
        }
        int serverPort = Integer.parseInt(args[1]);
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter a command");
        String command = keyboard.nextLine();
        SocketChannel channel = SocketChannel.open();

        channel.connect(
                new InetSocketAddress(args[0], serverPort)
        );
        while (true) {
            switch (command) {
                case "L":
                    getList(channel);
                    break;
                case "D":
                    System.out.println("Enter the name of the file you wan to delete:\n");
                    String fileName = keyboard.nextLine();
                    downloadFile(channel);
                    break;
                default:
                    System.out.println("Not a correct command");
                    break;
            }
        }
    }
    public static void getList(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap("L".getBytes());
        channel.write(buffer);

        ByteBuffer replyBuffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(replyBuffer);
        channel.close();
        replyBuffer.flip();
        byte[] a = new byte[bytesRead];
        replyBuffer.get(a);
        System.out.println(new String(a));
        channel.close();
    }
    public static void downloadFile(SocketChannel channel, String fileName) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap("D".getBytes());
        channel.write(buffer);

        ByteBuffer fileContent = ByteBuffer.wrap(fileName.getBytes());
        channel.write(fileContent);

        ByteBuffer replyBuffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(replyBuffer);
        channel.close();
        replyBuffer.flip();
        byte[] a = new byte[bytesRead];
        replyBuffer.get(a);
        System.out.println(new String (a));
        channel.close();
    }


}
