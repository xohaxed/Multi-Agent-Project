package auctions.agents;

import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class BuyerAgent extends Agent {
    private AID sellerAID;
    private double lastPrice     = 0.0;
    private String lastBidder    = null;
    private double maxBudget     = Double.MAX_VALUE;
    private TickerBehaviour biddingBehaviour;
    private final Random rng     = new Random();

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    protected void setup() {
        // 1) Parse optional budget argument
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            try {
                maxBudget = Double.parseDouble(args[0].toString());
            } catch (NumberFormatException e) {
                System.err.println("[BUYER] Invalid budget; using no limit.");
            }
        }

        // 2) Register with DF as a "buyer"
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("buyer");
        sd.setName(getLocalName() + "-service");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // 3) Identify the seller
        sellerAID = new AID("seller1", AID.ISLOCALNAME);

        // 4) Listen for NEW_AUCTION and BID messages
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String[] parts = msg.getContent().split("\\|", 3);
                    switch (parts[0]) {
                        case "NEW_AUCTION":
                            // Reset price & bidder, start bidding
                            lastPrice  = Double.parseDouble(parts[1]);
                            lastBidder = null;
                            restartBidding();
                            break;
                        case "BID":
                            // Update lastPrice & lastBidder
                            String bidder = parts[1];
                            double amt    = Double.parseDouble(parts[2]);
                            if (amt > lastPrice) {
                                lastPrice  = amt;
                                lastBidder = bidder;
                            }
                            break;
                        default:
                            // ignore unknown
                    }
                } else {
                    block();
                }
            }
        });
    }

    /** (Re)start the periodic bidding behaviour with improved logic. */
    private void restartBidding() {
        // Remove any existing bidding behaviour
        if (biddingBehaviour != null) {
            removeBehaviour(biddingBehaviour);
        }

        // Every 2 seconds, consider placing a new bid
        biddingBehaviour = new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                double nextBid = Math.round(lastPrice * 1.05 * 100.0) / 100.0;

                // Logic: 
                //  - Must be under or equal to budget
                //  - Must not already be the highest bidder
                //  - 50% random chance to skip this tick
                if (nextBid <= maxBudget
                    && !getLocalName().equals(lastBidder)
                    && rng.nextDouble() < 0.5) {

                    ACLMessage bidMsg = new ACLMessage(ACLMessage.PROPOSE);
                    bidMsg.addReceiver(sellerAID);
                    bidMsg.setContent("BID|" + getLocalName() + "|" + nextBid);
                    send(bidMsg);
                    System.out.printf("[%s] Placed bid: $%.2f%n", getLocalName(), nextBid);
                }
                // If over budget, stop bidding
                else if (nextBid > maxBudget) {
                    stop();
                    System.out.printf("[%s] Budget exhausted. Halting bids.%n", getLocalName());
                }
            }
        };

        addBehaviour(biddingBehaviour);
    }

    @Override
    protected void takeDown() {
        // Deregister from DF and clean up
        if (biddingBehaviour != null) {
            removeBehaviour(biddingBehaviour);
        }
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {}
        System.out.println("[BUYER] " + getLocalName() + " terminating.");
    }
}
