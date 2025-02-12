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
                    getList();
                    break;
                case "D":
                    downloadFile();
                    System.out.println("Enter the file name you want to download");
                    String fileName = keyboard.nextLine();
                    break;
            }
        }
        channel.shutdownOutput();
    }
    public getList() {
        SocketChannel channel = SocketChannel.open();

        channel.connect(
                new InetSocketAddress(args[0], serverPort)
        );
        ByteBuffer buffer = ByteBuffer("L");
        channel.write(buffer);
        FileChannel fc = fs.getChannel();
        ByteBuffer fileContent =
                ByteBuffer.allocate(1024);
        while(channel.read(fileContent) >=0) {
            fileContent.flip();
            fc.write(fileContent);
            fileContent.clear();
        }
        fs.close();
        channel.close();
        return "L";
    }
    public downloadFile(fileName){
        ByteBuffer buffer = ByteBuffer.wrap(filename.getBytes());
        channel.write(buffer);
    }

}
