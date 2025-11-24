package home.work.controllers;

import home.work.dto.SentimentResult;
import home.work.services.SentimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SentimentController {

    @Autowired
    private SentimentService sentimentService;

    @GetMapping("/sentiment")
    public ResponseEntity<Map<String, Object>> analyzeSentiment(
            @RequestParam String text,
            @RequestParam(required = false, defaultValue = "false") boolean detailed) {

        try {
            SentimentResult result = sentimentService.analyze(text);

            Map<String, Object> response = new HashMap<>();
            response.put("sentiment", result.sentiment());

            if (detailed) {
                response.put("confidence", Math.round(result.confidence() * 10000) / 100.0);
                response.put("featuresUsed", result.featuresUsed());
                response.put("text", text);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("text", text);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
