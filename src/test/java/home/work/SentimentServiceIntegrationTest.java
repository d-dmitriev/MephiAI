package home.work;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SentimentServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/sentiment";
    }

    @Test
    void testPositiveSentiment() {
        String text = "Всё работает идеально.";
        Map<?, ?> response = restTemplate.getForObject(baseUrl() + "?text={text}", Map.class, text);
        assertThat(response).isNotNull();
        assertThat(response.get("sentiment")).isEqualTo("positive");
    }

    @Test
    void testNegativeSentiment() {
        String text = "Ужасный сервис, ничего не работает.";
        Map<?, ?> response = restTemplate.getForObject(baseUrl() + "?text={text}", Map.class, text);
        assertThat(response).isNotNull();
        assertThat(response.get("sentiment")).isEqualTo("negative");
    }

    @Test
    void testPositiveShort() {
        String text = "Отлично!";
        Map<?, ?> response = restTemplate.getForObject(baseUrl() + "?text={text}", Map.class, text);
        assertThat(response.get("sentiment")).isEqualTo("positive");
    }

    @Test
    void testNegativeShort() {
        String text = "Плохо.";
        Map<?, ?> response = restTemplate.getForObject(baseUrl() + "?text={text}", Map.class, text);
        assertThat(response.get("sentiment")).isEqualTo("negative");
    }


    @Test
    void testEmptyText() {
        String text = "";
        Map<?, ?> response = restTemplate.getForObject(baseUrl() + "?text={text}", Map.class, text);
        // Модель может вернуть "neutral" или "negative" на пустой строке — проверим, что не упало
        assertThat(response).isNotNull();
        assertThat(response.get("sentiment")).isIn("negative", "neutral", "positive");
    }

    @Test
    void testVeryLongText() {
        // Создаём текст длиной >512 токенов (но для простоты — длинную строку)
        Map<?, ?> response = restTemplate.getForObject(baseUrl() + "?text={text}", Map.class, "Отличный продукт! ".repeat(80));
        assertThat(response).isNotNull();
        assertThat(response.get("sentiment")).isIn("negative", "neutral", "positive");
    }

    @Test
    void testSpecialCharactersAndNumbers() {
        String text = "Цена: 999 руб. — это отлично.";
        Map<?, ?> response = restTemplate.getForObject(baseUrl() + "?text={text}", Map.class, text);
        assertThat(response).isNotNull();
        assertThat(response.get("sentiment")).isEqualTo("positive");
    }
}
