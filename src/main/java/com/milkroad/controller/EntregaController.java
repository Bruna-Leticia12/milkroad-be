package com.milkroad.controller;

import com.milkroad.entity.Entrega;
import com.milkroad.service.EntregaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    // Listar entregas de um cliente (ADMIN ou CLIENTE dono)
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Entrega>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(entregaService.listarEntregasCliente(clienteId));
    }

    // Listar entregas de uma data específica
    @GetMapping("/data/{data}")
    public ResponseEntity<List<Entrega>> listarPorData(@PathVariable String data) {
        LocalDate dataEntrega = LocalDate.parse(data);
        return ResponseEntity.ok(entregaService.listarEntregasPorData(dataEntrega));
    }

    // Cancelar uma entrega específica
    @PutMapping("/{entregaId}/cancelar")
    public ResponseEntity<?> cancelarEntrega(@PathVariable Long entregaId) {
        try {
            return ResponseEntity.ok(entregaService.cancelarEntrega(entregaId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
