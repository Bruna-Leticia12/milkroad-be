package com.milkroad.controller;

import com.milkroad.dto.ClienteRequestDTO;
import com.milkroad.dto.ClienteResponseDTO;
import com.milkroad.entity.Cliente;
import com.milkroad.entity.Perfil;
import com.milkroad.exception.CancelamentoInvalidoException;
import com.milkroad.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // Criar cliente (somente ADMIN pode cadastrar novos clientes)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criarCliente(@RequestBody ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setCelular(dto.getCelular());
        cliente.setTelefone(dto.getTelefone());
        cliente.setLogradouro(dto.getLogradouro());
        cliente.setNumero(dto.getNumero());
        cliente.setBairro(dto.getBairro());
        cliente.setCidade(dto.getCidade());
        cliente.setCep(dto.getCep());
        cliente.setEmail(dto.getEmail());
        cliente.setPerfil(Perfil.CLIENTE); // sempre CLIENTE

        Cliente novoCliente = clienteService.salvarCliente(cliente);

        ClienteResponseDTO response = mapToResponse(novoCliente);
        return ResponseEntity.ok(response);
    }

    // Listar todos (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        List<ClienteResponseDTO> clientes = clienteService.listarClientes()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    // Listar ativos (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ativos")
    public ResponseEntity<List<ClienteResponseDTO>> listarAtivos() {
        List<ClienteResponseDTO> clientes = clienteService.listarClientesAtivos()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    // Listar inativos (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inativos")
    public ResponseEntity<List<ClienteResponseDTO>> listarInativos() {
        List<ClienteResponseDTO> clientes = clienteService.listarClientesInativos()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    // Cliente visualiza os próprios dados
    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/me")
    public ResponseEntity<ClienteResponseDTO> meusDados(Principal principal) {
        Cliente cliente = clienteService.buscarPorEmail(principal.getName());
        return ResponseEntity.ok(mapToResponse(cliente));
    }

    // Cliente cancela sua própria entrega
    @PreAuthorize("hasRole('CLIENTE')")
    @PutMapping("/me/cancelar")
    public ResponseEntity<?> cancelarEntrega(Principal principal) {
        try {
            Cliente cliente = clienteService.cancelarEntregaPorEmail(principal.getName());
            return ResponseEntity.ok(mapToResponse(cliente));
        } catch (CancelamentoInvalidoException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========================
    // Métodos auxiliares
    // ========================
    private ClienteResponseDTO mapToResponse(Cliente c) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(c.getId());
        dto.setNome(c.getNome());
        dto.setCelular(c.getCelular());
        dto.setTelefone(c.getTelefone());
        dto.setLogradouro(c.getLogradouro());
        dto.setNumero(c.getNumero());
        dto.setBairro(c.getBairro());
        dto.setCidade(c.getCidade());
        dto.setCep(c.getCep());
        dto.setEmail(c.getEmail());
        dto.setAtivo(c.isAtivo());
        dto.setPerfil(c.getPerfil().name());
        return dto;
    }
}
