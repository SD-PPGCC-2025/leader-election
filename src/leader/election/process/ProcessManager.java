package leader.election.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProcessManager {
    private static int currentPid = 0;
    private static final int CREATE_INTERVAL = 3000;
    private static final int INACTIVE_INTERVAL = 8000;
    private static final int INACTIVE_COORDINATOR_INTERVAL = 10000;

    public static final List<Process> activeProcesses =  new ArrayList<>();

    private final Object lock = new Object();
    private final Random random = new Random();

    public void createProcess() {
        new Thread(() -> {
            while (true) {
                synchronized (lock) {
                    if (activeProcesses.isEmpty()) {
                        activeProcesses.add(new Process(++currentPid, true));
                        System.out.printf("Processo " + currentPid + " (Coordernador) criado\n");
                    }
                    else {
                        activeProcesses.add(new Process(++currentPid));
                        System.out.printf("Processo " + currentPid + " criado\n");
                    }
                }

                try {
                    Thread.sleep(CREATE_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void inactiveProcess() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(INACTIVE_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (lock) {
                    if (!activeProcesses.isEmpty()) {
                        final Process process = getRandomProcess();
                        if (!process.isCoordinator()) {
                            activeProcesses.remove(process);
                            System.out.println("Process " + process.getPid() + " inativado");
                        }
                    }
                }
            }
        }).start();
    }

    public void inactiveCoordinator() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(INACTIVE_COORDINATOR_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (lock) {
                    var coordinators = activeProcesses.stream()
                            .filter(Process::isCoordinator)
                            .toList();
                    if (!coordinators.isEmpty()) {
                        final Process coordinator = coordinators.getFirst();
                        activeProcesses.remove(coordinator);
                        System.out.println(coordinator + " inativado");
                    }
                }
            }
        }).start();
    }

    private Process getRandomProcess() {
        final int randomIndex = random.nextInt(activeProcesses.size());
        return activeProcesses.get(randomIndex);
    }
}
