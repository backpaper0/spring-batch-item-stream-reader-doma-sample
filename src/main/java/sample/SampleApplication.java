package sample;

import org.flywaydb.core.Flyway;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    @Bean
    ItemProcessor<Book, String> processor() {
        return item -> item != null ? item.title : null;
    }

    @Bean
    ItemWriter<String> writer() {
        return items -> System.out.println(items);
    }

    @Bean
    Step step(StepBuilderFactory steps, BookReader reader) {
        SimpleStepBuilder<Book, String> builder = steps.get("step1").chunk(2);
        return builder.reader(reader).processor(processor()).writer(writer()).build();
    }

    @Bean
    Job job(JobBuilderFactory jobs, Step step) {
        return jobs.get("job1").start(step).build();
    }

    @Bean
    FlywayMigrationInitializer flywayMigrationInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway);
    }
}
