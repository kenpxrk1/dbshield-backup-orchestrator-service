package db.shield.backup.orchestrator.service.dto.integration.response;

import db.shield.backup.orchestrator.service.model.constant.DatabaseType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response with database configuration details")
public record DatabaseConfigurationResponse(

        @Schema(description = "Database type", example = "POSTGRESQL")
        DatabaseType dbType
) {}
