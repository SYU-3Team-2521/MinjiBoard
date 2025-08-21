package syu.likealion3.hackathon.support;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@ActiveProfiles("test")
@SpringBootTest
@Transactional          // JUnit5 + Spring 테스트에서는 기본이 rollback=true
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTest { }
