package com.milkroad.repository;

import com.milkroad.entity.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByClienteId(Long clienteId);
    List<Entrega> findByDataEntrega(LocalDate dataEntrega);
    List<Entrega> findByDataEntregaAndConfirmadaTrue(LocalDate dataEntrega);
}