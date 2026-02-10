package auctions.utils;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class DFHelper {
    public static DFAgentDescription[] searchAuctions(Agent agent) throws FIPAException {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("auction");
        template.addServices(serviceDescription);
        return DFService.search(agent, template);
    }
}
