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

    // ✅ Criar cliente (somente ADMIN)
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

    // ✅ Listar todos (ADMIN) — exclui perfis ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        List<ClienteResponseDTO> clientes = clienteService.listarClientes()
                .stream()
                .filter(c -> c.getPerfil() == Perfil.CLIENTE) // exclui ADMIN
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    // ✅ Listar ativos (ADMIN) — exclui perfis ADMIN
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

    // ✅ Listar inativos (ADMIN) — exclui perfis ADMIN
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

    // ✅ Atualizar dados de um cliente (somente ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(
            @PathVariable Long id,
            @RequestBody ClienteRequestDTO dto) {

        Cliente atualizado = clienteService.atualizarCliente(id, dto);
        return ResponseEntity.ok(mapToResponse(atualizado));
    }

    // ✅ Desativar cliente (somente ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativarCliente(@PathVariable Long id) {
        clienteService.desativarCliente(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Buscar cliente por nome (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/nome/{nome}")
    public ResponseEntity<ClienteResponseDTO> buscarPorNome(@PathVariable String nome) {
        Cliente cliente = clienteService.buscarPorNome(nome);
        return ResponseEntity.ok(mapToResponse(cliente));
    }

    // 🔄 Conversão para DTO
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