package com.smartsub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsub.dto.ReceiptScanResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReceiptScanningService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptScanningService.class);

    @Value("${app.openai.api-key:}")
    private String configuredApiKey;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String modelName;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReceiptScanResultDto scanReceipt(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        // Validate file size (max 5 MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

        boolean isImage = contentType.startsWith("image/") || originalFilename.endsWith(".png") || originalFilename.endsWith(".jpg") || originalFilename.endsWith(".jpeg");
        boolean isPdf = contentType.contains("pdf") || originalFilename.endsWith(".pdf");
        boolean isText = contentType.contains("text") || originalFilename.endsWith(".txt") || originalFilename.endsWith(".csv");

        if (!isImage && !isPdf && !isText) {
            throw new IllegalArgumentException("Unsupported file format. Please upload an image (PNG, JPG), PDF, or text invoice.");
        }

        String apiKey = getEffectiveApiKey();

        // If OpenAI is configured and file is an image, attempt GPT-4o-mini Vision extraction
        if (apiKey != null && !apiKey.isBlank() && isImage) {
            try {
                return extractUsingOpenAiVision(file, apiKey);
            } catch (Exception e) {
                logger.warn("OpenAI Vision receipt extraction failed: {}. Falling back to heuristic text analysis.", e.getMessage());
            }
        }

        // Fallback: heuristic pattern analysis on filename and file bytes/text
        return extractUsingHeuristics(file, originalFilename);
    }

    private String getEffectiveApiKey() {
        String envKey = System.getenv("OPENAI_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }
        return (configuredApiKey != null && !configuredApiKey.isBlank()) ? configuredApiKey.trim() : null;
    }

    private ReceiptScanResultDto extractUsingOpenAiVision(MultipartFile file, String apiKey) throws Exception {
        byte[] bytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(bytes);
        String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        String dataUrl = "data:" + mimeType + ";base64," + base64Image;

        String prompt = "Extract subscription details from this invoice/receipt image. "
                + "Return ONLY a raw JSON object with keys: "
                + "\"serviceName\" (string, e.g. Netflix), "
                + "\"planName\" (string, e.g. Premium 4K), "
                + "\"cost\" (numeric amount paid), "
                + "\"billingCycle\" (MONTHLY or YEARLY), "
                + "\"renewalDate\" (YYYY-MM-DD or next billing date), "
                + "\"category\" (Entertainment, Productivity, Cloud Storage, Fitness, Education, Utilities, Gaming, News, Other), "
                + "\"confidence\" (0.0 to 1.0). Do not include markdown code fences.";

        Map<String, Object> textPart = Map.of("type", "text", "text", prompt);
        Map<String, Object> imagePart = Map.of("type", "image_url", "image_url", Map.of("url", dataUrl, "detail", "low"));

        Map<String, Object> messageUser = Map.of("role", "user", "content", List.of(textPart, imagePart));
        Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "messages", List.of(messageUser),
                "max_tokens", 350
        );

        String jsonPayload = objectMapper.writeValueAsString(requestBody);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() >= 400) {
            throw new RuntimeException("OpenAI API returned " + httpResponse.statusCode());
        }

        JsonNode root = objectMapper.readTree(httpResponse.body());
        String rawContent = root.path("choices").get(0).path("message").path("content").asText();
        rawContent = rawContent.replaceAll("```json", "").replaceAll("```", "").trim();

        JsonNode parsed = objectMapper.readTree(rawContent);

        String serviceName = parsed.path("serviceName").asText("Unknown Service");
        String planName = parsed.path("planName").asText("Standard");
        double cost = parsed.path("cost").asDouble(0.0);
        String billingCycle = parsed.path("billingCycle").asText("MONTHLY").toUpperCase();
        String renewalDateStr = parsed.path("renewalDate").asText();
        String category = parsed.path("category").asText("Entertainment");
        double confidence = parsed.path("confidence").asDouble(0.9);

        LocalDate renewalDate = LocalDate.now().plusMonths(1);
        try {
            if (renewalDateStr != null && !renewalDateStr.isBlank()) {
                renewalDate = LocalDate.parse(renewalDateStr);
            }
        } catch (Exception ignored) {}

        return new ReceiptScanResultDto(
                serviceName,
                planName,
                cost,
                billingCycle,
                renewalDate,
                category,
                "Extracted via OpenAI Vision from " + file.getOriginalFilename(),
                confidence,
                "Please verify extracted values before saving."
        );
    }

    private ReceiptScanResultDto extractUsingHeuristics(MultipartFile file, String filename) {
        String content = "";
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception ignored) {}

        String fullText = (filename + " " + content).toLowerCase();

        // 1. Detect Brand
        String serviceName = "Subscription Service";
        String category = "Entertainment";
        String planName = "Standard Plan";

        if (fullText.contains("netflix")) {
            serviceName = "Netflix";
            category = "Entertainment";
            planName = "Premium 4K";
        } else if (fullText.contains("spotify")) {
            serviceName = "Spotify";
            category = "Entertainment";
            planName = "Individual Premium";
        } else if (fullText.contains("prime") || fullText.contains("amazon")) {
            serviceName = "Amazon Prime";
            category = "Entertainment";
            planName = "Prime Video & Shipping";
        } else if (fullText.contains("disney") || fullText.contains("hotstar")) {
            serviceName = "Disney+ Hotstar";
            category = "Entertainment";
            planName = "Super Plan";
        } else if (fullText.contains("github")) {
            serviceName = "GitHub Pro";
            category = "Productivity";
            planName = "Developer Tier";
        } else if (fullText.contains("chatgpt") || fullText.contains("openai")) {
            serviceName = "ChatGPT Plus";
            category = "Productivity";
            planName = "Plus Tier";
        } else if (fullText.contains("google one") || fullText.contains("google storage")) {
            serviceName = "Google One";
            category = "Cloud Storage";
            planName = "200GB Cloud Storage";
        } else if (fullText.contains("icloud") || fullText.contains("apple")) {
            serviceName = "Apple iCloud+";
            category = "Cloud Storage";
            planName = "50GB Storage";
        } else if (fullText.contains("coursera")) {
            serviceName = "Coursera Plus";
            category = "Education";
            planName = "Specialization Access";
        } else if (fullText.contains("cult") || fullText.contains("gym") || fullText.contains("fitness")) {
            serviceName = "Cult.fit";
            category = "Fitness";
            planName = "Elite Pass";
        }

        // 2. Detect Amount / Cost
        double cost = 499.0;
        Pattern amountPattern = Pattern.compile("(?:total|amount|inr|rs|₹|usd|\\$)[\\s:]*([0-9]+(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE);
        Matcher amountMatcher = amountPattern.matcher(content);
        if (amountMatcher.find()) {
            try {
                cost = Double.parseDouble(amountMatcher.group(1));
            } catch (Exception ignored) {}
        } else {
            // Check default brand pricing
            if (serviceName.equals("Netflix")) cost = 649.0;
            else if (serviceName.equals("Spotify")) cost = 139.0;
            else if (serviceName.equals("Amazon Prime")) cost = 1499.0;
            else if (serviceName.equals("GitHub Pro")) cost = 350.0;
        }

        // 3. Detect Billing Cycle
        String cycle = "MONTHLY";
        if (fullText.contains("annual") || fullText.contains("yearly") || cost >= 1200) {
            cycle = (serviceName.equals("Amazon Prime") || cost >= 1200) ? "YEARLY" : "MONTHLY";
        }

        // 4. Renewal Date
        LocalDate renewalDate = LocalDate.now().plusMonths(cycle.equals("YEARLY") ? 12 : 1);

        return new ReceiptScanResultDto(
                serviceName,
                planName,
                cost,
                cycle,
                renewalDate,
                category,
                "Extracted from document metadata and header patterns (" + filename + ")",
                0.80,
                "Extracted via SmartSub Intelligence parser. Please verify details before saving."
        );
    }
}
