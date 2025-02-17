import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel listenChannel =
                ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(3000));
        while (true) {
            SocketChannel serverChannel =
                    listenChannel.accept();
            ByteBuffer buffer =
                    ByteBuffer.allocate(1024);
            int bytesRead = serverChannel.read(buffer);
            buffer.flip();
            byte[] a = new byte[bytesRead];
            buffer.get(a);
            String fileName = new String(a);
            System.out.println("File name: " + fileName);
            switch (fileName) {
                case "L":
                    ByteBuffer replyBuffer = ByteBuffer.allocate(1024);
                    replyBuffer = ByteBuffer.wrap("Yes".getBytes());
                    serverChannel.write(replyBuffer);
                    replyBuffer.clear();
                    break;
            }
            serverChannel.close();
        }
    }
}

