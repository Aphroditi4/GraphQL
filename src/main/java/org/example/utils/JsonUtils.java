package org.example.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TimeZone;

public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = objectMapper();
    private static final String DATA_DIR = "data";

    private JsonUtils() {
        throw new UnsupportedOperationException("This class is not supposed to be instantiated");
    }

    public static <T> T loadFromJsonFile(String path, TypeReference<T> typeReference) {
        String json = readResourceContent(path);
        return deserializeFromJson(json, typeReference);
    }

    public static <T> String serializeToJson(T data) {
        try {
            return OBJECT_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error occurred while serializing object to JSON", e);
        }
    }

    public static <T> T deserializeFromJson(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error occurred while deserializing object from JSON", e);
        }
    }

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    public static void saveToJsonFile(String path, Object data) {
        try {
            // Create a directory in the current working directory for data storage
            File dataDirectory = new File(DATA_DIR);
            if (!dataDirectory.exists()) {
                dataDirectory.mkdirs();
            }

            // Extract the filename from the path
            String filename = new File(path).getName();
            File outputFile = new File(dataDirectory, filename);

            // Serialize and save
            String json = OBJECT_MAPPER.writeValueAsString(data);
            Files.writeString(outputFile.toPath(), json);

            System.out.println("Data saved to: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Error saving data to JSON file: " + path, e);
        }
    }

    /**
     * Reads content from a resource path, trying classpath first then filesystem
     */
    private static String readResourceContent(String path) {
        try {
            // First try to load as absolute classpath resource
            if (path.startsWith("/")) {
                return readFromClasspath(path);
            }

            // Then try to load as relative classpath resource
            try {
                return readFromClasspath("/" + path);
            } catch (IllegalArgumentException e) {
                // Finally try as filesystem path
                return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }

    private static String readFromClasspath(String resourcePath) {
        try (InputStream inputStream = JsonUtils.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found in classpath: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read classpath resource: " + resourcePath, e);
        }
    }
}