package auctions.agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class SellerAgent extends Agent {
    private double currentHighest;
    private AID guiAID;

    @Override
    protected void setup() {
        // Initialize state
        currentHighest = 0.0;
        // Register as "seller" service
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("seller");
            sd.setName(getLocalName() + "-service");
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (FIPAException fe) {}

        // Main behavior: listen for NEW_AUCTION or BID
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg == null) {
                    block();
                    return;
                }

                String[] parts = msg.getContent().split("\\|", 3);
                String type = parts[0];

                switch (type) {
                    case "NEW_AUCTION":
                        handleNewAuction(msg.getSender(), parts[1], parts[2]);
                        break;

                    case "BID":
                        handleBid(parts[1], parts[2]);
                        break;

                    default:
                        // ignore unknown
                }
            }
        });
    }

    private void handleNewAuction(AID sender, String priceStr, String itemName) {
        // Reset state
        currentHighest = Double.parseDouble(priceStr);
        guiAID         = sender;

        // Build broadcast message
        String content = String.format("NEW_AUCTION|%s|%s", priceStr, itemName);

        // Send to GUI (only sender) AND to all buyers
        // 1) GUI
        ACLMessage guiMsg = new ACLMessage(ACLMessage.INFORM);
        guiMsg.addReceiver(guiAID);
        guiMsg.setContent(content);
        send(guiMsg);

        // 2) All buyers
        for (AID buyer : findAll("buyer")) {
            ACLMessage buyerMsg = new ACLMessage(ACLMessage.INFORM);
            buyerMsg.addReceiver(buyer);
            buyerMsg.setContent(content);
            send(buyerMsg);
        }

        System.out.printf("[Seller] Started auction \"%s\" at $%s%n", itemName, priceStr);
    }

    private void handleBid(String bidder, String amountStr) {
        double amount = Double.parseDouble(amountStr);
        if (amount > currentHighest) {
            currentHighest = amount;
            // Broadcast new high bid to GUI + all buyers
            String content = String.format("BID|%s|%s", bidder, amountStr);

            // GUI
            ACLMessage guiMsg = new ACLMessage(ACLMessage.INFORM);
            guiMsg.addReceiver(guiAID);
            guiMsg.setContent(content);
            send(guiMsg);

            // Buyers
            for (AID buyer : findAll("buyer")) {
                ACLMessage buyerMsg = new ACLMessage(ACLMessage.INFORM);
                buyerMsg.addReceiver(buyer);
                buyerMsg.setContent(content);
                send(buyerMsg);
            }

            System.out.printf("[Seller] New high bid by %s: $%.2f%n", bidder, amount);
        }
    }

    /** Utility: query DF for all agents offering the given service type. */
    private List<AID> findAll(String serviceType) {
        List<AID> result = new ArrayList<>();
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(serviceType);
            template.addServices(sd);

            // No limit on results
            SearchConstraints sc = new SearchConstraints();
            sc.setMaxResults(Long.MAX_VALUE);

            DFAgentDescription[] matches = DFService.search(this, template, sc);
            for (DFAgentDescription d : matches) {
                result.add(d.getName());
            }
        } catch (FIPAException e) {}
        return result;
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {}
        System.out.println("[Seller] Shutting down.");
    }
}
