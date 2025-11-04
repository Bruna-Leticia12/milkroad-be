package com.milkroad.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milkroad.dto.RouteDTO;
import com.milkroad.dto.RouteStopDTO;
import com.milkroad.entity.Cliente;
import com.milkroad.entity.Entrega;
import com.milkroad.entity.Perfil;
import com.milkroad.repository.ClienteRepository;
import com.milkroad.repository.EntregaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RouteService {

    @Value("${google.api.key:}")
    private String googleApiKey;

    private final GeoService geoService;
    private final ClienteRepository clienteRepository;
    private final EntregaRepository entregaRepository;
    private final RestTemplate rest;
    private final ObjectMapper mapper = new ObjectMapper();

    public RouteService(GeoService geoService,
                        ClienteRepository clienteRepository,
                        EntregaRepository entregaRepository,
                        RestTemplate rest) {
        this.geoService = geoService;
        this.clienteRepository = clienteRepository;
        this.entregaRepository = entregaRepository;
        this.rest = rest;
    }

    public RouteDTO buildOptimizedRouteForDate(LocalDate date) {
        Cliente admin = clienteRepository.findByAtivo(true).stream()
                .filter(c -> c.getPerfil() == Perfil.ADMIN)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nenhum ADMIN ativo cadastrado para ser depot."));
        geoService.geocodeCliente(admin);

        List<Entrega> entregasDia = entregaRepository.findByDataEntregaAndConfirmadaTrue(date).stream()
                .filter(e -> e.getCliente() != null && e.getCliente().isAtivo())
                .collect(Collectors.toList());

        if (entregasDia.isEmpty()) {
            return new RouteDTO(date.toString(), 0.0, Collections.emptyList());
        }

        class WaypointInfo {
            final Entrega entrega;
            final Cliente cliente;
            final String address;
            final double lat;
            final double lng;

            WaypointInfo(Entrega entrega, Cliente cliente, String address, double lat, double lng) {
                this.entrega = entrega;
                this.cliente = cliente;
                this.address = address;
                this.lat = lat;
                this.lng = lng;
            }
        }

        List<WaypointInfo> waypoints = new ArrayList<>();
        for (Entrega e : entregasDia) {
            Cliente c = e.getCliente();
            if (c == null || !c.isAtivo()) continue;
            double[] coords = geoService.geocodeCliente(c);
            String addr = buildAddress(c);
            waypoints.add(new WaypointInfo(e, c, addr, coords[0], coords[1]));
        }

        if (waypoints.isEmpty()) {
            return new RouteDTO(date.toString(), 0.0, Collections.emptyList());
        }

        String originEnc = encode(buildAddress(admin));
        String destEnc = originEnc;
        String joinedWps = waypoints.stream()
                .map(w -> encode(w.address))
                .collect(Collectors.joining("|"));

        String url = "https://maps.googleapis.com/maps/api/directions/json"
                + "?origin=" + originEnc
                + "&destination=" + destEnc
                + "&waypoints=optimize:true|" + joinedWps
                + "&key=" + googleApiKey;

        try {
            String response = rest.getForObject(url, String.class);
            JsonNode root = mapper.readTree(response);

            JsonNode routes = root.path("routes");
            if (!routes.isArray() || routes.isEmpty()) {
                throw new RuntimeException("Não foi possível calcular rota no Google Directions API.");
            }

            JsonNode route = routes.get(0);
            JsonNode legs = route.path("legs");
            JsonNode waypointOrderNode = route.path("waypoint_order");

            List<Integer> optimizedOrder = new ArrayList<>();
            if (waypointOrderNode.isArray()) {
                for (JsonNode n : waypointOrderNode) optimizedOrder.add(n.asInt());
            } else {
                for (int i = 0; i < waypoints.size(); i++) optimizedOrder.add(i);
            }

            List<RouteStopDTO> stops = new ArrayList<>();
            double totalDistanceMeters = 0.0;

            for (JsonNode leg : legs) {
                totalDistanceMeters += leg.path("distance").path("value").asDouble(0.0);
            }

            for (int pos = 0; pos < optimizedOrder.size(); pos++) {
                int originalIndex = optimizedOrder.get(pos);
                WaypointInfo wp = waypoints.get(originalIndex);

                JsonNode legForThisStop = legs.get(pos);
                double distFromPrev = legForThisStop.path("distance").path("value").asDouble(0.0);

                stops.add(new RouteStopDTO(
                        wp.entrega.getId(),
                        wp.cliente.getId(),
                        wp.cliente.getNome(),
                        wp.address,
                        wp.lat,
                        wp.lng,
                        distFromPrev,
                        pos + 1
                ));
            }

            return new RouteDTO(date.toString(), totalDistanceMeters, stops);

        } catch (Exception ex) {
            throw new RuntimeException("Erro ao gerar rota otimizada: " + ex.getMessage(), ex);
        }
    }

    private String buildAddress(Cliente c) {
        StringBuilder sb = new StringBuilder();
        if (c.getLogradouro() != null) sb.append(c.getLogradouro());
        if (c.getNumero() != null) sb.append(", ").append(c.getNumero());
        if (c.getBairro() != null) sb.append(" - ").append(c.getBairro());
        if (c.getCidade() != null) sb.append(" - ").append(c.getCidade());
        if (c.getCep() != null) sb.append(" - CEP ").append(c.getCep());
        return sb.toString();
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}