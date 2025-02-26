package org.miscbot.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.miscbot.util.Forecast;
import org.miscbot.util.Location;
import org.miscbot.util.Webcam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class WeatherService {
    private String apiKey;
    private final String API_URL = "http://api.weatherapi.com/v1/forecast.json?key=%s&q=%s&days=3&aqi=no&alerts=no";
    private final static Logger logger = LoggerFactory.getLogger(WeatherService.class);

    public WeatherService(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<Forecast> getForecasts(Location location) {
        var uri = URI.create(String.format(API_URL, apiKey, location.getLat() + "," + location.getLon()));
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .build();
        var client = HttpClient.newHttpClient();
        List<Forecast> forecasts = new ArrayList<>();
        try{
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var json = response.body();
            var body = new ObjectMapper().readTree(json).get("forecast").get("forecastday");
            logger.debug("Weather response: {}", body);
            var mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            var reader = mapper.readerFor(new TypeReference<List<Forecast>>() {});
            forecasts = reader.readValue(body);
            forecasts.forEach(f -> f.setLocation(location));
        }catch (Exception e) {
            logger.error("Failed to fetch forecast: {}", e.getMessage());
        }
        return forecasts;
    }
}
