# 🧩 Service Overview

- **Name:** dbshield-backup-orchestrator-service
- **High-level purpose:** Orchestrates database backup jobs by creating work items, coordinating execution with workers over Kafka, and persisting outcomes for auditability and monitoring.
- **Key responsibility:** Accept backup job requests via REST.
- **Key responsibility:** Persist backup job state and lifecycle timestamps.
- **Key responsibility:** Publish backup request events to Kafka.
- **Key responsibility:** Consume worker events (started, completed, failed) and update job state.
- **Key responsibility:** Persist backup results and failure details.
- **Key responsibility:** Apply retry logic for transient failures.
- **Problem it solves:** Provides a reliable, auditable control plane for distributed backup execution, decoupling API requests from long-running backup work.

# 🏗 Architecture

- **Architectural style:** Layered architecture (Controller → Service → Repository → Database) with Kafka producer/consumer integration.
- **Controller layer:** `BackupController` exposes REST endpoints for creating, listing, retrieving, and canceling backup jobs.
- **Service layer:** `BackupServiceImpl` contains orchestration logic, state transitions, retry policy, and event publishing.
- **Repository layer:** Spring Data JPA repositories for `BackupJobEntity`, `BackupResultEntity`, and `BackupScheduleEntity`.
- **DTO / mapping layer:** MapStruct-based `BackupJobMapper` for request/response and result mapping.
- **Configurations:** Kafka producer/consumer setup, error handling with DLT routing, and Liquibase migrations.
- **Key design decision:** Event-driven orchestration decouples API requests from worker execution.
- **Key design decision:** Idempotent producer settings (`enable.idempotence=true`, `acks=all`) reduce duplicate event risk.
- **Key design decision:** State checks on updates prevent duplicate or out-of-order event application.
- **Key design decision:** DLT error handling routes failed Kafka processing to `*.DLT` topics with backoff.

# 🔄 Business Logic

- **Create backup job:** Client sends `CreateBackupRequest` with `databaseId` → service creates `backup_job` with `REQUESTED` and `requested_at` → service publishes `backup.job.requested`.
- **Why it exists:** The API must respond quickly while workers perform long-running backups asynchronously.
- **Start backup job:** Worker publishes `backup.job.started` → service updates job to `STARTED` and sets `started_at`.
- **Why it exists:** Start events provide operational visibility and enable SLA tracking.
- **Complete backup job:** Worker publishes `backup.job.completed` → service sets status `COMPLETED`, sets `completed_at`, and stores a `backup_result`.
- **Why it exists:** Results are persisted for audit, download references, and reporting.
- **Fail backup job:** Worker publishes `backup.job.failed` with `retryable` → service retries if under limit, otherwise marks job `FAILED` and stores error.
- **Why it exists:** Transient failures are retried automatically while preserving failure context.

# 🔗 Integrations

- **Kafka (spring-kafka):** Produces `backup.job.requested` and consumes `backup.job.started`, `backup.job.completed`, `backup.job.failed`; used to decouple orchestration from execution and to scale workers independently.
- **PostgreSQL:** Stores job state (`backup_job`), results (`backup_result`), and schedules (`backup_schedule`) for durability and auditability.
- **REST:** `/api/v1/backups` and `/api/v1/backups/{jobId}` provide synchronous control-plane APIs for clients and system integrations.

# 🗄 Data Model

- **backup_job:** `id`, `database_id`, `db_type`, `status`, `requested_at`, `started_at`, `completed_at`, `retry_count`, `error_message`; indexed by `database_id` and `status` for query performance.
- **backup_result:** `id`, `job_id`, `file_path`, `file_size`, `checksum`; indexed by `job_id` for fast result lookup.
- **backup_schedule:** `id`, `database_id`, `cron_expression`, `enabled`; present for scheduled backups and future automation.
- **Schema design rationale:** Results are separated from jobs to keep the primary job table compact; relationships are logical rather than enforced by foreign keys, simplifying migrations at the cost of application-side consistency.

# ⚙️ Configuration

- **Active profile:** `local` (`application.yaml`).
- **Datasource:** `application-local.yaml` sets `spring.datasource.url=jdbc:postgresql://localhost:5432/dbshield`, `username=dbshield`, `password=dbshield`, `hikari.schema=backup_service`.
- **Liquibase:** `spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml`, `spring.liquibase.default-schema=backup_service`.
- **Kafka producer:** Idempotent producer, `acks=all`, high retry count, and delivery timeouts.
- **Kafka consumer:** Manual record ack, explicit group ID, JSON deserialization with trusted packages.
- **Listener concurrency:** Configured at 3 in Kafka container factories; YAML listener concurrency is 6 but does not override those explicit bean settings.

# 🚀 How It Works End-to-End

1. Client calls `POST /api/v1/backups` with `databaseId`.
2. Orchestrator writes a `backup_job` row and emits `backup.job.requested` to Kafka.
3. Worker service consumes the request, starts backup, and emits `backup.job.started`.
4. Orchestrator marks the job `STARTED` and records `started_at`.
5. Worker completes the backup and emits `backup.job.completed` with file metadata.
6. Orchestrator marks the job `COMPLETED` and stores a `backup_result` row.
7. On failure, worker emits `backup.job.failed`; orchestrator retries or marks the job `FAILED` based on retry policy.

# 📈 Scalability & Performance Considerations

- **Kafka decoupling** enables horizontal scaling of orchestrator and workers.
- **Database load** is proportional to job state transitions; indexes support common queries by status and database.
- **Consumer concurrency** is set to 3; tune based on partition count and throughput.
- **Potential bottleneck:** High-frequency state updates can stress the primary database.
- **Potential bottleneck:** Large result metadata growth may require retention or archiving.
- **Suggested improvement:** Add a unique constraint on `backup_result.job_id` to hard-enforce idempotency.
- **Suggested improvement:** Partition Kafka topics by `jobId` to preserve ordering per job.
- **Suggested improvement:** Add metrics and tracing for SLA and throughput visibility.

# 🔐 Security

- **Current state:** No authentication or authorization in the service code.
- **Recommended deployment:** Place behind an API gateway or service mesh with JWT/OAuth enforcement.
- **Sensitive data:** File paths and error messages are stored; access should be restricted at the API layer.

# 🧪 Testing

- **Unit tests only:** Shared `Initializer` prepares reusable DTOs/entities.
- **Mapper tests:** Use MapStruct implementations directly, no Spring context.
- **Service tests:** Use Mockito for repositories and Kafka producer; cover happy paths and failure branches.
- **No integration tests:** `@SpringBootTest` is not used.

# 🧠 Design Decisions & Trade-offs

- **Event-driven orchestration** improves resilience and scalability but requires strict idempotency handling.
- **State-machine logic in `BackupStatus`** makes transitions explicit and consistent.
- **DLT-based error handling** favors operational observability over silent retries.
- **No foreign keys** simplify schema changes but push consistency guarantees into service logic.

# 📦 Role in Microservice Ecosystem

- **Role:** Central control plane for backup operations.
- **Depends on:** Kafka for worker coordination and PostgreSQL for durable state.
- **If it goes down:** New backup requests cannot be created; worker events accumulate in Kafka; state visibility is delayed until recovery.

# 🎤 Presentation Summary (IMPORTANT)

- Orchestrates backup jobs and coordinates workers via Kafka.
- Decouples API calls from long-running backups with event-driven design.
- Persists job state and results for auditability and operational visibility.
- Uses idempotent Kafka producer settings and DLT error handling for resilience.
- Clear layered architecture with REST, service orchestration, and JPA repositories.
- Designed to scale horizontally through Kafka concurrency and stateless services.
- Extensible data model supports scheduled backups and future automation.
