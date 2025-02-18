import java.io.File;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;


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
            String header = new String(a);
            System.out.println("Header: " + header);

            switch (header) {
                case "L":

                    File directoryPath = new File("ServerFiles/");
                    File[] filesList = directoryPath.listFiles();
                    if(filesList != null){
                        List<String> fileNames = new ArrayList<>();

                        for(File file : filesList){
                            fileNames.add(file.getName());

                        }
                        String fileNamesString = String.join("\n", fileNames);
                        ByteBuffer replyBuffer = ByteBuffer.wrap(fileNamesString.getBytes());
                        serverChannel.write(replyBuffer);
                    }else{
                        ByteBuffer replyBuffer = ByteBuffer.wrap("No files found".getBytes());
                        serverChannel.write(replyBuffer);
                    }
                    break;
                case "D":
                    File file = new File("ServerFiles/+")



            }
            serverChannel.close();
        }
    }
}

