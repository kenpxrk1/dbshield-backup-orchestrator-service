package db.shield.backup.orchestrator.service.config.outbox;

import db.shield.backup.orchestrator.service.outbox.OutboxProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxConfiguration {
}
