package auctions.models;

public class Bid {
    private String auctionId;
    private String bidder;
    private double amount;

    // Required for GSON deserialization
    public Bid() {}

    public Bid(String auctionId, String bidder, double amount) {
        this.auctionId = auctionId;
        this.bidder = bidder;
        this.amount = amount;
    }

    // Getters
    public String getAuctionId() { return auctionId; }
    public String getBidder() { return bidder; }
    public double getAmount() { return amount; }

    // Setters
    public void setAuctionId(String id) { this.auctionId = id; }
    public void setBidder(String bidder) { this.bidder = bidder; }
    public void setAmount(double amount) { this.amount = amount; }
}
