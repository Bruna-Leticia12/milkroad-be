package com.milkroad.controller;

import com.milkroad.entity.Cliente;
import com.milkroad.exception.CancelamentoInvalidoException;
import com.milkroad.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // Criar cliente (somente ADMIN pode cadastrar novos clientes)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Cliente> criarCliente(@RequestBody Cliente cliente) {
        Cliente novoCliente = clienteService.salvarCliente(cliente);
        return ResponseEntity.ok(novoCliente);
    }

    // Listar todos (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    // Listar ativos (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ativos")
    public ResponseEntity<List<Cliente>> listarAtivos() {
        return ResponseEntity.ok(clienteService.listarClientesAtivos());
    }

    // Listar inativos (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inativos")
    public ResponseEntity<List<Cliente>> listarInativos() {
        return ResponseEntity.ok(clienteService.listarClientesInativos());
    }

    // Cliente visualiza os próprios dados
    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/me")
    public ResponseEntity<Cliente> meusDados(Principal principal) {
        Cliente cliente = clienteService.buscarPorEmail(principal.getName());
        return ResponseEntity.ok(cliente);
    }

    // Cliente cancela sua própria entrega
    @PreAuthorize("hasRole('CLIENTE')")
    @PutMapping("/me/cancelar")
    public ResponseEntity<?> cancelarEntrega(Principal principal) {
        try {
            Cliente cliente = clienteService.cancelarEntregaPorEmail(principal.getName());
            return ResponseEntity.ok(cliente);
        } catch (CancelamentoInvalidoException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
