package org.example.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String readFileContent(String path) {
        try {
            if (path.startsWith("/")) {
                return readFromClasspath(path);
            }

            try {
                return readFromClasspath("/" + path);
            } catch (IllegalArgumentException e) {
                return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read resource: " + path, e);
        }
    }

    private static String readFromClasspath(String resourcePath) {
        try (InputStream inputStream = FileUtils.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found in classpath: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read classpath resource: " + resourcePath, e);
        }
    }
}