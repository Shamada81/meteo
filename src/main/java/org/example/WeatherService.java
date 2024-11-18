package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {
    private static final String API_KEY = "66da7b3e-0e7e-427f-8305-e1abd732fce5"; // Замените на ваш API-ключ
    private static final String BASE_URL = "https://api.weather.yandex.ru/v2/forecast";

    public static void main(String[] args) {
        // Координаты для запроса (например, Москва)
        double lat = 55.75;
        double lon = 37.62;
        int limit = 7; // Количество дней прогноза

        try {
            // Получение данных о погоде
            String jsonResponse = getWeatherData(lat, lon, limit);

            // Парсинг JSON-ответа
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(jsonResponse);

            // Вывод полного JSON
            System.out.println("Полный JSON-ответ:");
            System.out.println(data.toPrettyString());

            // Извлечение текущей температуры
            int currentTemp = data.path("fact").path("temp").asInt();
            System.out.println("Текущая температура: " + currentTemp + "°C");

            // Вычисление средней температуры
            double avgTemp = calculateAverageTemperature(data);
            System.out.println("Средняя температура за " + limit + " дня(ей): " + avgTemp + "°C");

        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    // Метод выполняющий запрос к API Яндекс.Погоды и возвращает ответ в виде строки JSON.
    private static String getWeatherData(double lat, double lon, int limit) throws IOException, InterruptedException {
        String url = BASE_URL + "?lat=" + lat + "&lon=" + lon + "&limit=" + limit;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Yandex-Weather-Key", API_KEY)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ошибка при запросе к API: " + response.statusCode());
        }

        return response.body();
    }
    // Метод вычислящий среднюю температуру
    private static double calculateAverageTemperature(JsonNode rootNode) {
        JsonNode forecasts = rootNode.path("forecasts");
        double sumTemp = 0;
        int count = 0;

        for (JsonNode dayForecast : forecasts) {
            JsonNode dayPart = dayForecast.path("parts").path("day");
            if (dayPart.has("temp_avg")) {
                sumTemp += dayPart.path("temp_avg").asDouble();
                count++;
            }
        }

        return count > 0 ? sumTemp / count : 0;
    }
}
