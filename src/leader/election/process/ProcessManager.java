package leader.election.process;

import leader.election.IElection;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class ProcessManager {
    private static int currentPid = 0;
    private static final int CREATE_INTERVAL = 3000;
    private static final int REQUEST_INTERVAL = 2500;
    private static final int INACTIVE_INTERVAL = 8000;
    private static final int INACTIVE_COORDINATOR_INTERVAL = 10000;

    private static final List<Process> activeProcesses =  new ArrayList<>();

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
                            System.out.println("Processo " + process.getPid() + " inativado");
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

    public void doRequests(IElection election) {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(REQUEST_INTERVAL);
                } catch (InterruptedException ignored) {}

                synchronized (lock) {
                    if (!activeProcesses.isEmpty()) {
                        var process = getRandomProcess();
                        int result = process.doRequest();

                        if (result == -1) {
                            System.out.printf("Ring Election iniciada pelo processo %d %n", process.getPid());

                            var newCoordinator = election.doElection(activeProcesses);
                            setCoordinatorProcess(newCoordinator);

                            System.out.printf("Ring Election finalizada, novo Coordenador é: %d %n", newCoordinator.getPid());

                            process.doRequest();
                            System.out.printf("Requisição solicitada pelo processo %d e realizada pelo Processo %d (Coordenador) %n", process.getPid(), newCoordinator.getPid());
                        }

                        System.out.printf("Requisição solicitada pelo processo %d e realizada pelo Processo %d (Coordenador) %n", process.getPid(), getCoordinatorProcess().getPid());
                    }
                }
            }
        }).start();
    }

    private Process getRandomProcess() {
        final int randomIndex = random.nextInt(activeProcesses.size());
        return activeProcesses.get(randomIndex);
    }

    public static Process getCoordinatorProcess() {
        var coordinator = activeProcesses.stream()
                .filter(Process::isCoordinator)
                .toList();
        try {
            return coordinator.getFirst();
        } catch (NoSuchElementException _) {
            return null;
        }
    }

    private void setCoordinatorProcess(Process process) {
        activeProcesses.stream()
                .filter(p -> p.getPid() == process.getPid())
                .toList()
                .getFirst()
            .setCoordinator(true);
    }
}
