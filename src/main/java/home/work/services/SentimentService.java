package home.work.services;

import home.work.dto.SentimentResult;
import home.work.exceptions.SentimentAnalysisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tribuo.Example;
import org.tribuo.ImmutableFeatureMap;
import org.tribuo.Model;
import org.tribuo.Prediction;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.impl.ArrayExample;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class SentimentService {
    private final Logger log = LoggerFactory.getLogger(SentimentService.class);

    private final Model<Label> model;

    public SentimentService(@Value("${app.model.path:sentiment_model.proto}") String modelPath) throws IOException {
        Resource resource = new ClassPathResource(modelPath);
        this.model = Model.deserializeFromStream(resource.getInputStream()).castModel(Label.class);
        log.info("Model {} loaded", modelPath);
    }

    public SentimentResult analyze(String text) {
        try {
            Example<Label> example = textToExampleForPrediction(text, model);
            Prediction<Label> prediction = model.predict(example);

            String sentiment = prediction.getOutput().getLabel();
            double confidence = prediction.getOutput().getScore();

            log.info("Analyze string={}, sentiment={}, confidence={}", text, sentiment, confidence);

            return new SentimentResult(sentiment, confidence, example.size());
        } catch (Exception e) {
            log.error("Analyze error :{}", e.getMessage());
            throw new SentimentAnalysisException("Ошибка при анализе тональности: " + e.getMessage());
        }
    }

    private Example<Label> textToExampleForPrediction(String text, Model<?> model) {
        ArrayExample<Label> example = new ArrayExample<>(LabelFactory.UNKNOWN_LABEL);

        String[] words = preprocessText(text);
        ImmutableFeatureMap featureMap = model.getFeatureIDMap();

        // Считаем TF для известных слов
        Map<String, Integer> knownWordCounts = new HashMap<>();
        for (String word : words) {
            if (isValidWord(word)) {
                String featureName = "word_" + word;
                if (featureMap.get(featureName) != null) {
                    knownWordCounts.put(featureName, knownWordCounts.getOrDefault(featureName, 0) + 1);
                }
            }
        }

        // Добавляем нормализованные значения
        for (Map.Entry<String, Integer> entry : knownWordCounts.entrySet()) {
            double normalizedValue = 1.0 + Math.log(entry.getValue());
            example.add(entry.getKey(), normalizedValue);
        }

        // Если нет известных фич, используем фолбэк
        if (example.size() == 0) {
            addFallbackFeatures(example, featureMap);
        }

        return example;
    }

    private String[] preprocessText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .split("\\s+");
    }

    private boolean isValidWord(String word) {
        return word.length() > 2 && word.length() < 20;
    }

    private void addFallbackFeatures(ArrayExample<Label> example, ImmutableFeatureMap featureMap) {
        String[] commonFeatures = {"word_качество", "word_товар", "word_продукт"};
        for (String feature : commonFeatures) {
            if (featureMap.get(feature) != null) {
                example.add(feature, 0.01);
            }
        }
    }
}
