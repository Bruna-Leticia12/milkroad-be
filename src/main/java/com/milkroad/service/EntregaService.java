package com.milkroad.service;

import com.milkroad.entity.Cliente;
import com.milkroad.entity.Entrega;
import com.milkroad.repository.ClienteRepository;
import com.milkroad.repository.EntregaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final ClienteRepository clienteRepository;

    public EntregaService(EntregaRepository entregaRepository, ClienteRepository clienteRepository) {
        this.entregaRepository = entregaRepository;
        this.clienteRepository = clienteRepository;
    }

    public void gerarEntregasAutomaticas(Cliente cliente) {
        if (cliente == null || !cliente.isAtivo()) {
            return;
        }

        LocalDate hoje = LocalDate.now();
        LocalDate fim = hoje.plusMonths(1);
        Long clienteId = cliente.getId();

        for (LocalDate data = hoje; data.isBefore(fim); data = data.plusDays(1)) {
            if (data.getDayOfWeek() != DayOfWeek.SATURDAY && data.getDayOfWeek() != DayOfWeek.SUNDAY) {

                final LocalDate dataAtual = data;
                boolean existe = entregaRepository.findByClienteId(clienteId)
                        .stream()
                        .anyMatch(e -> e.getDataEntrega().isEqual(dataAtual));

                if (!existe) {
                    Entrega entrega = Entrega.builder()
                            .cliente(cliente)
                            .dataEntrega(dataAtual)
                            .confirmada(true)
                            .build();
                    entregaRepository.save(entrega);
                }
            }
        }
    }

    public void desativarEntregasFuturas(Long clienteId) {

        LocalDate hoje = LocalDate.now();

        List<Entrega> entregas = entregaRepository.findByClienteId(clienteId)
                .stream()
                .filter(e -> e.getDataEntrega().isAfter(hoje))
                .collect(Collectors.toList());

        for (Entrega e : entregas) {
            e.setConfirmada(false);
            entregaRepository.save(e);
        }
    }


    public List<Entrega> listarEntregasCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
        if (cliente == null || !cliente.isAtivo()) {
            return List.of();
        }
        return entregaRepository.findByClienteId(clienteId);
    }

    public List<Entrega> listarEntregasPorData(LocalDate data) {
        return entregaRepository.findByDataEntrega(data).stream()
                .filter(e -> e.getCliente() != null && e.getCliente().isAtivo())
                .collect(Collectors.toList());
    }

    public Entrega cancelarEntrega(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));

        if (entrega.getCliente() == null || !entrega.getCliente().isAtivo()) {
            throw new RuntimeException("Não é possível alterar entrega de cliente inativo.");
        }

        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();

        if (entrega.getDataEntrega().isEqual(hoje) && agora.isAfter(LocalTime.of(7, 0))) {
            throw new RuntimeException("Cancelamento da entrega do dia atual só é permitido até as 07h.");
        }

        entrega.setConfirmada(false);
        return entregaRepository.save(entrega);
    }

    @Scheduled(cron = "0 0 2 28 * *")
    public void gerarEntregasProximoMes() {
        LocalDate hoje = LocalDate.now();
        LocalDate primeiroDiaProximoMes = hoje.plusMonths(1).withDayOfMonth(1);
        LocalDate ultimoDiaProximoMes = primeiroDiaProximoMes.withDayOfMonth(primeiroDiaProximoMes.lengthOfMonth());

        List<Cliente> clientesAtivos = clienteRepository.findByAtivo(true);

        for (Cliente cliente : clientesAtivos) {
            Long clienteId = cliente.getId();
            LocalDate data = primeiroDiaProximoMes;

            while (!data.isAfter(ultimoDiaProximoMes)) {
                if (data.getDayOfWeek() != DayOfWeek.SATURDAY && data.getDayOfWeek() != DayOfWeek.SUNDAY) {

                    final LocalDate dataAtual = data;
                    boolean existe = entregaRepository.findByClienteId(clienteId)
                            .stream()
                            .anyMatch(e -> e.getDataEntrega().isEqual(dataAtual));

                    if (!existe) {
                        Entrega entrega = Entrega.builder()
                                .cliente(cliente)
                                .dataEntrega(dataAtual)
                                .confirmada(true)
                                .build();

                        entregaRepository.save(entrega);
                    }
                }
                data = data.plusDays(1);
            }
        }
        System.out.println("✅ Entregas do próximo mês geradas automaticamente (apenas clientes ativos).");
    }
}