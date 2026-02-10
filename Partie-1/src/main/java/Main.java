
import javax.swing.SwingUtilities;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class Main {
    public static void main(String[] args) {
        
        // Start JADE Platform in another thread
        new Thread(() -> {
            System.out.println("[1/2] Starting JADE platform...");
            try {
                Runtime rt = Runtime.instance();
                Profile p = new ProfileImpl(true);
                p.setParameter(Profile.MAIN_HOST, "localhost");
                p.setParameter(Profile.MAIN_PORT, "1099");

                AgentContainer container = rt.createMainContainer(p);

                // Start agents after short delay
                Thread.sleep(2000);
                startAgent(container, "seller1", "auctions.agents.SellerAgent", 500.0);
                startAgent(container, "buyer1", "auctions.agents.BuyerAgent", 800.0);
                startAgent(container, "buyer2", "auctions.agents.BuyerAgent", 1000.0);
                startAgent(container, "buyer3", "auctions.agents.BuyerAgent", 1200.0);
                startAgent(container, "buyer4", "auctions.agents.BuyerAgent", 1500.0);
                startAgent(container, "buyer5", "auctions.agents.BuyerAgent", 900.0);
                startAgent(container, "gui", "auctions.agents.AuctionGUIAgent");

            } catch (Exception e) {}
        }).start();

        // Start GUI in EDT thread
        SwingUtilities.invokeLater(() -> {
            System.out.println("[3/3] Preparing GUI environment...");
        });
    }

    private static void startAgent(AgentContainer container,
                                   String name,
                                   String className,
                                   Object... args) throws Exception {
        AgentController ac = container.createNewAgent(name, className, args);
        ac.start();
        System.out.println("[SUCCESS] Started agent: " + name);
    }
}
