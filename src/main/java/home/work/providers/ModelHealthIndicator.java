package home.work.providers;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ModelHealthIndicator implements HealthIndicator {
    private volatile boolean modelLoaded = false;

    public void modelLoaded() {
        this.modelLoaded = true;
    }

    @Override
    public Health health() {
        if (modelLoaded) {
            return Health.up().withDetail("model", "loaded").build();
        } else {
            return Health.down().withDetail("model", "not loaded").build();
        }
    }
}
