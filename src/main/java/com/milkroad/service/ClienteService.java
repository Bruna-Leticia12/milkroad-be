package com.milkroad.service;

import com.milkroad.dto.ClienteRequestDTO;
import com.milkroad.entity.Cliente;
import com.milkroad.exception.CancelamentoInvalidoException;
import com.milkroad.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        cliente.setAtivo(cliente.isAtivo());

        Cliente salvo = clienteRepository.save(cliente);

        if (salvo.isAtivo()) {
            entregaService.gerarEntregasAutomaticas(salvo);
        }

        return salvo;
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll()
                .stream()
                .filter(c -> c.getPerfil() != null && !"ADMIN".equalsIgnoreCase(c.getPerfil().name()))
                .sorted(Comparator.comparing(c -> c.getNome().toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Cliente> listarClientesAtivos() {
        return clienteRepository.findByAtivo(true)
                .stream()
                .filter(c -> c.getPerfil() != null && !"ADMIN".equalsIgnoreCase(c.getPerfil().name()))
                .sorted(Comparator.comparing(c -> c.getNome().toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Cliente> listarClientesInativos() {
        return clienteRepository.findByAtivo(false)
                .stream()
                .filter(c -> c.getPerfil() != null && !"ADMIN".equalsIgnoreCase(c.getPerfil().name()))
                .sorted(Comparator.comparing(c -> c.getNome().toLowerCase()))
                .collect(Collectors.toList());
    }

    public Cliente buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com email: " + email));
    }

    public Cliente cancelarEntregaPorEmail(String email) {
        throw new CancelamentoInvalidoException("Use o endpoint de /api/entregas/{id}/cancelar para cancelar entregas.");
    }

    public Cliente atualizarCliente(Long id, ClienteRequestDTO dto) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        boolean estavaAtivo = existente.isAtivo();

        existente.setNome(dto.getNome());
        existente.setCelular(dto.getCelular());
        existente.setTelefone(dto.getTelefone());
        existente.setLogradouro(dto.getLogradouro());
        existente.setNumero(dto.getNumero());
        existente.setBairro(dto.getBairro());
        existente.setCidade(dto.getCidade());
        existente.setCep(dto.getCep());
        existente.setEmail(dto.getEmail());

        if (dto.getAtivo() != null) {
            existente.setAtivo(dto.getAtivo());
        }

        Cliente atualizado = clienteRepository.save(existente);

        if (!estavaAtivo && atualizado.isAtivo()) {
            entregaService.gerarEntregasAutomaticas(atualizado);
        }

        if (estavaAtivo && !atualizado.isAtivo()) {
            entregaService.desativarEntregasFuturas(atualizado.getId());
        }

        return atualizado;
    }

    public void desativarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
        cliente.setAtivo(false);
        clienteRepository.save(cliente);

        entregaService.desativarEntregasFuturas(id);
    }

    public Cliente buscarPorNome(String nome) {
        return clienteRepository.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com o nome: " + nome));
    }
}