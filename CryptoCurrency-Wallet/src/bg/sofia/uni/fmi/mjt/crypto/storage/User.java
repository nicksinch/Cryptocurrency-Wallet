package bg.sofia.uni.fmi.mjt.crypto.storage;

public class User {
    String username;
    String password;
    Wallet personalWallet;

    public User(String username, String password, Wallet personalWallet){
        this.username = username;
        this.password = password;
        this.personalWallet = personalWallet;
    }

}
