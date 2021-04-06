package bg.sofia.uni.fmi.mjt.crypto.storage;

import bg.sofia.uni.fmi.mjt.crypto.exceptions.CryptoClientException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class InMemoryStorage implements Storage {

    private HttpClient cryptoHttpClient;
    private final String TOKEN = "199C3D90-CE0D-471F-BECB-26F16C19D4EB";
    private final String API = "https://rest.coinapi.io/v1/assets";

    private final Path databasePath = Paths.get("/home/nicksinch/IdeaProjects/CryptoCurrency-Wallet/src/bg/sofia/uni/fmi/mjt/crypto/storage/userDataBase.json");

    private Map<String, Wallet> userAssets;
    private Map<String, String> userAccounts;
    private Map<SocketChannel, String> currentlyLoggedInUsers;

    public InMemoryStorage(Reader jsonDatabase) {
        this.userAssets = new HashMap<>();
        this.userAccounts = new HashMap<>();
        this.currentlyLoggedInUsers = new HashMap<>();
        this.cryptoHttpClient = HttpClient.newBuilder()
                .build();

        try (BufferedReader bufferedReader = new BufferedReader(jsonDatabase)) {
            StringBuilder json = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine())!=null){
                json.append(line);
            }
            List<User> loadedUsers = new Gson().fromJson(json.toString(), new TypeToken<List<User>>() {}.getType());
            for(User u : loadedUsers){
                this.userAccounts.put(u.username, u.password);
                this.userAssets.put(u.username, u.personalWallet);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String registerUser(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Either username or password is null");
        }

        if(this.userAccounts.containsKey(username)) {
            return String.format("Username %s already taken!", username);
        }

        String hashedPassword = encryptPassword(password);

        User newUser = new User(username, hashedPassword, new Wallet());
        writeToDatabase(newUser);

        this.userAccounts.put(username, hashedPassword);
        this.userAssets.put(username, new Wallet());
        return String.format("Username %s successfully registered!", username);
    }

    public String loginUser(String username, String password, SocketChannel clientChannel) {
        if (!this.userAccounts.containsKey(username)) {
            return String.format("Username %s doesn't exist!", username);
        }
        String hashedPassword = encryptPassword(password);

        if (this.userAccounts.containsKey(username) && !this.userAccounts.get(username).equals(hashedPassword)) {
            return String.format("Wrong password for user %s !", username);
        }
        if (this.currentlyLoggedInUsers.get(clientChannel) != null
                && this.currentlyLoggedInUsers.get(clientChannel).equals(username)) {
            return String.format("User %s already logged in!", username);
        }

        this.userAccounts.put(username, password);
        this.userAssets.put(username, new Wallet());
        this.currentlyLoggedInUsers.put(clientChannel, username);
        return "Successfully logged in!";
    }

    public String logoutUser(String username, SocketChannel clientChannel){
        if (!this.currentlyLoggedInUsers.containsKey(clientChannel) ||
                !this.currentlyLoggedInUsers.get(clientChannel).equals(username)) {
            return String.format("[ You are not logged in ]%n");
        }
        this.currentlyLoggedInUsers.remove(clientChannel);
        return String.format("[ Successfully logged out ]%n");
    }

    public String depositMoney(double amount, SocketChannel clientChannel){
        if(!currentlyLoggedInUsers.containsKey(clientChannel)){
            return String.format("[ You are not logged in. You need to log in to deposit money! ]%n");
        }
        else if(amount <= 0){
            return "Amount of money to deposit should be positive!";
        }
        String username = currentlyLoggedInUsers.get(clientChannel);
        Wallet userWallet = this.userAssets.get(username);
        userWallet.depositMoney(amount);
        this.userAssets.put(username, userWallet);
        String password = userAccounts.get(username);
        User updatedUser = new User(username, password, userWallet);
        writeToDatabase(updatedUser);
        return String.format("[ %s USD deposit successfully! You now have $ %s !]%n", amount, userWallet.getMoneyAmount());
    }

    public String buyCryptoCurrency(String cryptoID, int amount, SocketChannel clientChannel){
        if(!currentlyLoggedInUsers.containsKey(clientChannel)){
            return String.format("[ You are not logged in. You need to be logged in to buy a crypto! ]%n");
        }

        Asset crypto = takeCurrentCryptoStatus(cryptoID);
        double cryptoPrice = crypto.priceUSD();

        String username = currentlyLoggedInUsers.get(clientChannel);
        Wallet userWallet = this.userAssets.get(username);

        if(userWallet.getMoneyAmount() < cryptoPrice * amount){
            return String.format("[ You don't have enough amount to buy this crypto! ]%n");
        }

        userWallet.withdrawAmount(cryptoPrice * amount);

        for(int i = 0;i < amount;++i) {
            userWallet.addAsset(crypto);
        }

        String password = this.userAccounts.get(username);
        User updatedUser = new User(username, password, userWallet);
        writeToDatabase(updatedUser);

        return String.format("[ Congratulations! You have bought %s amount of %s successfully! ]%n", amount,
                crypto.assetName());
    }

    public String displayListOfferings() {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(API))
                .setHeader("X-CoinAPI-Key", TOKEN)
                .build();

        HttpResponse<String> response;
        Gson gson = new Gson();
        try {
            response = cryptoHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new CryptoClientException("Retrieving info about the cryptocurrencies went wrong", e);
        }
        //TODO: Add exception
//        if (response.statusCode() == 404) {
//            throw new LocationNotFoundException("Can't find the city");
//        }
        StringBuilder offerings = new StringBuilder();
        Asset[] assets = gson.fromJson(response.body(), Asset[].class);
        Stream<Asset> streamAssets = Arrays.stream(assets);
        Predicate<Asset> isCrypto = a -> a.isCrypto() == 1;
        streamAssets.filter(isCrypto).skip(0).limit(5).forEach(asset -> offerings.append(asset.Info()));
        return offerings.toString();
    }

    public String sellCryptoCurrency(String cryptoID, int sellingAmount, SocketChannel clientChannel) {
        if (!currentlyLoggedInUsers.containsKey(clientChannel)) {
            return String.format("[ You are not logged in. You need to be logged in to sell a crypto! ]%n");
        }
        double currentCryptoPrice = takeCurrentCryptoStatus(cryptoID).priceUSD();

        String username = currentlyLoggedInUsers.get(clientChannel);
        Wallet userWallet = this.userAssets.get(username);

        double profit = 0.0;
        int soldAmount = 0;
        double restoreMoney = 0.0;
        for (int i = 0;i < sellingAmount; ++i) {
            Asset a = userWallet.removeAsset(cryptoID);
            if (a != null) {
                profit += currentCryptoPrice - a.priceUSD();
                restoreMoney += profit + a.priceUSD();
                soldAmount += 1;
            }
            else{
                userWallet.realizeProfit(restoreMoney);
                return String.format("[ You don't have this amount of %s! You successfully sold %s of %s and " +
                                "realized $ %s ]%n", cryptoID, soldAmount, cryptoID, profit);
            }
        }
        userWallet.realizeProfit(restoreMoney);
        return String.format("[ You successfully sold %s amount of %s and have realized $ %s of profit ! ]%n",
                soldAmount, cryptoID, profit);
    }

    public Asset takeCurrentCryptoStatus(String cryptoID) {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(API + "/" + cryptoID))
                .setHeader("X-CoinAPI-Key",TOKEN)
                .build();

        HttpResponse<String> response;
        Gson gson = new Gson();
        try {
            response = cryptoHttpClient.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            throw new CryptoClientException("Retrieving info about the cryptocurrencies went wrong", e);
        }

        Asset[] asset = gson.fromJson(response.body(), Asset[].class);

        return asset[0];
    }

    public String getWalletSummary(SocketChannel clientChannel) {
        if(!this.currentlyLoggedInUsers.containsKey(clientChannel)){
            return String.format("[ You need to be logged in to see your wallet summary! ]%n");
        }
        String username = currentlyLoggedInUsers.get(clientChannel);
        Wallet userWallet = this.userAssets.get(username);

        StringBuilder summary = new StringBuilder();
        if(userWallet.investments().size() == 0){
            return String.format("Currently, you don't have any active investments!%n You have $%s to spend.", userWallet.getMoneyAmount());
        }
        userWallet.investments().forEach(asset -> summary.append(asset.Info()));
        summary.append(String.format("You also have $%s available.%n ", userWallet.getMoneyAmount()));
        return summary.toString();
    }

    public String getWalletOverallSummary(SocketChannel clientChannel) {
        String username = currentlyLoggedInUsers.get(clientChannel);
        Wallet userWallet = this.userAssets.get(username);

        StringBuilder overallSummary = new StringBuilder();

        double currentTotalProfit = 0.0;
        for (Asset a: userWallet.investments()){
            Asset currentAssetStatus = takeCurrentCryptoStatus(a.assetID());
            overallSummary.append(String.format("You bought %s for %s - now its %s",
                    a.assetID(), a.priceUSD(),  currentAssetStatus.priceUSD()));
            currentTotalProfit += currentAssetStatus.priceUSD() - a.priceUSD();
        }
        overallSummary.append(String.format("Your total profit if you sell everything now: %s%n", currentTotalProfit));

        return overallSummary.toString();
    }

    private String encryptPassword(String password) {
        String hashedPassword = null;

        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(password.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest();
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            hashedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace(); //TODO: Don't print stack trace
        }
        return hashedPassword;
    }

    private void writeToDatabase(User u) {

        String json = null;
        try {
            json = new String(Files.readAllBytes(Paths.get("/home/nicksinch/IdeaProjects/CryptoCurrency-Wallet/src/bg/sofia/uni/fmi/mjt/crypto/storage/userDataBase.json")));
        }catch(Exception e){}
        try(Writer wr = new FileWriter("/home/nicksinch/IdeaProjects/CryptoCurrency-Wallet/src/bg/sofia/uni/fmi/mjt/crypto/storage/userDataBase.json")){
            writeToJson(wr, json, u);
        }
        catch (Exception e){}
    }

    private void writeToJson(Writer jsonFile, String json, User u) {
        Gson gson = new GsonBuilder().create();
        boolean userAlreadyExists = false;
        try(BufferedWriter bufferedWriter = new BufferedWriter(jsonFile)){
            List<User> users = new Gson().fromJson(json, new TypeToken<List<User>>() {}.getType());
            for (User existingUser : users) {
                if(existingUser.username.equals(u.username)){
                    existingUser.personalWallet = u.personalWallet;
                    userAlreadyExists = true;
                }
            }
            if(!userAlreadyExists) {
                users.add(u);
            }
            gson.toJson(users, bufferedWriter);
        }catch(Exception e){}
    }
}