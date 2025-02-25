package org.miscbot.services;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.miscbot.util.Location;
import org.miscbot.util.Webcam;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class WebcamService {
    private final String windyToken;
    private final String windyBaseUrl = "https://api.windy.com/webcams/api/v3/";
    private final String param = "nearby=%s,%s,10&limit=10";
    HttpClient client = HttpClient.newHttpClient();

    public WebcamService(String windyToken) {
        this.windyToken = windyToken;
    }


    public List<Webcam> getImage(Location location) throws Exception {
        var webcams = getWebcamInfo(location);
        webcams.forEach(webcam -> {
            webcam.setCurrentImage(
                    fetchImage(webcam.getImages().getCurrent().getPreview())
            );
        });
        return webcams;
    }

    private List<Webcam> getWebcamInfo(Location location) throws Exception  {
        URL url = new URL(windyBaseUrl + "webcams?" + String.format(param, location.getLat(), location.getLon()).replaceAll(",", "%2C") + "&include=images");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url.toURI())
                .setHeader("x-windy-api-key", windyToken)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();
        JsonNode body = new ObjectMapper().readTree(json);
        if(body.get("total").asInt() == 0){
            throw new NoSuchElementException("No Webcam found");
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(new TypeReference<List<Webcam>>() {});
        List<Webcam> webcams = reader.readValue(body.get("webcams"));
        return webcams.stream().filter(e -> e.getStatus().equals("active")).limit(5).toList();
    }

    private File fetchImage(String url)  {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        try{
            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
            tempFile.deleteOnExit();
            if(response.statusCode() == 200){
                Files.write(tempFile.toPath(), response.body());
                return tempFile;
            }
            throw new RuntimeException("Could not fetch image from " + url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
