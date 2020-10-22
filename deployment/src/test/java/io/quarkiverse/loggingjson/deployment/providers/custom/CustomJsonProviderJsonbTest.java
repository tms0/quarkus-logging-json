package io.quarkiverse.loggingjson.deployment.providers.custom;

import java.util.Arrays;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import io.quarkiverse.loggingjson.deployment.JsonFormatterBaseTest;
import io.quarkus.bootstrap.model.AppArtifact;
import io.quarkus.test.QuarkusUnitTest;

class CustomJsonProviderJsonbTest extends JsonFormatterBaseTest {
    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(FirstCustomJsonProvider.class, SecondCustomJsonProvider.class, ThirdCustomJsonProvider.class))
            .setForcedDependencies(Arrays.asList(
                    new AppArtifact("io.quarkus", "quarkus-jsonb-deployment", System.getProperty("test.quarkus.version")),
                    new AppArtifact("org.jboss.slf4j", "slf4j-jboss-logging", "1.2.1.Final") // FIXME Remove when quarkus is updated
            ))
            .withConfigurationResource("application-json.properties");

    @Inject
    FirstCustomJsonProvider firstCustomJsonProvider;

    @Inject
    SecondCustomJsonProvider secondCustomJsonProvider;

    @Inject
    ThirdCustomJsonProvider thirdCustomJsonProvider;

    @Test
    void testCustomJsonProvider() throws Exception {
        org.slf4j.Logger log = LoggerFactory.getLogger("JsonStructuredTest");

        log.info("testCustomJsonProvider");

        String[] lines = logLines();
        Assertions.assertEquals(1, lines.length);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readValue(lines[0], JsonNode.class);
        Assertions.assertTrue(jsonNode.isObject());

        ImmutableList<String> fields = ImmutableList.copyOf(jsonNode.fieldNames());
        Assertions.assertTrue(fields.contains("first"));
        Assertions.assertEquals(1, firstCustomJsonProvider.getWriteToNumberOfCalls());

        Assertions.assertTrue(fields.contains("second"));
        Assertions.assertEquals(1, secondCustomJsonProvider.getIsEnabledNumberOfCalls());
        Assertions.assertEquals(1, secondCustomJsonProvider.getWriteToNumberOfCalls());

        Assertions.assertFalse(fields.contains("third"));
        Assertions.assertEquals(1, thirdCustomJsonProvider.getIsEnabledNumberOfCalls());
        Assertions.assertEquals(0, thirdCustomJsonProvider.getWriteToNumberOfCalls());
    }
}
