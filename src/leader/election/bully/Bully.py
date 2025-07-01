import time
import random
import threading

class Node:
    def __init__(self, node_id, total_nodes, nodes_list):
        self.node_id = node_id
        self.total_nodes = total_nodes
        self.is_coordinator = False
        self.is_alive = True
        self.coorgdinator = -1
        self.nodes_list = nodes_list 

    def __str__(self):
        return f"Node {self.node_id}"

    def set_coordinator(self, coordinator_id):
        self.coordinator = coordinator_id
        if self.node_id == coordinator_id:
            self.is_coordinator = True
            print(f"Node {self.node_id} é o NOVO COORDENADOR.")
        else:
            self.is_coordinator = False
            print(f"Node {self.node_id} reconhece Node {coordinator_id} como COORDENADOR.")

    def fail(self):
        self.is_alive = False
        print(f"Node {self.node_id} FALHOU.")

    def recover(self):
        self.is_alive = True
        print(f"Node {self.node_id} RECUPEROU.")
        self.start_election()

    def send_election_message(self, target_node_id):
        if self.is_alive:
            print(f"Node {self.node_id} envia ELEIÇÃO para Node {target_node_id}")
            return self.nodes_list[target_node_id].receive_election_message(self.node_id)
        return False 

    def send_ok_message(self, target_node_id):
        if self.is_alive:
            print(f"Node {self.node_id} envia OK para Node {target_node_id}")
            self.nodes_list[target_node_id].receive_ok_message(self.node_id)
            return True
        return False

    def send_coordinator_message(self, target_node_id):
        if self.is_alive:
            print(f"Node {self.node_id} envia COORDENADOR para Node {target_node_id}")
            self.nodes_list[target_node_id].set_coordinator(self.node_id)
            return True
        return False

    def receive_election_message(self, sender_node_id):
        if not self.is_alive:
            print(f"Node {self.node_id} está INATIVO e não pode responder à eleição de Node {sender_node_id}.")
            return False 

        print(f"Node {self.node_id} recebe ELEIÇÃO de Node {sender_node_id}")

        self.send_ok_message(sender_node_id)

        if self.node_id > sender_node_id:
            print(f"Node {self.node_id} tem ID maior que {sender_node_id}, iniciando sua própria eleição.")
            self.start_election()
        return True

    def receive_ok_message(self, sender_node_id):
        if not self.is_alive:
            return 

        print(f"Node {self.node_id} recebe OK de Node {sender_node_id}")
        self.election_in_progress = False

    def start_election(self):
        if not self.is_alive:
            print(f"Node {self.node_id} está INATIVO e não pode iniciar uma eleição.")
            return

        print(f"Node {self.node_id} INICIA uma ELEIÇÃO.")
        self.election_in_progress = True

        higher_nodes_responded = False
        for i in range(self.node_id + 1, self.total_nodes):
            if self.send_election_message(i):
                higher_nodes_responded = True
            time.sleep(0.1) 

        if not higher_nodes_responded:
            self.set_coordinator(self.node_id)
            for i in range(self.total_nodes):
                if i != self.node_id:
                    self.send_coordinator_message(i)
        else:
            print(f"Node {self.node_id} recebeu respostas de nós com ID maior. Aguardando coordenador.")
            time.sleep(2)
            if self.coordinator != -1 and self.coordinator > self.node_id and self.nodes_list[self.coordinator].is_alive:
                print(f"Node {self.node_id} reconhece o novo coordenador: Node {self.coordinator}")
            elif not self.is_coordinator: 
                 print(f"Node {self.node_id} não viu um novo coordenador ser eleito. Pode re-iniciar a eleição se necessário.")


def simulate_bully_algorithm(num_nodes):
    nodes = [Node(i, num_nodes, None) for i in range(num_nodes)]

    for node in nodes:
        node.nodes_list = nodes

    print("--- Início da Simulação do Algoritmo Bully ---")

    print("\nFase 1: Início do sistema e eleição inicial")
    nodes[0].start_election()
    time.sleep(1) 

    print("\n--- Estado Atual ---")
    for node in nodes:
        print(f"{node}: Coordenador: {node.coordinator}, Vivo: {node.is_alive}, É Coordenador: {node.is_coordinator}")

    print("\nFase 2: Simulação de falha do coordenador (Nó 4)")
    nodes[num_nodes - 1].fail() # 

    fault_detector_node_id = random.randint(0, num_nodes - 2) 
    print(f"Node {fault_detector_node_id} detecta falha do coordenador e INICIA ELEIÇÃO.")
    nodes[fault_detector_node_id].start_election()
    time.sleep(2) 

    print("\n--- Estado Atual Após Falha e Nova Eleição ---")
    for node in nodes:
        print(f"{node}: Coordenador: {node.coordinator}, Vivo: {node.is_alive}, É Coordenador: {node.is_coordinator}")

    print("\nFase 3: Simulação de recuperação de um nó (Nó 0) e potencial nova eleição")
    nodes[0].recover()
    time.sleep(2) 

    print("\n--- Estado Atual Após Recuperação ---")
    for node in nodes:
        print(f"{node}: Coordenador: {node.coordinator}, Vivo: {node.is_alive}, É Coordenador: {node.is_coordinator}")

    print(f"\nFase 4: Simulação de falha do NOVO coordenador (Nó {nodes[num_nodes - 2].node_id})")
    nodes[num_nodes - 2].fail()

    fault_detector_node_id = random.randint(0, num_nodes - 3)
    print(f"Node {fault_detector_node_id} detecta falha do novo coordenador e INICIA ELEIÇÃO.")
    nodes[fault_detector_node_id].start_election()
    time.sleep(2)

    print("\n--- Estado Final da Simulação ---")
    for node in nodes:
        print(f"{node}: Coordenador: {node.coordinator}, Vivo: {node.is_alive}, É Coordenador: {node.is_coordinator}")


if __name__ == "__main__":
    num_nodes = 5 
    simulate_bully_algorithm(num_nodes)