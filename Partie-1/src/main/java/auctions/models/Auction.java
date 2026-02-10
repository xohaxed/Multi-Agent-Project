package auctions.models;

import java.util.UUID;

public class Auction {
    private String winningBidder;  // Add this field
    private long endTime;  // Add this field
    
        public Auction(String item, double startPrice, double reservePrice) {
            this.id = UUID.randomUUID().toString();
            this.item = item;
            this.currentPrice = startPrice;
            this.reservePrice = reservePrice;
            this.endTime = System.currentTimeMillis() + 60000; // 60 seconds
        }
    
        public boolean hasEnded() {
            return System.currentTimeMillis() > endTime;
        }
    
        // Add getter for winner
        public String getWinningBidder() {
            return winningBidder != null ? winningBidder : "No winner";
        }
    
    
    public void applyBid(Bid bid) {
        if (bid.getAmount() > currentPrice) {
            currentPrice = bid.getAmount();
            winningBidder = bid.getBidder();
        }
    }

    private String id;
    private String item;
    private double currentPrice;
    private double reservePrice;

    public Auction() {} // Needed for GSON

    // Getters and Setters
    public String getId() { return id; }
    public String getItem() { return item; }
    public double getCurrentPrice() { return currentPrice; }
    public double getReservePrice() { return reservePrice; }
    public void setCurrentPrice(double price) { this.currentPrice = price; }
    
    public boolean isValid() {
        return id != null && !id.isEmpty() && 
               item != null && !item.isEmpty() &&
               currentPrice > 0 && reservePrice > 0;
    }
}
