package home.work.dto;

public record SentimentResult(String sentiment, double confidence, int featuresUsed) {
}