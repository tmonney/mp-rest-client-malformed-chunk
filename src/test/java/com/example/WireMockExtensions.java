package com.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.Options.ChunkedEncodingPolicy.NEVER;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.Fault.MALFORMED_RESPONSE_CHUNK;

public class WireMockExtensions implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(options()
                .dynamicPort()
                .useChunkedTransferEncoding(NEVER)
        );
        wireMockServer.start();

        wireMockServer.stubFor(get(urlPathMatching("/ok")).willReturn(okJson("""
                {
                    "key": "foo",
                    "value": "bar"
                }
                """)));
        wireMockServer.stubFor(get(urlPathMatching("/malformed-chunk")).willReturn(aResponse()
                .withFault(MALFORMED_RESPONSE_CHUNK)));

        return Map.of("quarkus.rest-client.my-client.url", wireMockServer.baseUrl());
    }

    @Override
    public void stop() {
        if (null != wireMockServer) {
            wireMockServer.stop();
        }
    }
}