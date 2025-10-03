package com.milkroad.dto;

import lombok.Data;

@Data
public class ClienteRequestDTO {
    private String nome;
    private String celular;
    private String telefone;
    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String cep;
    private String email;
}
