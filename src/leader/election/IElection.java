package leader.election;

import leader.election.process.Process;

import java.util.List;

public interface IElection {
    Process doElection(List<Process> activeProcesses);
}
