package db.shield.backup.orchestrator.service.outbox;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "outbox")
public class OutboxProperties {

    private boolean enabled = true;
    private Dispatcher dispatcher = new Dispatcher();

    @Getter
    @Setter
    public static class Dispatcher {
        private long fixedDelayMs = 2000;
        private int batchSize = 50;
        private int maxAttempts = 50;
        private long initialBackoffMs = 1000;
        private long maxBackoffMs = 60000;
    }
}
