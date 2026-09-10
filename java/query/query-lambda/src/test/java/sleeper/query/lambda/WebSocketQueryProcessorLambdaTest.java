/*
 * Copyright 2022-2026 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sleeper.query.lambda;

import org.junit.jupiter.api.Test;

import sleeper.query.core.model.Query;
import sleeper.query.core.model.QueryProcessingConfig;
import sleeper.query.core.output.ResultsOutput;
import sleeper.query.runner.output.WebSocketOutput;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketQueryProcessorLambdaTest {

    private static final String ENDPOINT = "https://example.test/stage";
    private static final String CONNECTION_ID = "connection-123";

    @Test
    void shouldConfigureImmutableEmptyResultsPublisherConfig() {
        // Given
        Query query = queryWithResultsPublisherConfig(Map.of());

        // When
        Query updated = WebSocketQueryProcessorLambda.withWebSocketResultsPublisherConfig(query, ENDPOINT, CONNECTION_ID);

        // Then
        assertThat(updated.getResultsPublisherConfig()).containsExactlyInAnyOrderEntriesOf(Map.of(
                ResultsOutput.DESTINATION, WebSocketOutput.DESTINATION_NAME,
                WebSocketOutput.ENDPOINT, ENDPOINT,
                WebSocketOutput.CONNECTION_ID, CONNECTION_ID));
    }

    @Test
    void shouldPreserveExistingPublisherConfigWhenDefaultingToWebSocket() {
        // Given
        Query query = queryWithResultsPublisherConfig(Map.of("custom-key", "custom-value"));

        // When
        Query updated = WebSocketQueryProcessorLambda.withWebSocketResultsPublisherConfig(query, ENDPOINT, CONNECTION_ID);

        // Then
        assertThat(updated.getResultsPublisherConfig()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "custom-key", "custom-value",
                ResultsOutput.DESTINATION, WebSocketOutput.DESTINATION_NAME,
                WebSocketOutput.ENDPOINT, ENDPOINT,
                WebSocketOutput.CONNECTION_ID, CONNECTION_ID));
    }

    @Test
    void shouldRefreshExistingWebSocketConnectionDetails() {
        // Given
        Query query = queryWithResultsPublisherConfig(Map.of(
                ResultsOutput.DESTINATION, WebSocketOutput.DESTINATION_NAME,
                WebSocketOutput.ENDPOINT, "https://old.example/stage",
                WebSocketOutput.CONNECTION_ID, "old-connection",
                "custom-key", "custom-value"));

        // When
        Query updated = WebSocketQueryProcessorLambda.withWebSocketResultsPublisherConfig(query, ENDPOINT, CONNECTION_ID);

        // Then
        assertThat(updated.getResultsPublisherConfig()).containsExactlyInAnyOrderEntriesOf(Map.of(
                ResultsOutput.DESTINATION, WebSocketOutput.DESTINATION_NAME,
                WebSocketOutput.ENDPOINT, ENDPOINT,
                WebSocketOutput.CONNECTION_ID, CONNECTION_ID,
                "custom-key", "custom-value"));
    }

    @Test
    void shouldNotOverrideExplicitNonWebSocketDestination() {
        // Given
        Map<String, String> config = Map.of(
                ResultsOutput.DESTINATION, "other-destination",
                "custom-key", "custom-value");
        Query query = queryWithResultsPublisherConfig(config);

        // When
        Query updated = WebSocketQueryProcessorLambda.withWebSocketResultsPublisherConfig(query, ENDPOINT, CONNECTION_ID);

        // Then
        assertThat(updated).isSameAs(query);
        assertThat(updated.getResultsPublisherConfig()).isEqualTo(config);
    }

    private static Query queryWithResultsPublisherConfig(Map<String, String> resultsPublisherConfig) {
        return Query.builder()
                .tableName("test-table")
                .regions(List.of())
                .processingConfig(QueryProcessingConfig.builder()
                        .resultsPublisherConfig(resultsPublisherConfig)
                        .build())
                .build();
    }
}
