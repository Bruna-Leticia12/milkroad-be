package com.milkroad.controller;

import com.milkroad.entity.Cliente;
import com.milkroad.exception.CancelamentoInvalidoException;
import com.milkroad.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // Criar cliente
    @PostMapping
    public ResponseEntity<Cliente> criarCliente(@RequestBody Cliente cliente) {
        Cliente novoCliente = clienteService.salvarCliente(cliente);
        return ResponseEntity.ok(novoCliente);
    }

    // Listar todos
    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    // Listar ativos
    @GetMapping("/ativos")
    public ResponseEntity<List<Cliente>> listarAtivos() {
        return ResponseEntity.ok(clienteService.listarClientesAtivos());
    }

    // Listar inativos
    @GetMapping("/inativos")
    public ResponseEntity<List<Cliente>> listarInativos() {
        return ResponseEntity.ok(clienteService.listarClientesInativos());
    }

    // Cancelar entrega
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarEntrega(@PathVariable Long id) {
        try {
            Cliente cliente = clienteService.cancelarEntrega(id);
            return ResponseEntity.ok(cliente);
        } catch (CancelamentoInvalidoException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
