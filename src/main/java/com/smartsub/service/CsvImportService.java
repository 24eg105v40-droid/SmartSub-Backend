package com.smartsub.service;

import com.smartsub.dto.CsvImportItemDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CsvImportService {

    public List<CsvImportItemDto> parseAndDetectSubscriptions(MultipartFile file) {
        List<CsvImportItemDto> detectedList = new ArrayList<>();
        Map<String, List<TransactionRow>> groupedTransactions = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && (line.toLowerCase().contains("date") || line.toLowerCase().contains("amount") || line.toLowerCase().contains("description"))) {
                    continue; // Skip CSV header
                }

                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // CSV split respecting quotes
                if (parts.length >= 2) {
                    try {
                        String rawDesc = parts.length > 1 ? parts[1].replace("\"", "").trim() : "Unknown";
                        String amountStr = parts.length > 2 ? parts[2].replace("\"", "").replace("₹", "").replace("$", "").trim() : "0";
                        String dateStr = parts[0].replace("\"", "").trim();

                        double amount = Double.parseDouble(amountStr);
                        LocalDate date = parseDate(dateStr);

                        String normalizedKey = normalizeMerchantName(rawDesc);
                        groupedTransactions.computeIfAbsent(normalizedKey, k -> new ArrayList<>())
                                .add(new TransactionRow(rawDesc, amount, date));
                    } catch (Exception ignored) {
                        // Skip unparseable row
                    }
                }
            }

            for (Map.Entry<String, List<TransactionRow>> entry : groupedTransactions.entrySet()) {
                String cleanName = entry.getKey();
                List<TransactionRow> rows = entry.getValue();
                int count = rows.size();
                double avgAmount = rows.stream().mapToDouble(r -> r.amount).average().orElse(0.0);
                avgAmount = Math.round(avgAmount * 100.0) / 100.0;

                LocalDate latestDate = rows.stream()
                        .map(r -> r.date)
                        .filter(Objects::nonNull)
                        .max(LocalDate::compareTo)
                        .orElse(LocalDate.now());

                boolean isCandidate = count >= 2 || isKnownSubscriptionBrand(cleanName);
                String category = guessCategory(cleanName);
                String cycle = count >= 2 ? "MONTHLY" : "MONTHLY";
                String reason = count >= 2
                        ? ("Found " + count + " recurring transactions averaging ₹" + avgAmount)
                        : ("Identified standard recurring subscription brand: " + cleanName);

                detectedList.add(new CsvImportItemDto(
                        rows.get(0).rawDesc,
                        cleanName,
                        avgAmount,
                        category,
                        cycle,
                        latestDate.plusMonths(1), // Next projected renewal
                        count,
                        isCandidate,
                        reason
                ));
            }

            // Sort candidates first, then by occurrence count
            detectedList.sort((a, b) -> {
                if (a.isRecurringCandidate() != b.isRecurringCandidate()) {
                    return b.isRecurringCandidate() ? 1 : -1;
                }
                return Integer.compare(b.getOccurrenceCount(), a.getOccurrenceCount());
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }

        return detectedList;
    }

    private LocalDate parseDate(String dateStr) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
        );
        for (DateTimeFormatter dtf : formatters) {
            try {
                return LocalDate.parse(dateStr, dtf);
            } catch (Exception ignored) {}
        }
        return LocalDate.now();
    }

    private String normalizeMerchantName(String raw) {
        String upper = raw.toUpperCase();
        if (upper.contains("NETFLIX")) return "Netflix";
        if (upper.contains("SPOTIFY")) return "Spotify";
        if (upper.contains("PRIME") || upper.contains("AMAZON PRIME")) return "Amazon Prime";
        if (upper.contains("DISNEY") || upper.contains("HOTSTAR")) return "Disney+ Hotstar";
        if (upper.contains("YOUTUBE") || upper.contains("GOOGLE*YOUTUBE")) return "YouTube Premium";
        if (upper.contains("GITHUB")) return "GitHub Pro";
        if (upper.contains("OPENAI") || upper.contains("CHATGPT")) return "ChatGPT Plus";
        if (upper.contains("AWS") || upper.contains("AMAZON WEB SERVICES")) return "AWS Cloud";
        if (upper.contains("GOOGLE ONE") || upper.contains("GOOGLE STORAGE")) return "Google One Storage";
        if (upper.contains("APPLE.COM") || upper.contains("ICLOUD")) return "Apple iCloud+";
        if (upper.contains("COURSERA")) return "Coursera Plus";
        if (upper.contains("NOTION")) return "Notion Plus";
        if (upper.contains("FITNESS") || upper.contains("GYM") || upper.contains("CULT")) return "Cult.fit Gym";
        if (upper.contains("PLAYSTATION") || upper.contains("PSN")) return "PlayStation Plus";
        if (upper.contains("XBOX") || upper.contains("GAME PASS")) return "Xbox Game Pass";
        if (upper.contains("MEDIUM")) return "Medium Membership";

        // Clean raw string
        return raw.replaceAll("(?i)(autopay|mandate|recurring|bill|payment|in\\*|pos|txn|inr|usd|com)", "").trim();
    }

    private boolean isKnownSubscriptionBrand(String name) {
        List<String> brands = List.of("Netflix", "Spotify", "Amazon Prime", "Disney+ Hotstar", "YouTube Premium",
                "GitHub Pro", "ChatGPT Plus", "AWS Cloud", "Google One Storage", "Apple iCloud+", "Coursera Plus", "Notion Plus");
        return brands.stream().anyMatch(b -> b.equalsIgnoreCase(name));
    }

    private String guessCategory(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("netflix") || lower.contains("prime") || lower.contains("disney") || lower.contains("youtube") || lower.contains("hotstar")) {
            return "Entertainment";
        }
        if (lower.contains("spotify") || lower.contains("music") || lower.contains("apple music") || lower.contains("gaana")) {
            return "Entertainment";
        }
        if (lower.contains("github") || lower.contains("notion") || lower.contains("chatgpt") || lower.contains("copilot") || lower.contains("figma")) {
            return "Productivity";
        }
        if (lower.contains("google one") || lower.contains("icloud") || lower.contains("aws") || lower.contains("dropbox")) {
            return "Cloud Storage";
        }
        if (lower.contains("coursera") || lower.contains("udemy") || lower.contains("duolingo") || lower.contains("skillshare")) {
            return "Education";
        }
        if (lower.contains("gym") || lower.contains("fitness") || lower.contains("cult") || lower.contains("strava")) {
            return "Fitness";
        }
        if (lower.contains("playstation") || lower.contains("xbox") || lower.contains("steam") || lower.contains("nintendo")) {
            return "Gaming";
        }
        if (lower.contains("medium") || lower.contains("times") || lower.contains("wsj") || lower.contains("economist")) {
            return "News";
        }
        return "Utilities";
    }

    private static class TransactionRow {
        String rawDesc;
        double amount;
        LocalDate date;

        TransactionRow(String rawDesc, double amount, LocalDate date) {
            this.rawDesc = rawDesc;
            this.amount = amount;
            this.date = date;
        }
    }
}
