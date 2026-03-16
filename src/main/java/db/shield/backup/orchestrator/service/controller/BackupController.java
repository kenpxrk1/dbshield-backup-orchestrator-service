package db.shield.backup.orchestrator.service.controller;


import db.shield.backup.orchestrator.service.dto.request.CreateBackupRequest;
import db.shield.backup.orchestrator.service.dto.response.BackupJobResponse;
import db.shield.backup.orchestrator.service.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/backups")
@RequiredArgsConstructor
@Tag(name = "Backup Controller", description = "Operations for managing database backup jobs")
public class BackupController {

    private final BackupService backupService;

    @Operation(
            summary = "Create backup job",
            description = "Creates a new backup job for the specified database. " +
                    "The job will be scheduled and executed asynchronously by workers."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Backup job successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    public ResponseEntity<BackupJobResponse> createBackup(
            @RequestBody @Valid CreateBackupRequest request) {

        BackupJobResponse response = backupService.createBackup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get backup job by ID",
            description = "Returns detailed information about a backup job including status and timestamps."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Backup job found"),
            @ApiResponse(responseCode = "404", description = "Backup job not found")
    })
    @GetMapping("/{jobId}")
    public ResponseEntity<BackupJobResponse> getBackup(
            @Parameter(description = "Backup job ID", required = true)
            @PathVariable UUID jobId) {

        BackupJobResponse response = backupService.getBackup(jobId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all backup jobs",
            description = "Returns a list of all backup jobs created in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of backup jobs")
    })
    @GetMapping
    public ResponseEntity<List<BackupJobResponse>> getAllBackups() {

        List<BackupJobResponse> backups = backupService.getAllBackups();

        return ResponseEntity.ok(backups);
    }

    @Operation(
            summary = "Cancel backup job",
            description = "Cancels a backup job if it has not been completed yet."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Backup job successfully cancelled"),
            @ApiResponse(responseCode = "404", description = "Backup job not found"),
            @ApiResponse(responseCode = "409", description = "Backup job cannot be cancelled")
    })
    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> cancelBackup(
            @Parameter(description = "Backup job ID", required = true)
            @PathVariable UUID jobId) {

        backupService.cancelBackup(jobId);

        return ResponseEntity.noContent().build();
    }

}
