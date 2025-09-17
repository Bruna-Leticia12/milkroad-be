package com.milkroad.service;

import com.milkroad.entity.Cliente;
import com.milkroad.entity.Entrega;
import com.milkroad.repository.ClienteRepository;
import com.milkroad.repository.EntregaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final ClienteRepository clienteRepository;

    public EntregaService(EntregaRepository entregaRepository, ClienteRepository clienteRepository) {
        this.entregaRepository = entregaRepository;
        this.clienteRepository = clienteRepository;
    }

    public Entrega criarEntrega(Long clienteId, LocalDate dataEntrega) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Entrega entrega = Entrega.builder()
                .cliente(cliente)
                .dataEntrega(dataEntrega)
                .confirmada(true)
                .build();

        return entregaRepository.save(entrega);
    }

    public List<Entrega> listarEntregasCliente(Long clienteId) {
        return entregaRepository.findByClienteId(clienteId);
    }

    public List<Entrega> listarEntregasPorData(LocalDate data) {
        return entregaRepository.findByDataEntrega(data);
    }

    public Entrega cancelarEntrega(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));

        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();

        // Se a entrega for para HOJE e já passou das 7h → não pode cancelar
        if (entrega.getDataEntrega().isEqual(hoje) && agora.isAfter(LocalTime.of(7, 0))) {
            throw new RuntimeException("Cancelamento de entrega no dia atual só é permitido até as 07h.");
        }

        // Se for entrega de amanhã ou datas futuras, pode cancelar a qualquer hora
        entrega.setConfirmada(false);
        return entregaRepository.save(entrega);
    }
}
