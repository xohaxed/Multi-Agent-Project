package auctions.gui;

import javax.swing.JOptionPane;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

public class GUIHelper {
    // No ControllerException handling needed here
    public static void updateAuctionState(Agent agent, String item, double price) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(agent.getAMS());
        msg.setContent("AUCTION_UPDATE|" + item + "|" + price);
        agent.send(msg); // This doesn't throw ControllerException
    }

    // Keep ControllerException handling here
    public static void displayMessage(Agent agent, String content) {
        try {
            AgentController ac = agent.getContainerController().getAgent(agent.getLocalName());
            if (ac != null) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(agent.getAID());
                msg.setContent(content);
                agent.send(msg);
            }
        } catch (ControllerException ce) {
            handleControllerException(ce);
        }
    }

    private static void handleControllerException(ControllerException ce) {
        System.err.println("Agent controller error:");
        ce.printStackTrace();
        JOptionPane.showMessageDialog(null,
            "Agent communication error: " + ce.getMessage(),
            "Controller Error",
            JOptionPane.ERROR_MESSAGE);
    }
}