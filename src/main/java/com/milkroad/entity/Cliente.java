package com.milkroad.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clientes", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")})
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    @NotBlank(message = "Celular é obrigatório")
    private String celular;
    private String telefone;

    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String cep;

    @Email(message = "E-mail inválido")
    @NotBlank(message = "E-mail é obrigatório")
    @Column(unique = true, nullable = false)
    private String email;

    private boolean ativo = true;

    @JsonIgnore
    private String senha;

    @Enumerated(EnumType.STRING)
    private Perfil perfil;  // ADMIN ou CLIENTE
}
