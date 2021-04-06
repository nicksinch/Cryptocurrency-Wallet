package bg.sofia.uni.fmi.mjt.crypto.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class CryptoClient {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(512);

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            while (true) {
                System.out.print("=> ");
                String message = scanner.nextLine(); // read a line from the console

                buffer.clear(); // switch to writing mode
                buffer.put(message.getBytes()); // buffer fill
                buffer.flip(); // switch to reading mode
                socketChannel.write(buffer); // buffer drain

                buffer.clear(); // switch to writing mode
                socketChannel.read(buffer); // buffer fill
                buffer.flip(); // switch to reading mode

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, "UTF-8"); // buffer drain

                // if buffer is a non-direct one, is has a wrapped array and we can get it
                //String reply = new String(buffer.array(), 0, buffer.position(), "UTF-8"); // buffer drain

                System.out.println(reply);

                if (reply.equals("[ Disconnected from server ]\n")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("There is a problem with the network communication");
            e.printStackTrace();
        }
    }
}
