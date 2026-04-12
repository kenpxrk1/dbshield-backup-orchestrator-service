package db.shield.backup.orchestrator.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@ComponentScan(basePackages = "db.shield")
@EnableJpaAuditing
public class DbshieldBackupOrchestratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbshieldBackupOrchestratorServiceApplication.class, args);
    }

}
