package com.milkroad.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milkroad.entity.Cliente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeoService {

    @Value("${google.api.key:}")
    private String googleApiKey;

    private final RestTemplate rest;
    //private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public GeoService(RestTemplate rest) {
        this.rest = rest;
    }

    public double[] geocodeCliente(Cliente cliente) {
        if (googleApiKey == null || googleApiKey.isBlank()) {
            throw new RuntimeException("Google API key não configurada. Configure google.api.key no application.yml");
        }

        String address = buildAddress(cliente);
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address={addr}&key={key}";

        try {
            String response = rest.getForObject(url, String.class, address, googleApiKey);
            JsonNode root = mapper.readTree(response);

            String status = root.path("status").asText("");
            if (!"OK".equals(status)) {
                String err = root.path("error_message").asText("");
                throw new RuntimeException("Geocoding API status=" + status + " message=" + err);
            }

            JsonNode results = root.path("results");
            if (results.isArray() && results.size() > 0) {
                JsonNode location = results.get(0).path("geometry").path("location");
                double lat = location.path("lat").asDouble();
                double lng = location.path("lng").asDouble();
                return new double[]{lat, lng};
            } else {
                throw new RuntimeException("Nenhum resultado encontrado para endereço: " + address);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao geocodificar endereço: " + ex.getMessage(), ex);
        }
    }

    private String buildAddress(Cliente c) {
        StringBuilder sb = new StringBuilder();
        if (c.getLogradouro() != null) sb.append(c.getLogradouro()).append(", ");
        if (c.getNumero() != null) sb.append(c.getNumero()).append(" - ");
        if (c.getBairro() != null) sb.append(c.getBairro()).append(" - ");
        if (c.getCidade() != null) sb.append(c.getCidade()).append(", Brasil");
        if (c.getCep() != null) sb.append(" - ").append(c.getCep());
        return sb.toString();
    }
}