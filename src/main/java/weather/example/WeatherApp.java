package weather.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherApp {
    public static void main(String[] args) {
        String apiKey = "4429618b-46fe-4286-bc5b-07e373775f74";

        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите долготу в градусах в формате XX.XX: ");
        String lon = scanner.nextLine();
        System.out.println("Введите широту в градусах в формате XX.XX: ");
        String lat = scanner.nextLine();
        System.out.println("Введите количество дней: ");
        int limit = scanner.nextInt();
        String url = String.format("https://api.weather.yandex.ru/v2/forecast?lat=%s&lon=%s&limit=%d", lon, lat, limit);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                System.out.println("Данные с сервера: " + responseBody);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);

                JsonNode factNode = rootNode.path("fact");
                System.out.println("Текущая температура: " + factNode.path("temp").asDouble() + "°C");

                JsonNode forecasts = rootNode.path("forecasts");

                if (forecasts.isArray() && !forecasts.isEmpty()) {
                    JsonNode forecast = forecasts.get(0);
                    JsonNode parts = forecast.path("parts");

                    if (parts.isObject()) {
                        double totalTemp = 0;
                        int count = 0;

                        for (JsonNode part : parts) {
                            double tempAvg = part.path("temp_avg").asDouble();
                            totalTemp += tempAvg;
                            count++;
                        }

                        if (count > 0) {
                            double avgTemp = totalTemp / count;
                            System.out.printf("Средняя температура за период: %.2f°C%n", avgTemp);
                        }
                    }
                }

            } else {
                System.err.println("Ошибка: Получен код ответа " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
