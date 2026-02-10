package agents;

import java.util.Locale;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class MultiCriteriaSellerAgent extends Agent {
    private final Random random = new Random();

    @Override
    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.CFP) {
                    sendOffer(msg.getSender());
                } else {
                    block();
                }
            }
        });
    }

    private void sendOffer(AID buyer) {
        double price = 50 + random.nextDouble() * 50;
        double quality = 50 + random.nextDouble() * 50;
        double delivery = random.nextDouble() * 20;
        String content = String.format(Locale.US, "%.2f;%.2f;%.2f", price, quality, delivery);

        ACLMessage offer = new ACLMessage(ACLMessage.PROPOSE);
        offer.addReceiver(buyer);
        offer.setContent(content);
        send(offer);
    }
}
