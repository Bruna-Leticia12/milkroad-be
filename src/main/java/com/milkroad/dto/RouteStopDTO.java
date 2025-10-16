package com.milkroad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RouteStopDTO {
    private Long entregaId;
    private Long clienteId;
    private String clienteNome;
    private String endereco;
    private double latitude;
    private double longitude;
    private double distanceFromPreviousMeters;
    private int position;
}