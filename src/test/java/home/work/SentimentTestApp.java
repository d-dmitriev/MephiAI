package home.work;

import org.tribuo.Model;
import org.tribuo.classification.Label;

import java.io.IOException;
import java.nio.file.Path;

// Простой способ использования обученной модели
public class SentimentTestApp {
    public static void main(String[] args) throws Exception {
        // Загрузка модели
        Model<Label> model = loadModel(Path.of("models/final_sentiment_model.proto"));
        SentimentAnalyzerTrainer.SentimentService service = new SentimentAnalyzerTrainer.SentimentService((Model<Label>) model);

        // Анализ тональности
        String[] texts = {
                "Это просто великолепно!",
                "Ужасное обслуживание",
                "Нормально, но могло быть лучше",
                "Отличная работа! Рекомендую!"
        };

        for (String text : texts) {
            SentimentAnalyzerTrainer.SentimentService.SentimentResult result = service.analyzeWithConfidence(text);
            System.out.printf("'%s' → %s (%.1f%% уверенности)%n",
                    text, result.sentiment, result.confidence * 100);
        }
    }

    private static Model<Label> loadModel(Path filename)
            throws IOException {
        return Model.deserializeFromFile(filename).castModel(Label.class);
    }
}