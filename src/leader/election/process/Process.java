package leader.election.process;

public class Process {
    private final int pid;
    private boolean isCoordinator;

    public Process(int pid) {
        this.pid = pid;
        this.isCoordinator = false;
    }

    public Process(int pid, boolean isCoordinator) {
        this.pid = pid;
        this.isCoordinator = isCoordinator;
    }

    public int getPid() {
        return pid;
    }

    public boolean isCoordinator() {
        return isCoordinator;
    }

    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }

    @Override
    public String toString() {
        return "Processo " + pid + (isCoordinator ? " (Coordenador)" : "");
    }
}
