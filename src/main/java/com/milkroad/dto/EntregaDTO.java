package com.milkroad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaDTO {
    private Long idEntrega;
    private String clienteNome;
    private boolean confirmada;
    private String dataEntrega;
}
