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

    /**
     * Cria entregas automáticas para o cliente (segunda a sexta, por 1 mês)
     * Ignora automaticamente clientes inativos.
     */
    public void gerarEntregasAutomaticas(Cliente cliente) {
        // Ignora clientes inativos
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

    /**
     * Lista entregas de um cliente específico.
     * Caso o cliente esteja inativo, retorna lista vazia.
     */
    public List<Entrega> listarEntregasCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
        if (cliente == null || !cliente.isAtivo()) {
            return List.of();
        }
        return entregaRepository.findByClienteId(clienteId);
    }

    /**
     * Lista entregas por data, ignorando clientes inativos.
     */
    public List<Entrega> listarEntregasPorData(LocalDate data) {
        return entregaRepository.findByDataEntrega(data).stream()
                .filter(e -> e.getCliente() != null && e.getCliente().isAtivo())
                .collect(Collectors.toList());
    }

    /**
     * Cancela uma entrega, respeitando as regras de horário.
     */
    public Entrega cancelarEntrega(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));

        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();

        if (entrega.getDataEntrega().isEqual(hoje) && agora.isAfter(LocalTime.of(7, 0))) {
            throw new RuntimeException("Cancelamento da entrega do dia atual só é permitido até as 07h.");
        }

        entrega.setConfirmada(false);
        return entregaRepository.save(entrega);
    }

    /**
     * Gera automaticamente as entregas do próximo mês (dia 28 às 02:00).
     * Apenas para clientes ativos.
     */
    @Scheduled(cron = "0 0 2 28 * *")
    public void gerarEntregasProximoMes() {
        LocalDate hoje = LocalDate.now();
        LocalDate primeiroDiaProximoMes = hoje.plusMonths(1).withDayOfMonth(1);
        LocalDate ultimoDiaProximoMes = primeiroDiaProximoMes.withDayOfMonth(primeiroDiaProximoMes.lengthOfMonth());

        // Busca apenas clientes ativos
        List<Cliente> clientes = clienteRepository.findByAtivo(true);

        for (Cliente cliente : clientes) {
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

        System.out.println("✅ Entregas do próximo mês foram geradas automaticamente (apenas clientes ativos).");
    }
}