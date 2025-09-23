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

@Service
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final ClienteRepository clienteRepository;

    public EntregaService(EntregaRepository entregaRepository, ClienteRepository clienteRepository) {
        this.entregaRepository = entregaRepository;
        this.clienteRepository = clienteRepository;
    }

    // Criar entregas automáticas para o cliente (segunda a sexta a partir de hoje, por 1 mês)
    public void gerarEntregasAutomaticas(Cliente cliente) {
        LocalDate hoje = LocalDate.now();
        LocalDate fim = hoje.plusMonths(1); // gera 1 mês de entregas futuras

        Long clienteId = cliente.getId(); // variável final para usar no lambda

        for (LocalDate data = hoje; data.isBefore(fim); data = data.plusDays(1)) {
            if (data.getDayOfWeek() != DayOfWeek.SATURDAY && data.getDayOfWeek() != DayOfWeek.SUNDAY) {

                final LocalDate dataAtual = data; // ← variável final para usar no lambda

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

        // Se for hoje e já passou das 7h, não pode cancelar
        if (entrega.getDataEntrega().isEqual(hoje) && agora.isAfter(LocalTime.of(7, 0))) {
            throw new RuntimeException("Cancelamento da entrega do dia atual só é permitido até as 07h.");
        }

        entrega.setConfirmada(false);
        return entregaRepository.save(entrega);
    }

    /**
     * Gera automaticamente as entregas do próximo mês todo dia 28 às 02:00 da manhã.
     */
    @Scheduled(cron = "0 0 2 28 * *") // minuto=0, hora=2, dia=28, todo mês
    public void gerarEntregasProximoMes() {
        LocalDate hoje = LocalDate.now();
        LocalDate primeiroDiaProximoMes = hoje.plusMonths(1).withDayOfMonth(1);
        LocalDate ultimoDiaProximoMes = primeiroDiaProximoMes.withDayOfMonth(primeiroDiaProximoMes.lengthOfMonth());

        List<Cliente> clientes = clienteRepository.findByAtivo(true);

        for (Cliente cliente : clientes) {
            Long clienteId = cliente.getId(); // variável final para usar no lambda

            LocalDate data = primeiroDiaProximoMes;
            while (!data.isAfter(ultimoDiaProximoMes)) {

                if (data.getDayOfWeek() != DayOfWeek.SATURDAY && data.getDayOfWeek() != DayOfWeek.SUNDAY) {

                    final LocalDate dataAtual = data; // variável final para usar no lambda

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

        System.out.println("Entregas do próximo mês foram geradas automaticamente!");
    }
}
