package com.milkroad.controller;

import com.milkroad.dto.RouteDTO;
import com.milkroad.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/rotas")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * Gera rota otimizada de entregas para uma data específica
     * Somente ADMIN deve consumir este endpoint
     */
    @GetMapping("/data/{data}")
    public ResponseEntity<RouteDTO> rotaPorData(@PathVariable String data) {
        LocalDate d = LocalDate.parse(data);
        RouteDTO dto = routeService.buildOptimizedRouteForDate(d);
        return ResponseEntity.ok(dto);
    }
}