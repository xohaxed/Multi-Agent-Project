package agents;

import gui.MultiCriteriaBuyerFrame;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class GUIAgent extends Agent {
    private MultiCriteriaBuyerFrame gui;

    @Override
    protected void setup() {
        gui = new MultiCriteriaBuyerFrame();
        gui.setVisible(true);
        gui.appendLog("GUI Agent started");

        // Handle GUI events
        gui.addRequestListener(e -> sendToBuyer("REQUEST_OFFERS"));
        gui.addMigrateContainerListener(e -> sendToBuyer("MIGRATE_CONTAINER:NewContainer"));
        gui.addMigratePlatformListener(e -> sendToBuyer("MIGRATE_PLATFORM:192.168.56.2"));

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        String content = msg.getContent();
                        if (content == null) return;
                        if (content.startsWith("OFFER|")) {
                            String[] p = content.split("\\|");
                            if (p.length >= 6) {
                                gui.addOffer(p[1],
                                    Double.parseDouble(p[2]),
                                    Double.parseDouble(p[3]),
                                    Double.parseDouble(p[4]),
                                    Double.parseDouble(p[5]));
                                gui.appendLog("Offer from " + p[1] + ": $" + p[2]
                                    + ", Q=" + p[3] + ", D=" + p[4] + " days, Score=" + p[5]);
                            }
                        } else if (content.startsWith("WINNER|")) {
                            String[] p = content.split("\\|");
                            if (p.length >= 3) {
                                gui.setBestSupplier(p[1], Double.parseDouble(p[2]));
                                gui.setStatus("Winner: " + p[1]);
                            }
                        } else if ("CLEAR_OFFERS".equals(content)) {
                            gui.clearOffers();
                        } else {
                            gui.appendLog(content);
                        }
                    } catch (Exception ex) {
                        gui.appendLog("Error processing message: " + ex.getMessage());
                    }
                } else {
                    block();
                }
            }
        });
        
    }

    private void sendToBuyer(String content) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("buyer", AID.ISLOCALNAME));
        msg.setContent(content);
        send(msg);
        gui.setStatus("Sent request: " + content); // Update status after sending
    }
    
}