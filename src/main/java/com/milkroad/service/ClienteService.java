package com.milkroad.service;

import com.milkroad.entity.Cliente;
import com.milkroad.exception.CancelamentoInvalidoException;
import com.milkroad.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntregaService entregaService;

    public Cliente salvarCliente(Cliente cliente) {
        // senha = últimos 4 dígitos do celular
        if (cliente.getCelular() != null && cliente.getCelular().length() >= 4) {
            String senha = cliente.getCelular().substring(cliente.getCelular().length() - 4);
            cliente.setSenha(passwordEncoder.encode(senha));
        }

        Cliente salvo = clienteRepository.save(cliente);

        // Gerar entregas automáticas (segunda a sexta)
        entregaService.gerarEntregasAutomaticas(salvo);

        return salvo;
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public List<Cliente> listarClientesAtivos() {
        return clienteRepository.findByAtivo(true);
    }

    public List<Cliente> listarClientesInativos() {
        return clienteRepository.findByAtivo(false);
    }

    public Cliente buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com email: " + email));
    }

    public Cliente cancelarEntregaPorEmail(String email) {
        throw new CancelamentoInvalidoException("Use o endpoint de /api/entregas/{id}/cancelar para cancelar entregas.");
    }
}
