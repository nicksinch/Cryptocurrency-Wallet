package bg.sofia.uni.fmi.mjt.crypto.storage;

import java.nio.channels.SocketChannel;

public interface Storage {
    String registerUser(String username, String password);

    String loginUser(String username, String password, SocketChannel clientChannel);

    String logoutUser(String usrename, SocketChannel clientChannel);

    String depositMoney(double amount, SocketChannel clientChannel);

    String buyCryptoCurrency(String cryptoID, int amount, SocketChannel clientChannel);

    String displayListOfferings();

    String sellCryptoCurrency(String cryptoID, int sellingAmount , SocketChannel clientChannel);

    String getWalletSummary(SocketChannel clientChannel);

    String getWalletOverallSummary(SocketChannel clientChannel);

}
