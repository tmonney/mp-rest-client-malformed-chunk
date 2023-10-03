# mp-rest-client-malformed-chunk

This reproducer is a very simple Quarkus application that exposes endpoints in `ServerResource` that use the `ClientResource` Rest client to forward calls to Wiremock.

```
                            Quarkus application
                    ------------------------------------
RestAssured test -> | ServerResource -> ClientResource | -> Wiremock
                    ------------------------------------
```

The bug is demonstrated by having Wiremock simulate a `Fault` where:
* the `Transfer-Encoding: chunked` header is set
* some garbage is written as the body, with the important thing that there is no chunk length line
* the connection is closed by the server

The test expects an `HttpClosedConnection` to be thrown, but with the current Quarkus version the call never completes, so the test instead gets `TimeoutException` (from MP Fault Tolerance).

The test also demonstrates that the `@Bulkhead`' semaphore permit is not correctly released (see `com.example.MalformedChunkTest#restClientUni_malformedChunk_exhaustBulkhead`).
