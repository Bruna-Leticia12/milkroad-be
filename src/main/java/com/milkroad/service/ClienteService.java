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

    // ✅ Listar todos os clientes
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    // ✅ Listar somente clientes ativos
    public List<Cliente> listarClientesAtivos() {
        return clienteRepository.findByAtivo(true);
    }

    // ✅ Listar somente clientes inativos
    public List<Cliente> listarClientesInativos() {
        return clienteRepository.findByAtivo(false);
    }

    // ⚠️ Cancelamento de entrega (a regra ideal seria em EntregaService)
    public Cliente cancelarEntrega(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();

        // Só bloqueia se for a entrega de hoje e já passou das 7h
        if (agora.isAfter(LocalTime.of(7, 0))) {
            throw new CancelamentoInvalidoException(
                    "Cancelamento de entrega no dia atual só é permitido até as 07h."
            );
        }

        cliente.setAtivo(false); // marcar como indisponível
        return clienteRepository.save(cliente);
    }
}
