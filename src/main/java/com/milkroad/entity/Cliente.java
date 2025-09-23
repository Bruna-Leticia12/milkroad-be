package com.milkroad.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String celular;
    private String telefone;

    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String cep;

    @Column(unique = true, nullable = false)
    private String email;

    private boolean ativo = true;

    @JsonIgnore
    private String senha;

    @Enumerated(EnumType.STRING)
    private Perfil perfil;  // ADMIN ou CLIENTE
}
