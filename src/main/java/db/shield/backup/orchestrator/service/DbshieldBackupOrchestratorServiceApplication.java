package db.shield.backup.orchestrator.service;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@ComponentScan(basePackages = "db.shield")
@EnableJpaAuditing
@EnableFeignClients
public class DbshieldBackupOrchestratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbshieldBackupOrchestratorServiceApplication.class, args);
    }

}
