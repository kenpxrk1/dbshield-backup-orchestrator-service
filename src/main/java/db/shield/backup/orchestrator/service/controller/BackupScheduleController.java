package db.shield.backup.orchestrator.service.controller;

import db.shield.backup.orchestrator.service.dto.request.CreateScheduleRequest;
import db.shield.backup.orchestrator.service.service.scheduler.BackupScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "Backup Schedule Controller", description = "Operations for managing backup schedules")
public class BackupScheduleController {

    private final BackupScheduleService scheduleService;

    @Operation(
            summary = "Create backup schedule",
            description = "Creates a new backup schedule using a cron expression. " +
                    "The scheduler will automatically trigger backup jobs based on this configuration."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Schedule successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid cron expression or request body")
    })
    @PostMapping
    public ResponseEntity<Void> createSchedule(@RequestBody @Valid CreateScheduleRequest request) {
        scheduleService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();    }
}
