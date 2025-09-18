package com.milkroad.service;

import com.milkroad.entity.Cliente;
import com.milkroad.exception.CancelamentoInvalidoException;
import com.milkroad.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Cliente salvarCliente(Cliente cliente) {
        // senha = últimos 4 dígitos do celular
        if (cliente.getCelular() != null && cliente.getCelular().length() >= 4) {
            String senha = cliente.getCelular().substring(cliente.getCelular().length() - 4);
            cliente.setSenha(passwordEncoder.encode(senha)); // <<--- criptografa senha
        }
        return clienteRepository.save(cliente);
    }

    // Listar todos
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    // Listar ativos
    public List<Cliente> listarClientesAtivos() {
        return clienteRepository.findByAtivo(true);
    }

    // Listar inativos
    public List<Cliente> listarClientesInativos() {
        return clienteRepository.findByAtivo(false);
    }

    // Buscar cliente por email (para autenticação ou dados do perfil)
    public Cliente buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com email: " + email));
    }

    // Cancelar entrega de um cliente (pela própria conta)
    public Cliente cancelarEntregaPorEmail(String email) {
        Cliente cliente = buscarPorEmail(email);

        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();

        // Só bloqueia cancelamento de entrega do dia atual depois das 07h
        if (agora.isAfter(LocalTime.of(7, 0))) {
            throw new CancelamentoInvalidoException(
                    "Cancelamento da entrega do dia atual só é permitido até as 07h."
            );
        }

        cliente.setAtivo(false); // marca indisponível
        return clienteRepository.save(cliente);
    }
}
