package bg.sofia.uni.fmi.mjt.crypto.command;

import bg.sofia.uni.fmi.mjt.crypto.exceptions.CryptoClientException;
import bg.sofia.uni.fmi.mjt.crypto.storage.Asset;
import bg.sofia.uni.fmi.mjt.crypto.storage.Storage;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.SocketChannel;

public class CommandExecutor {

    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT = "Invalid count of arguments: \"%s\" expects %d arguments. Example: \"%s\"";

    private static final String LIST = "list-offerings";
    private static final String DEPOSIT = "deposit-money";
    private static final String REGISTER = "register";
    private static final String LOGIN = "login";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final String LOGOUT = "logout";
    private static final String WALLET_SUMMARY = "get-wallet-summary";
    private static final String WALLET_OVERALL_SUMMARY = "get-wallet-overall-summary";
    private static final String HELP = "help";


    private Storage storage;

    public CommandExecutor(Storage storage) {
        this.storage = storage;
    }

    public String execute(Command cmd, SocketChannel clientChannel) {
        return switch (cmd.command()) {
            case REGISTER -> registerUser(cmd.arguments());
            case LOGIN -> loginUser(cmd.arguments(), clientChannel);
            case LOGOUT -> logoutUser(cmd.arguments(), clientChannel);
            case DEPOSIT -> depositMoney(cmd.arguments(), clientChannel);
            case BUY -> buyCrypto(cmd.arguments(), clientChannel);
            case SELL -> sellCrypto(cmd.arguments(), clientChannel);
            case WALLET_SUMMARY -> walletSummary(clientChannel);
            case WALLET_OVERALL_SUMMARY  -> walletOverallSummary(clientChannel);
            case LIST -> displayListOfferings();
            case HELP -> help();
            default -> "Unknown command";
        };
    }

    private String displayListOfferings() {
        return storage.displayListOfferings();
    }

    private String registerUser(String[] args) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, REGISTER, 2, REGISTER + " Misho password1234");
        }

        String username = args[0];
        String password = args[1];

        return storage.registerUser(username, password);
    }

    private String loginUser(String[] args, SocketChannel clientChannel) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, LOGIN, 2, LOGIN + " Misho password1234");
        }

        String username = args[0];
        String password = args[1];

        return storage.loginUser(username, password, clientChannel);
    }

    private String logoutUser(String[] args, SocketChannel clientChannel) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, LOGOUT, 1, LOGOUT + " Misho");
        }

        if(clientChannel == null){
            throw new IllegalArgumentException("ClientChannel is null");
        }
        return storage.logoutUser(args[0], clientChannel);
    }

    private String depositMoney(String[] args, SocketChannel clientChannel){
        if(args.length != 1){
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, DEPOSIT, 1, DEPOSIT + "300.00");
        }

        return storage.depositMoney(Double.parseDouble(args[0]), clientChannel);
    }

    private String buyCrypto(String[] args, SocketChannel clientChannel){
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, BUY, 2, BUY + " BTC 5");
        }
        String cryptoID = args[0];
        int amount = Integer.parseInt(args[1]);
        return storage.buyCryptoCurrency(cryptoID, amount, clientChannel);
    }

    private String sellCrypto(String[] args, SocketChannel clientChannel) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SELL, 2, SELL + " BTC 3");
        }

        String cryptoID = args[0];
        int amount = Integer.parseInt(args[1]);

        return storage.sellCryptoCurrency(cryptoID, amount, clientChannel);
    }

    private String walletSummary(SocketChannel clientChannel){
        if(clientChannel == null){
            throw new IllegalArgumentException("Client SocketChannel is null");
        }
        return storage.getWalletSummary(clientChannel);
    }

    private String walletOverallSummary (SocketChannel clientChannel) {
        if(clientChannel == null){
            throw new IllegalArgumentException("Client SocketChannel is null");
        }
        return storage.getWalletOverallSummary(clientChannel);
    }

    private String help(){
        return String.format("%s <username> <password>%n------------------------------%n" +
                "%s <username> <password>%n------------------------------%n" +
                "%s <username> - logs you out (if you are currently logged in)%n------------------------------%n" +
                "%s <amount>%n------------------------------%n" +
                "%s - to list available cryptocurrencies%n------------------------------%n" +
                "%s <offering_code> <amount>%n------------------------------%n" +
                "%s <offering_code> <amount>%n------------------------------%n" +
                "%s - to list all active investments and available money%n------------------------------%n" +
                "%s - to list the overall profit/loss of every active investment%n------------------------------%n"
        ,REGISTER, LOGIN, LOGOUT, DEPOSIT, LIST, BUY, SELL, WALLET_SUMMARY, WALLET_OVERALL_SUMMARY);
    }
}
