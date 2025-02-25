package org.miscbot.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.miscbot.util.Location;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.NoSuchElementException;

public class GeocodingService {
    HttpClient client = HttpClient.newHttpClient();
    String token;
    private final String urlTemplate = "https://geocode.maps.co/search?q=%s&api_key=%s";

    public GeocodingService(String token) {
        this.token = token;
    }

    public Location getLocation(String address) throws Exception {
        List<Location> locations;

        try {
            URL url = new URL(String.format(urlTemplate, address, token));
            HttpRequest request = HttpRequest.newBuilder().uri(url.toURI()).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            JsonNode body = new ObjectMapper().readTree(json);
            var mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            var reader = mapper.readerFor(new TypeReference<List<Location>>() {});
            locations = reader.readValue(body);
            if(locations.isEmpty()) {
                throw new NoSuchElementException("Location could not be found");
            }
        }catch (Exception e) {
            locations = null;
            throw e;
        }

        return locations.get(0);
    }

}
