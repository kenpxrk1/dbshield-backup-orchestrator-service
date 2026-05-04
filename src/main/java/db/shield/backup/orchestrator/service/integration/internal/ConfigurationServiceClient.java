package db.shield.backup.orchestrator.service.integration.internal;

import db.shield.backup.orchestrator.service.dto.integration.response.DatabaseConfigurationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "configuration-service",
        url = "${integration.internal.dbshield.configuration-service-url}"
)
public interface ConfigurationServiceClient {
    @GetMapping("/api/internal/configs/{id}")
    DatabaseConfigurationResponse getById(@PathVariable UUID id);
}
