package com.milkroad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RouteDTO {
    private String date;
    private double totalDistanceMeters;
    private List<RouteStopDTO> stops;
}