import bg.sofia.uni.fmi.mjt.crypto.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.crypto.server.Server;
import bg.sofia.uni.fmi.mjt.crypto.storage.InMemoryStorage;
import bg.sofia.uni.fmi.mjt.crypto.storage.Storage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CryptoWalletTester {

    private static Thread serverStarterThread;
    private static Server cryptoWalletManagerServer;
    private static Path databasePath = Path.of("/home/nicksinch/IdeaProjects/CryptoCurrency-Wallet/src/bg/sofia/uni/fmi/mjt/crypto/storage/userDataBase.json");

    @BeforeClass
    public static void setUpBeforeClass() {
        serverStarterThread = new Thread(() -> {
            try (Reader database = new FileReader(String.valueOf(databasePath))) {
                Storage st = new InMemoryStorage(database);
                CommandExecutor commandExec = new CommandExecutor(st);
                Server cryptoServer = new Server(9999, commandExec);
                cryptoWalletManagerServer = cryptoServer;
                cryptoServer.start();
            } catch (Exception e) {
                System.out.println("An error has occurred");
                e.printStackTrace();
            }
        });
        serverStarterThread.start();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        cryptoWalletManagerServer.stop();
        // Wake up the server so that it can exit
        serverStarterThread.interrupt();
    }

    @Test
    public void registerUserSuccessfullyTest() {
        List<String> requests = new ArrayList<>(Collections.singleton("register Zdravko password"));
        List<String> responses = sendReceiveRequest(requests);
        assertEquals(String.format("Username Zdravko successfully registered!%n"), responses.get(0));
    }


    private List<String> sendReceiveRequest(List<String> requests) {
        List<String> responses = new ArrayList<>();
        try (Socket socket = new Socket("localhost", 9999);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
             System.out.println("Client " + socket + " connected to server");

            for (String request : requests) {
                out.println(request);

                String response = String.format("%s%n", in.readLine());
                responses.add(response);
            }
            return responses;
        } catch (IOException e) {
            System.out.println("An error with socket has occurred " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
