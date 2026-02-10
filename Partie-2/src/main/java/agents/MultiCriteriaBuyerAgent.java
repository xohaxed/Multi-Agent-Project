package agents;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

public class MultiCriteriaBuyerAgent extends Agent {
    private final List<String> sellers = Arrays.asList("seller1", "seller2", "seller3");
    private final Map<String, double[]> offers = new HashMap<>();
    private final double wQuality = 0.4, wPrice = 0.4, wDelivery = 0.2;

    @Override
    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String content = msg.getContent();
                    if ("REQUEST_OFFERS".equals(content)) {
                        startNegotiation();
                    } else if (content.startsWith("MIGRATE")) {
                        handleMigration(content);
                    } else if (msg.getPerformative() == ACLMessage.PROPOSE) {
                        recordOffer(msg);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void startNegotiation() {
        offers.clear();
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        sellers.forEach(s -> cfp.addReceiver(new jade.core.AID(s, jade.core.AID.ISLOCALNAME)));
        send(cfp);
        addBehaviour(new WakerBehaviour(this, 2000) {
            protected void onWake() {
                evaluateOffers();
            }
        });
    }

    private void recordOffer(ACLMessage msg) {
        String[] parts = msg.getContent().split(";");
        double price = Double.parseDouble(parts[0]);
        double quality = Double.parseDouble(parts[1]);
        double delivery = Double.parseDouble(parts[2]);
        offers.put(msg.getSender().getLocalName(), new double[]{price, quality, delivery});
        sendGUI("Received offer from " + msg.getSender().getLocalName() + ": Price=" + price + ", Quality=" + quality + ", Delivery=" + delivery);
    }
    

    private void evaluateOffers() {
        if (offers.isEmpty()) {
            sendGUI("No offers received from suppliers.");
            return;
        }

        // Find min/max for proper normalization
        double minP = Double.MAX_VALUE, maxP = 0;
        double minQ = Double.MAX_VALUE, maxQ = 0;
        double minD = Double.MAX_VALUE, maxD = 0;
        for (double[] v : offers.values()) {
            minP = Math.min(minP, v[0]); maxP = Math.max(maxP, v[0]);
            minQ = Math.min(minQ, v[1]); maxQ = Math.max(maxQ, v[1]);
            minD = Math.min(minD, v[2]); maxD = Math.max(maxD, v[2]);
        }

        sendGUI("CLEAR_OFFERS");

        String best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        int bestRow = -1;
        int row = 0;

        for (var e : offers.entrySet()) {
            double price = e.getValue()[0];
            double quality = e.getValue()[1];
            double delivery = e.getValue()[2];

            // Normalize: price lower=better, quality higher=better, delivery lower=better
            double np = (maxP == minP) ? 1.0 : (maxP - price) / (maxP - minP);
            double nq = (maxQ == minQ) ? 1.0 : (quality - minQ) / (maxQ - minQ);
            double nd = (maxD == minD) ? 1.0 : (maxD - delivery) / (maxD - minD);
            double score = wQuality * nq + wPrice * np + wDelivery * nd;

            // Send structured offer data to GUI (Locale.US to ensure dots for decimals)
            sendGUI(String.format(Locale.US, "OFFER|%s|%.2f|%.1f|%.1f|%.3f", e.getKey(), price, quality, delivery, score));

            if (score > bestScore) {
                bestScore = score;
                best = e.getKey();
                bestRow = row;
            }
            row++;
        }

        if (best != null) {
            ACLMessage acc = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            acc.addReceiver(new jade.core.AID(best, jade.core.AID.ISLOCALNAME));
            send(acc);
            sendGUI(String.format(Locale.US, "WINNER|%s|%.3f", best, bestScore));
            sendGUI("Best supplier: " + best + " (score=" + String.format(Locale.US, "%.3f", bestScore) + ")");
        } else {
            sendGUI("No valid offers to evaluate.");
        }
    }

    private void handleMigration(String content) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                try {
                    String[] p = content.split(":");
                    if ("MIGRATE_CONTAINER".equals(p[0]) && p.length > 1) {
                        String containerName = p[1];
                        sendGUI("Creating container '" + containerName + "' and migrating...");
                        // Create the target container first
                        jade.core.Runtime rt = jade.core.Runtime.instance();
                        jade.core.ProfileImpl prof = new jade.core.ProfileImpl();
                        prof.setParameter(jade.core.Profile.CONTAINER_NAME, containerName);
                        prof.setParameter(jade.core.Profile.MAIN_HOST, "localhost");
                        prof.setParameter(jade.core.Profile.MAIN_PORT, "1099");
                        rt.createAgentContainer(prof);
                        // Now move the agent
                        jade.core.ContainerID cid = new jade.core.ContainerID(containerName, null);
                        doMove(cid);
                        sendGUI("Migrated to container '" + containerName + "' successfully.");
                    } else if ("MIGRATE_PLATFORM".equals(p[0]) && p.length > 1) {
                        String host = p[1];
                        sendGUI("Creating remote container on " + host + " and migrating...");
                        jade.core.Runtime rt = jade.core.Runtime.instance();
                        jade.core.ProfileImpl prof = new jade.core.ProfileImpl();
                        prof.setParameter(jade.core.Profile.MAIN_HOST, host);
                        prof.setParameter(jade.core.Profile.MAIN_PORT, "1099");
                        prof.setParameter(jade.core.Profile.CONTAINER_NAME, "RemoteContainer");
                        rt.createAgentContainer(prof);
                        jade.core.ContainerID cid = new jade.core.ContainerID("RemoteContainer", null);
                        doMove(cid);
                        sendGUI("Migrated to platform at " + host + " successfully.");
                    } else {
                        sendGUI("Unknown migration command: " + content);
                    }
                } catch (Exception e) {
                    sendGUI("Migration failed: " + e.getMessage());
                }
            }
        });
    }
    
    
    

    private void sendGUI(String msg) {
        ACLMessage m = new ACLMessage(ACLMessage.INFORM);
        m.addReceiver(new jade.core.AID("gui", jade.core.AID.ISLOCALNAME));
        m.setContent(msg);
        send(m);
    }
}
