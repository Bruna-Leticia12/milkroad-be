package com.milkroad.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "entregas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataEntrega;

    private boolean confirmada = true; // por padrão, entrega confirmada

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
