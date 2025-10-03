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
    //private final RestTemplate rest = new RestTemplate();
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

    /**
     * Monta rota otimizada para a data informada.
     * O depot (início/fim) será o primeiro ADMIN ativo encontrado.
     * Retorna RouteDTO (date, totalDistanceMeters, List<RouteStopDTO>).
     */
    public RouteDTO buildOptimizedRouteForDate(LocalDate date) {
        // 1) busca um admin ativo (depot)
        Cliente admin = clienteRepository.findByAtivo(true).stream()
                .filter(c -> c.getPerfil() == Perfil.ADMIN)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nenhum ADMIN ativo cadastrado para ser depot."));

        // valida (vai lançar se chave não estiver configurada ou endereço inválido)
        geoService.geocodeCliente(admin);

        // 2) buscar entregas confirmadas do dia
        List<Entrega> entregasDia = entregaRepository.findByDataEntregaAndConfirmadaTrue(date);

        if (entregasDia.isEmpty()) {
            return new RouteDTO(date.toString(), 0.0, Collections.emptyList());
        }

        // 3) montar lista de waypoints e informação associada (preserva ordem original)
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
            double[] coords = geoService.geocodeCliente(c); // geocodifica
            String addr = buildAddress(c);
            waypoints.add(new WaypointInfo(e, c, addr, coords[0], coords[1]));
        }

        // 4) construir URL do Directions API com optimize:true
        String originEnc = encode(buildAddress(admin));
        String destEnc = originEnc; // volta ao mesmo admin
        // cada waypoint precisa ser encode
        List<String> encodedWps = waypoints.stream()
                .map(w -> encode(w.address))
                .collect(Collectors.toList());

        String joinedWps = String.join("|", encodedWps);

        String url = "https://maps.googleapis.com/maps/api/directions/json"
                + "?origin=" + originEnc
                + "&destination=" + destEnc
                + "&waypoints=optimize:true|" + joinedWps
                + "&key=" + googleApiKey;

        try {
            String response = rest.getForObject(url, String.class);
            JsonNode root = mapper.readTree(response);

            JsonNode routes = root.path("routes");
            if (!routes.isArray() || routes.size() == 0) {
                throw new RuntimeException("Não foi possível calcular rota no Google Directions API.");
            }

            JsonNode route = routes.get(0);
            JsonNode legs = route.path("legs");
            JsonNode waypointOrderNode = route.path("waypoint_order"); // ordem otimizada dos índices dos waypoints

            // waypointOrderNode é array com tamanho = number of waypoints
            List<Integer> optimizedOrder = new ArrayList<>();
            if (waypointOrderNode.isArray()) {
                for (JsonNode n : waypointOrderNode) optimizedOrder.add(n.asInt());
            } else {
                // fallback: se não vier, use ordem original
                for (int i = 0; i < waypoints.size(); i++) optimizedOrder.add(i);
            }

            // legs: length = waypoints.size() + 1 (orig->wp0, wp0->wp1,... last->dest)
            // Para cada stop (waypoint) j (0..n-1), a leg index j corresponde à chegada desse stop.
            List<RouteStopDTO> stops = new ArrayList<>();
            double totalDistanceMeters = 0.0;

            //int nWaypoints = waypoints.size();
            for (int legIdx = 0; legIdx < legs.size(); legIdx++) {
                JsonNode leg = legs.get(legIdx);
                double legDistance = leg.path("distance").path("value").asDouble(0.0); // metros
                totalDistanceMeters += legDistance;
            }

        // montar stops na ordem otimizada
        for (int pos = 0; pos < optimizedOrder.size(); pos++) {
            int originalIndex = optimizedOrder.get(pos); // índice na lista 'waypoints'
            WaypointInfo wp = waypoints.get(originalIndex);

            // a distância from previous é leg at index pos (origin->first = leg0, etc)
            JsonNode legForThisStop = legs.get(pos);
            double distFromPrev = legForThisStop.path("distance").path("value").asDouble(0.0);

            RouteStopDTO stop = new RouteStopDTO(
                    wp.entrega.getId(),
                    wp.cliente.getId(),
                    wp.cliente.getNome(),
                    wp.address,
                    wp.lat,
                    wp.lng,
                    distFromPrev,
                    pos + 1
            );
            stops.add(stop);
        }

        // retorna conforme o DTO que você mandou
        return new RouteDTO(
                date.toString(),
                totalDistanceMeters,
                stops
        );

        } catch (Exception ex) {
            throw new RuntimeException("Erro ao gerar rota otimizada: " + ex.getMessage(), ex);
        }
    }

    // helper: monta endereço legível a partir do cliente
    private String buildAddress(Cliente c) {
        StringBuilder sb = new StringBuilder();
        if (c.getLogradouro() != null) sb.append(c.getLogradouro());
        if (c.getNumero() != null) sb.append(", ").append(c.getNumero());
        if (c.getBairro() != null) sb.append(" - ").append(c.getBairro());
        if (c.getCidade() != null) sb.append(" - ").append(c.getCidade());
        if (c.getCep() != null) sb.append(" - CEP ").append(c.getCep());
        return sb.toString();
    }

    // url-encode usando UTF-8
    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
