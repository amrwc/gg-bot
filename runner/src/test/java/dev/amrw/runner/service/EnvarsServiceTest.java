package dev.amrw.runner.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EnvarsServiceTest {

    @Spy
    private EnvarsService service;

    @ParameterizedTest
    @MethodSource
    @DisplayName("Should have verified environment variables")
    void shouldHaveVerifiedEnvars(
            final Map<String, String> env,
            final List<String> requiredEnvars,
            final boolean expectedResult
    ) {
        doReturn(env).when(service).getEnv();
        assertThat(service.verifyEnvars(requiredEnvars)).isEqualTo(expectedResult);
    }

    static Stream<Arguments> shouldHaveVerifiedEnvars() {
        return Stream.of(
                Arguments.of(Map.of(), List.of(), true),
                Arguments.of(Map.of("foo", "123"), List.of(), true),
                Arguments.of(Map.of("foo", "123"), List.of("foo"), true),
                Arguments.of(Map.of("foo", "123", "bar", "456"), List.of("bar"), true),
                Arguments.of(Map.of("foo", "123", "bar", "456"), List.of("foo", "bar"), true),

                Arguments.of(Map.of(), List.of("foo"), false),
                Arguments.of(Map.of("foo", "123"), List.of("bar"), false),
                Arguments.of(Map.of("foo", ""), List.of("foo"), false),
                Arguments.of(Map.of("foo", "   "), List.of("foo"), false)
        );
    }
}
