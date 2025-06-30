package leader.election.process;

import leader.election.ring.RingElection;

import java.util.*;

public class ProcessManager {
    private static int currentPid = 0;
    private static final int CREATE_INTERVAL = 3000;
    private static final int REQUEST_INTERVAL = 2500;
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
                        System.out.printf("Processo %d (Coordernador) criado %n".formatted(currentPid));
                    }
                    else {
                        activeProcesses.add(new Process(++currentPid));
                        System.out.printf("Processo %d criado %n".formatted(currentPid));
                    }
                }

                try {
                    Thread.sleep(CREATE_INTERVAL);
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    public void inactiveProcess() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(INACTIVE_INTERVAL);
                } catch (InterruptedException ignored) {}

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
                } catch (InterruptedException ignored) {}

                synchronized (lock) {
                    var coordinator = getCoordinatorProcess();
                    if (coordinator != null) {
                        activeProcesses.remove(coordinator);
                        System.out.println(coordinator + " inativado");
                    }
                }
            }
        }).start();
    }

    public void doRequests() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(REQUEST_INTERVAL);
                } catch (InterruptedException ignored) {}

                synchronized (lock) {
                    if (!activeProcesses.isEmpty()) {
                        var coordinator = getCoordinatorProcess();
                        if (coordinator != null)
                            /* imagine um processamento qualquer aqui */
                            System.out.printf("Requisição realizada pelo Processo %d (Coordenador) %n", coordinator.getPid());

                        else new RingElection().doElection();
                    }
                }
            }
        });
    }

    private Process getRandomProcess() {
        final int randomIndex = random.nextInt(activeProcesses.size());
        return activeProcesses.get(randomIndex);
    }

    private Process getCoordinatorProcess() {
        var coordinator = activeProcesses.stream()
                .filter(Process::isCoordinator)
                .toList();
        try {
            return coordinator.getFirst();
        } catch (NoSuchElementException _) {
            return null;
        }
    }
}
