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


}
