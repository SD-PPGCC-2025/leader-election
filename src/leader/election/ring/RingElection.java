package leader.election.ring;

import leader.election.IElection;
import leader.election.process.Process;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RingElection implements IElection {
    private final LinkedList<Integer> pidList = new LinkedList<>();

    public Process doElection(List<Process> activeProcesses) {
        System.out.println("Ring Election iniciada");

        // Percorre a lista de processos ativos para avaliar quais são aptos à eleição
        activeProcesses.forEach(process -> consultProcess(process.getPid()));

        // Da lista de processos aptos, obtém o de maior ID
        int pid = Collections.max(pidList);
        System.out.println("Ring Election finalizada");

        return activeProcesses.stream()
                .filter(process -> process.getPid() == pid)
                .toList()
                .getFirst();
    }

    private void consultProcess(int pid) {
        /* Imagine todas as validações pra ver se o processo pode ser coordenador */
        pidList.add(pid);
    }
}
