package bg.sofia.uni.fmi.mjt.crypto.storage;

public class Asset {
    private String asset_id;
    private String name;
    private int type_is_crypto;
    private double price_usd;

    public Asset(String assetID, String assetName, int typeIsCrypto, double priceUSD) {
        this.asset_id = assetID;
        this.name = assetName;
        this.type_is_crypto = typeIsCrypto;
        this.price_usd = priceUSD;
    }

    public String assetID() {
        return this.asset_id;
    }

    public String assetName() {
        return this.name;
    }

    public int isCrypto() {
        return this.type_is_crypto;
    }

    public double priceUSD() {
        return this.price_usd;
    }

    public String Info() {
        return String.format("ID: %s %nName: %s %nBought for: $%s %n----------%n", this.assetID(), this.assetName(), this.priceUSD());
    }
}
