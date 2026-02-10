import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Main {
    public static void main(String[] args) {
        try {
            Runtime runtime = Runtime.instance();
            Profile profile = new ProfileImpl();
            AgentContainer mainContainer = runtime.createMainContainer(profile);

            // Start GUI Agent
            AgentController guiAgent = mainContainer.createNewAgent("gui", "agents.GUIAgent", null);
            guiAgent.start();

            // Start seller agents
            String[] sellerNames = {"seller1", "seller2", "seller3"};
            for (String name : sellerNames) {
                AgentController seller = mainContainer.createNewAgent(name, "agents.MultiCriteriaSellerAgent", null);
                seller.start();
            }

            // Start buyer agent
            AgentController buyer = mainContainer.createNewAgent("buyer", "agents.MultiCriteriaBuyerAgent", null);
            buyer.start();

            System.out.println("Auction system started.");
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}