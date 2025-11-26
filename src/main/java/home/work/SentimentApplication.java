package home.work;

//import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SentimentApplication {
    public static void main(String[] args) {
        SpringApplication.run(SentimentApplication.class, args);
    }
//
//    @Bean
//    public ProcessMemoryMetrics processMemoryMetrics() {
//        return new ProcessMemoryMetrics();
//    }
}
