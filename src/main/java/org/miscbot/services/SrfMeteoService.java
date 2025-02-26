package org.miscbot.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.miscbot.util.Forecast;
import org.miscbot.util.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class SrfMeteoService {
    private static final String API_URL = "https://api.srgssr.ch/srf-meteo/v2/geolocations";
    private static final String FORECAST_URL = "https://api.srgssr.ch/srf-meteo/v2/forecastpoint/%s";
    private static final String TOKEN_URL = "https://api.srgssr.ch/oauth/v1/accesstoken?grant_type=client_credentials";
    private static final String CONSUMER_KEY = "";
    private static final String CONSUMER_SECRET = "";

    private static final Logger logger = LoggerFactory.getLogger(SrfMeteoService.class);


    public List<Forecast> getForecast(Location location) throws IOException {
        var accessToken = getAccessToken();
        logger.debug("access token: {}", accessToken);
        var id = fetchGeolocation(accessToken, location).replace("\"", "");
        logger.debug("id: {}", id);
        var forecasts = fetchForecast(accessToken, id);
        logger.debug("forecasts: {}", forecasts);
        return forecasts;
    }

    private String getAccessToken() throws IOException {
        HttpTransport transport = new NetHttpTransport();
        HttpRequestFactory requestFactory = transport.createRequestFactory();

        GenericUrl tokenUrl = new GenericUrl(TOKEN_URL);

        String credentials = CONSUMER_KEY + ":" + CONSUMER_SECRET;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        ByteArrayContent emptyContent = new ByteArrayContent(null, new byte[0]);

        HttpRequest request = requestFactory.buildPostRequest(tokenUrl, emptyContent);

        HttpHeaders headers = request.getHeaders();
        headers.setAuthorization("Basic " + encodedCredentials);
        headers.setCacheControl("no-cache");
        headers.setContentLength(0L);

        HttpResponse response = request.execute();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.getContent());


        String accessToken = jsonNode.path("access_token").asText(null);
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IOException("Access token is missing or empty. Verify credentials and API permissions.");
        }
        return accessToken;
    }


    private String fetchGeolocation(String accessToken, Location location) throws IOException {
        var transport = new NetHttpTransport();
        var requestFactory = transport.createRequestFactory();
        var url = new GenericUrl(API_URL);
        url.put("latitude", location.getLat());
        url.put("longitude", location.getLon());
        var node = executeRequest(accessToken, requestFactory, url);
        return node.get(0).get("id").toString();
    }

    private List<Forecast> fetchForecast(String accessToken, String id) throws IOException {
        var transport = new NetHttpTransport();
        var requestFactory = transport.createRequestFactory();
        var url = new GenericUrl(String.format(FORECAST_URL, id));
        logger.debug("url: {}", url);
        var node = executeRequest(accessToken, requestFactory, url).get("days");
        logger.debug("days: {}", node);
        var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var reader = mapper.readerFor(new TypeReference<List<Forecast>>() {});
        return reader.readValue(node);
    }

    private JsonNode executeRequest(String accessToken, HttpRequestFactory requestFactory, GenericUrl url) throws IOException {
        var request = requestFactory.buildGetRequest(url);
        request.getHeaders().setAuthorization("Bearer " + accessToken);
        var response = request.execute();
        logger.debug("response: {}", response.getContent());
        var objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getContent());
    }
}
