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
            SocketChannel serveChannel =
                    listenChannel.accept();
            ByteBuffer buffer =
                    ByteBuffer.allocate(1024);
            int bytesRead = serveChannel.read(buffer);
            buffer.flip();
            byte[] a = new byte[bytesRead];
            buffer.get(a);
            String fileName = new String(a);
            System.out.println("File name: " + fileName);
            File file = new File("ServerFiles/" + fileName);

            if (!file.exists()) {
                System.out.println("File doesn't exist");
            } else {
                FileInputStream fs =
                        new FileInputStream(file);
                FileChannel fc = fs.getChannel();
                ByteBuffer fileContent =
                        ByteBuffer.allocate(1024);
                int byteRead = 0;
                do {
                    byteRead = fc.read(fileContent);
                    fileContent.flip();
                    serveChannel.write(fileContent);
                    fileContent.clear();
                } while (byteRead >= 0);
                fs.close();
                switch (byteRead) {
                    case "L":
                        getList();
                        break;
                    case "N":
                        downloadFile():
                        break;
                    case "U":
                        uploadFile();
                        break;
                    case "D":
                        deleteFile():
                        break;
                    case "R":
                        renameFile();
                        break;
                }
            }
            serveChannel.close();
        }


        private static void renameFile () {

        }

        private static void deleteFile () {

        }

        private static void uploadFile () {

        }

        private static void downloadFile () {

        }

        private static String getList() {

            return null;
        }
    }
}
