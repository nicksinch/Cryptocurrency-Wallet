package bg.sofia.uni.fmi.mjt.crypto;

import bg.sofia.uni.fmi.mjt.crypto.client.CryptoClient;
import bg.sofia.uni.fmi.mjt.crypto.command.Command;
import bg.sofia.uni.fmi.mjt.crypto.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.crypto.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.crypto.exceptions.CryptoClientException;
import bg.sofia.uni.fmi.mjt.crypto.server.Server;
import bg.sofia.uni.fmi.mjt.crypto.storage.Asset;
import bg.sofia.uni.fmi.mjt.crypto.storage.InMemoryStorage;
import bg.sofia.uni.fmi.mjt.crypto.storage.Storage;
import bg.sofia.uni.fmi.mjt.crypto.storage.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {
        Path databasePath = Path.of("/home/nicksinch/IdeaProjects/CryptoCurrency-Wallet/src/bg/sofia/uni/fmi/mjt/crypto/storage/userDataBase.json");
        try(Reader database = new FileReader(String.valueOf(databasePath))){
            Storage st = new InMemoryStorage(database);
            CommandExecutor cmEx = new CommandExecutor(st);
            Server s = new Server(7777, cmEx);
            s.start();
        }
    }
}
