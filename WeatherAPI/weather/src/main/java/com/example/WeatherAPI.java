package com.example;

import java.awt.BorderLayout;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherAPI {

    // Replace with your actual API key
    private static final String API_KEY = "9adb265edb9b6970dacc539a7418d082";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=";

    // Function to fetch weather data asynchronously
    public static CompletableFuture<String> getWeatherDataAsync(String city) {
        String url = BASE_URL + city + "&appid=" + API_KEY + "&units=metric"; // Adding metric units for Celsius

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(e -> "Error fetching weather data for " + city + ": " + e.getMessage());
    }

    // Function to format the JSON response for display
    public static String formatWeatherData(String jsonData) {
        try {
            JSONObject obj = new JSONObject(jsonData);
            String cityName = obj.getString("name");
            JSONObject main = obj.getJSONObject("main");
            double temperature = main.getDouble("temp");
            int humidity = main.getInt("humidity");
            JSONObject weather = obj.getJSONArray("weather").getJSONObject(0);
            String description = weather.getString("description");
            double windSpeed = obj.getJSONObject("wind").getDouble("speed");

            // Format the weather data into a readable format
            return String.format("City: %s\nTemperature: %.2f°C\nWeather: %s\nHumidity: %d%%\nWind Speed: %.2f m/s", 
                    cityName, temperature, description, humidity, windSpeed);
        } catch (JSONException e) {
            return "Error parsing weather data.";
        }
    }

    // Function to build the GUI
    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Weather App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Label for displaying weather results
        JTextArea weatherDisplay = new JTextArea(10, 40);
        weatherDisplay.setEditable(false);

        // TextField for city search
        JTextField citySearchField = new JTextField(20);
        JButton searchButton = new JButton("Search Weather");

        // Add components to the top search panel
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Enter city:"));
        searchPanel.add(citySearchField);
        searchPanel.add(searchButton);

        // Add the search panel and weatherDisplay to the mainPanel
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(weatherDisplay), BorderLayout.CENTER);

        // Add action listener to the search button using lambda expression
        searchButton.addActionListener(e -> {
            String city = citySearchField.getText();
            if (!city.isEmpty()) {
                // Fetch and display weather asynchronously
                getWeatherDataAsync(city).thenAccept(jsonData -> {
                    String formattedData = formatWeatherData(jsonData);  // Format the data before displaying
                    weatherDisplay.setText(formattedData);
                });
            } else {
                weatherDisplay.setText("Please enter a city name.");
            }
        });

        // Add predefined city buttons for quick access
        JPanel cityButtonsPanel = new JPanel();
        String[] cities = {"London", "New York", "Tokyo", "Berlin", "Sydney", "Paris", "Los Angeles"};
        for (String city : cities) {
            JButton cityButton = new JButton(city);
            // Add action listener using lambda expression
            cityButton.addActionListener(e -> {
                getWeatherDataAsync(city).thenAccept(jsonData -> {
                    String formattedData = formatWeatherData(jsonData);  // Format the data before displaying
                    weatherDisplay.setText(formattedData);
                });
            });
            cityButtonsPanel.add(cityButton);
        }

        // Add city buttons panel to the bottom of the main panel
        mainPanel.add(cityButtonsPanel, BorderLayout.SOUTH);

        // Add main panel to the frame
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Ensure that GUI updates happen on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(WeatherAPI::createAndShowGUI);
    }
}
