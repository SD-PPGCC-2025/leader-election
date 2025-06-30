package leader.election.process;

import java.util.ArrayList;
import java.util.List;

public class ProcessManager {
    private static int currentPid = 0;
    private static final int CREATE_INTERVAL = 3000;

    public static List<Process> activeProcesses =  new ArrayList<>();

    private final Object lock = new Object();

    public void createProcess() {
        new Thread(() -> {
            while (true) {
                synchronized (lock) {
                    if (activeProcesses.isEmpty())
                        activeProcesses.add(new Process(++currentPid, true));
                    else
                        activeProcesses.add(new Process(++currentPid));
                    System.out.printf("Process %d created\n", currentPid);
                }

                try {
                    Thread.sleep(CREATE_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
