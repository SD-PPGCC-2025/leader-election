package leader.election;

import leader.election.process.ProcessManager;
import leader.election.ring.RingElection;

public class Main {
    public static void main(String[] args) {
        final ProcessManager pm = new ProcessManager();
        pm.createProcess();
        pm.inactiveProcess();
        pm.inactiveCoordinator();
        pm.doRequests(new RingElection());
    }
}
