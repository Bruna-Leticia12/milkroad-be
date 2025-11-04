package com.milkroad.controller;

import com.milkroad.dto.EntregaDTO;
import com.milkroad.entity.Entrega;
import com.milkroad.entity.Cliente;
import com.milkroad.entity.Perfil;
import com.milkroad.service.EntregaService;
import com.milkroad.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaService entregaService;
    private final ClienteService clienteService;

    public EntregaController(EntregaService entregaService, ClienteService clienteService) {
        this.entregaService = entregaService;
        this.clienteService = clienteService;
    }

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
    public List<EntregaDTO> listarEntregasDeHoje(Principal principal) {
        LocalDate hoje = LocalDate.now();

        List<EntregaDTO> entregas = entregaService.listarEntregasPorData(hoje)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        Cliente usuarioLogado = clienteService.buscarPorEmail(principal.getName());
        entregas = entregas.stream()
                .filter(e -> {
                    Cliente clienteEntrega = clienteService.buscarPorNome(e.getClienteNome());
                    return clienteEntrega != null && clienteEntrega.isAtivo();
                })
                .collect(Collectors.toList());
        if (usuarioLogado.getPerfil() == Perfil.ADMIN) {
            entregas = entregas.stream()
                    .filter(e -> !e.getClienteNome().equalsIgnoreCase(usuarioLogado.getNome()))
                    .collect(Collectors.toList());
        }
        return entregas;
    }
}