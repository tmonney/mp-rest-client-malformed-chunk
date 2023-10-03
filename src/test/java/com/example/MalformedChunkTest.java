package com.example;

import com.example.ClientResource.Dto;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@QuarkusTest
@QuarkusTestResource(WireMockExtensions.class)
public class MalformedChunkTest {

    private static final Logger logger = LoggerFactory.getLogger(MalformedChunkTest.class);

    @Test
    public void restClient_ok() {
        var response = given()
                .when().get("/rest-client/ok")
                .then()
                .statusCode(200)
                .extract().body()
                .as(Dto.class);
        assertThat(response, equalTo(new Dto("foo", "bar")));
    }

    /**
     * This test fails because instead of the expected HttpClosedException, we get a TimeoutException
     */
    @Test
    public void restClient_malformedChunk() {
        var response = given()
                .when().get("/rest-client/malformed-chunk")
                .then()
                .statusCode(200);
        var time = response.extract().time();
        var dto = response.extract().body().as(Dto.class);
        assertAll(
                () -> assertThat(time, lessThan(500L)),
                () -> assertThat(dto, equalTo(new Dto("error", "HttpClosedException")))
        );
    }

    @Test
    public void restClientCompletionStage_ok() {
        var response = given()
                .when().get("/rest-client-cs/ok")
                .then()
                .statusCode(200)
                .extract().body()
                .as(Dto.class);
        assertThat(response, equalTo(new Dto("foo", "bar")));
    }

    /**
     * This test fails because instead of the expected HttpClosedException, we get a TimeoutException
     */
    @Test
    public void restClientCompletionStage_malformedChunk() {
        var response = given()
                .when().get("/rest-client-cs/malformed-chunk")
                .then()
                .statusCode(200);
        var time = response.extract().time();
        var dto = response.extract().body().as(Dto.class);
        assertAll(
                () -> assertThat(time, lessThan(500L)),
                () -> assertThat(dto, equalTo(new Dto("error", "HttpClosedException")))
        );
    }

    @Test
    public void restClientUni_ok() {
        var response = given()
                .when().get("/rest-client-uni/ok")
                .then()
                .statusCode(200)
                .extract().body()
                .as(Dto.class);
        assertThat(response, equalTo(new Dto("foo", "bar")));
    }

    /**
     * This test fails because instead of the expected HttpClosedException, we get a TimeoutException
     */
    @Test
    public void restClientUni_malformedChunk() {
        var response = given()
                .when().get("/rest-client-uni/malformed-chunk")
                .then()
                .statusCode(200);
        var time = response.extract().time();
        var dto = response.extract().body().as(Dto.class);
        assertAll(
                () -> assertThat(time, lessThan(500L)),
                () -> assertThat(dto, equalTo(new Dto("error", "HttpClosedException")))
        );
    }

    @Test
    public void restClientUni_malformedChunk_exhaustBulkhead() {
        var responses = Stream.generate(() -> given()
                        .when().get("/rest-client-uni-bulkhead/malformed-chunk")
                        .then()
                        //.statusCode(500)
                        .extract().body().as(Dto.class))
                .limit(10)
                .toList();

        responses.forEach(dto -> logger.info("Response: {}", dto));

        // first 4 requests time out
        // all next requests are rejected by the bulkhead (3 slots + 1 in the queue)
        assertThat(responses, contains(
                new Dto("error", "TimeoutException"),
                new Dto("error", "TimeoutException"),
                new Dto("error", "TimeoutException"),
                new Dto("error", "TimeoutException"),
                new Dto("error", "BulkheadException"),
                new Dto("error", "BulkheadException"),
                new Dto("error", "BulkheadException"),
                new Dto("error", "BulkheadException"),
                new Dto("error", "BulkheadException"),
                new Dto("error", "BulkheadException")
        ));
    }

    @Test
    public void webClient_ok() {
        var response = given()
                .when().get("/web-client/ok")
                .then()
                .statusCode(200)
                .extract().body()
                .as(Dto.class);
        assertThat(response, equalTo(new Dto("foo", "bar")));
    }

    @Test
    public void webClient_malformedChunk() {
        var response = given()
                .when().get("/web-client/malformed-chunk")
                .then()
                .statusCode(200);
        var time = response.extract().time();
        var dto = response.extract().body().as(Dto.class);
        assertAll(
                () -> assertThat(time, lessThan(500L)),
                () -> assertThat(dto, equalTo(new Dto("error", "HttpClosedException")))
        );
    }

    @Test
    public void httpClient_ok() {
        var response = given()
                .when().get("/http-client/ok")
                .then()
                .statusCode(200)
                .extract().body()
                .as(Dto.class);
        assertThat(response, equalTo(new Dto("foo", "bar")));
    }

    @Test
    public void httpClient_malformedChunk() {
        var response = given()
                .when().get("/http-client/malformed-chunk")
                .then()
                .statusCode(200);
        var time = response.extract().time();
        var dto = response.extract().body().as(Dto.class);
        assertAll(
                () -> assertThat(time, lessThan(500L)),
                () -> assertThat(dto, equalTo(new Dto("error", "HttpClosedException")))
        );
    }
}