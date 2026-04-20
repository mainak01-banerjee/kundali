package tech.mainak.kundali.kundali_2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class MessageService {
    private final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private static final String token = "AAFEaNLNQ2881eF0ZVEpZxOpsXt-7ZibYHo";  // Bot token
    private static final String base_url = "https://api.telegram.org/bot7510736131:" + token + "/";  // Base URL for Telegram API
    private static final String my_chat_id = "2026118946";
    private static final String client_id="5376064066";

    public void sendMessage(String text) throws URISyntaxException {
        try {

            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = base_url + "sendMessage?chat_id=" + my_chat_id + "&text=" + encodedText;


            HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();


            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());


            logger.info(response.body());

            response.statusCode();
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage());
        }


    }
}
