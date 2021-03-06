/**
 * Copyright 2017 Pivotal Software, Inc.
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
package io.micrometer.spring.web.client;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MetricsRestTemplateCustomizer}.
 *
 * @author Jon Schneider
 */
public class MetricsRestTemplateCustomizerTest {

    @Test
    public void interceptRestTemplate() {
        MeterRegistry registry = new SimpleMeterRegistry();
        RestTemplate restTemplate = new RestTemplate();
        MetricsRestTemplateCustomizer customizer = new MetricsRestTemplateCustomizer(
            registry, new DefaultRestTemplateExchangeTagsProvider(),
            "http.client.requests", true);
        customizer.customize(restTemplate);

        MockRestServiceServer mockServer = MockRestServiceServer
            .createServer(restTemplate);
        mockServer.expect(MockRestRequestMatchers.requestTo("/test/123"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess("OK",
                MediaType.APPLICATION_JSON));

        String result = restTemplate.getForObject("/test/{id}", String.class, 123);

        assertThat(registry.find("http.client.requests").meters())
            .anySatisfy(m -> assertThat(stream(m.getId().getTags().spliterator(), false).map(Tag::getKey)).doesNotContain("bucket"));

        assertThat(registry.find("http.client.requests")
            .tags("method", "GET", "uri", "/test/{id}", "status", "200")
            .value(Statistic.Count, 1.0).timer()).isPresent();

//        assertThat(registry.find("http.client.requests").meters()
//            .stream().flatMap((m) -> StreamSupport
//                .stream(m.getId().getTags().spliterator(), false))
//            .map(Tag::getKey)).contains("bucket");

        assertThat(result).isEqualTo("OK");

        mockServer.verify();
    }
}
