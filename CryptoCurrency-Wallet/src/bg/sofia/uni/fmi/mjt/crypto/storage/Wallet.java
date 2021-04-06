package bg.sofia.uni.fmi.mjt.crypto.storage;

import java.util.*;
import java.util.function.Predicate;

public class Wallet {


    private List<Asset> assets; // stores the state that the asset was bought at
    private double moneyAmount;

    public Wallet() {
        assets = new ArrayList<>();
        moneyAmount = 0.0;
    }

    public void addAsset(Asset asset){
        this.assets.add(asset);
    }

    public double getMoneyAmount(){
        return this.moneyAmount;
    }

    public void depositMoney(double amount){
        this.moneyAmount += amount;
    }

    public void withdrawAmount(double amount){
        this.moneyAmount -= amount;
    }

    public double realizeProfit(double newAmount){
        return moneyAmount += newAmount;
    }

    public Asset removeAsset(String cryptoID){
        Predicate<Asset> filterID = m -> m.assetID().equals(cryptoID);
        Asset crypto = assets.stream().filter(filterID).findFirst().orElse(null);
        this.assets.remove(crypto);
        return crypto;
    }

    public List<Asset> investments(){
        return this.assets;
    }
}
