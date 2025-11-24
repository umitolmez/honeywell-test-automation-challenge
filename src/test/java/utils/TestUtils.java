package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TestUtils {

    public static String generateUUID(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString();
    }

    public static String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String getIsoDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "Z";
    }

    //"9990001" -> "00000000009990001"
    public static String formatProductCode(String rawProductCode) {
        return "0000000000" + rawProductCode;
    }

    public static String readFileAsString(String fileName) {
        try {
            String path = "src/test/resources/" + fileName;
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException("File couldn't read: " + fileName, e);
        }
    }

    public static String generateSSCC(){
        return "urn:epc:id:sscc:0403040." + System.currentTimeMillis();
    }
}
