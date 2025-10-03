package com.milkroad.controller;

import com.milkroad.dto.EntregaDTO;
import com.milkroad.entity.Entrega;
import com.milkroad.service.EntregaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    // Converter Entrega -> EntregaDTO
    private EntregaDTO toDTO(Entrega entrega) {
        return new EntregaDTO(
                entrega.getId(),
                entrega.getCliente().getNome(),
                entrega.isConfirmada(),
                entrega.getDataEntrega().toString()
        );
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<EntregaDTO>> listarPorCliente(@PathVariable Long clienteId) {
        List<EntregaDTO> entregas = entregaService.listarEntregasCliente(clienteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(entregas);
    }

    @GetMapping("/data/{data}")
    public ResponseEntity<List<EntregaDTO>> listarPorData(@PathVariable String data) {
        LocalDate dataEntrega = LocalDate.parse(data);
        List<EntregaDTO> entregas = entregaService.listarEntregasPorData(dataEntrega)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(entregas);
    }

    @PutMapping("/{entregaId}/cancelar")
    public ResponseEntity<?> cancelarEntrega(@PathVariable Long entregaId) {
        try {
            Entrega entrega = entregaService.cancelarEntrega(entregaId);
            return ResponseEntity.ok(toDTO(entrega));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/hoje")
    public List<EntregaDTO> listarEntregasDeHoje() {
        LocalDate hoje = LocalDate.now();
        return entregaService.listarEntregasPorData(hoje)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
