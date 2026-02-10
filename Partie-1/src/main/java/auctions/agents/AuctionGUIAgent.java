package auctions.agents;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import auctions.gui.AuctionFrame;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class AuctionGUIAgent extends Agent {
    private AuctionFrame frame;
    private static final String SELLER = "seller1";

    @Override
    protected void setup() {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("[GUI AGENT] Headless mode – exiting.");
            doDelete();
            return;
        }

        // Build the GUI
        EventQueue.invokeLater(() -> {
            frame = new AuctionFrame();
            frame.setTitle("Auction GUI - " + getLocalName());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // GUI → Agent wiring
            frame.setStartAuctionListener(this::triggerNewAuction);
            frame.setBidListener(this::sendBid);
        });

        // Listen for incoming ACL messages
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    processMessage(msg);        // <— call renamed method
                } else {
                    block();
                }
            }
        });
    }

    /** Called when user clicks “Start New Auction”. */
    private void triggerNewAuction() {
        String item = JOptionPane.showInputDialog(frame, "Item name:");
        if (item == null || item.isBlank()) return;

        String p = JOptionPane.showInputDialog(frame, "Starting price:");
        try {
            double price = Double.parseDouble(p);
            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.addReceiver(new AID(SELLER, AID.ISLOCALNAME));
            req.setContent("NEW_AUCTION|" + price + "|" + item);
            send(req);
            System.out.println("[GUI AGENT] Sent NEW_AUCTION|" + price + "|" + item);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid price.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Called when user places a bid in the GUI. */
    private void sendBid(String bidder, double amount) {
        ACLMessage bid = new ACLMessage(ACLMessage.PROPOSE);
        bid.addReceiver(new AID(SELLER, AID.ISLOCALNAME));
        bid.setContent("BID|" + bidder + "|" + amount);
        send(bid);
        System.out.println("[GUI AGENT] Sent BID|" + bidder + "|" + amount);
    }

    /** Process any incoming ACLMessage from the seller/auctioneer. */
    private void processMessage(ACLMessage msg) {
        String[] parts = msg.getContent().split("\\|", 3);
        SwingUtilities.invokeLater(() -> {
            switch (parts[0]) {
                case "NEW_AUCTION":
                    double startPrice = Double.parseDouble(parts[1]);
                    frame.startNewAuction(parts[2], startPrice);
                    JOptionPane.showMessageDialog(
                        frame,
                        "Auction started: " + parts[2] + "\nStarting at $" + startPrice,
                        "New Auction",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    break;
                case "BID":
                    frame.updateBid(parts[1], Double.parseDouble(parts[2]));
                    break;
                default:
                    System.err.println("[GUI AGENT] Unknown message: " + msg.getContent());
            }
        });
    }

    @Override
    protected void takeDown() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) frame.dispose();
        });
        System.out.println("[GUI AGENT] Terminated.");
    }
}
