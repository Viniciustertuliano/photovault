package com.PhotoVault;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14-alpine"))
            .withDatabaseName("photovault_test");

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.4.0"))
            .withServices(LocalStackContainer.Service.S3);

    @DynamicPropertySource
    static void overrrideAwsProperties(DynamicPropertyRegistry registry) {
        registry.add("AWS_S3_ENDPOINT", () -> localStack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        registry.add("AWS_REGION", localStack::getRegion);
        registry.add("AWS_ACCESS_KEY_ID", localStack::getAccessKey);
        registry.add("AWS_SECRET_ACCESS_KEY", localStack::getSecretKey);
        registry.add("AWS_S3_BUCKET_NAME", () -> "photovault-test-bucket");

        registry.add("storage.type", () -> "s3");
    }
}
