package leader.election;

import leader.election.process.ProcessManager;

public class Main {
    public static void main(String[] args) {
        ProcessManager pm = new ProcessManager();
        pm.createProcess();
    }
}
