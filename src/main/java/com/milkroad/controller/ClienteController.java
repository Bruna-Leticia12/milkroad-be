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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criarCliente(@RequestBody ClienteRequestDTO dto) {

        Cliente cliente = Cliente.builder()
                .nome(dto.getNome())
                .celular(dto.getCelular())
                .telefone(dto.getTelefone())
                .logradouro(dto.getLogradouro())
                .numero(dto.getNumero())
                .bairro(dto.getBairro())
                .cidade(dto.getCidade())
                .cep(dto.getCep())
                .email(dto.getEmail())
                .ativo(dto.getAtivo() != null ? dto.getAtivo() : true)
                .perfil(Perfil.CLIENTE).build();

        Cliente novoCliente = clienteService.salvarCliente(cliente);
        ClienteResponseDTO response = mapToResponse(novoCliente);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        List<ClienteResponseDTO> clientes = clienteService.listarClientes()
                .stream()
                .filter(c -> c.getPerfil() == Perfil.CLIENTE)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ativos")
    public ResponseEntity<List<ClienteResponseDTO>> listarAtivos() {
        List<ClienteResponseDTO> clientes = clienteService.listarClientesAtivos()
                .stream()
                .filter(c -> c.getPerfil() == Perfil.CLIENTE)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inativos")
    public ResponseEntity<List<ClienteResponseDTO>> listarInativos() {
        List<ClienteResponseDTO> clientes = clienteService.listarClientesInativos()
                .stream()
                .filter(c -> c.getPerfil() == Perfil.CLIENTE)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/me")
    public ResponseEntity<ClienteResponseDTO> meusDados(Principal principal) {
        Cliente cliente = clienteService.buscarPorEmail(principal.getName());
        return ResponseEntity.ok(mapToResponse(cliente));
    }

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

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(
            @PathVariable Long id,
            @RequestBody ClienteRequestDTO dto) {

        Cliente atualizado = clienteService.atualizarCliente(id, dto);
        return ResponseEntity.ok(mapToResponse(atualizado));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativarCliente(@PathVariable Long id) {
        clienteService.desativarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/nome/{nome}")
    public ResponseEntity<ClienteResponseDTO> buscarPorNome(@PathVariable String nome) {
        Cliente cliente = clienteService.buscarPorNome(nome);
        return ResponseEntity.ok(mapToResponse(cliente));
    }

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