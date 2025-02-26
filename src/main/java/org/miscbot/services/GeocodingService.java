package org.miscbot.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.miscbot.util.Location;

import java.net.URI;
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

    public Location getLocation(String address) throws NoSuchElementException {
        List<Location> locations = null;
        try {
            var uri = URI.create(String.format(urlTemplate, address, token));
            var request = HttpRequest.newBuilder().uri(uri).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var body = new ObjectMapper().readTree(response.body());
            var mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            var reader = mapper.readerFor(new TypeReference<List<Location>>() {});
            locations = reader.readValue(body);
            if(locations.isEmpty()) {
                throw new NoSuchElementException("Location could not be found");
            }
        }catch (NoSuchElementException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("Error during location search:");
            System.out.println(e.getMessage());
        }
        return locations == null ? null : locations.getFirst();
    }

}
