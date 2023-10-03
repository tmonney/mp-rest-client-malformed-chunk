package com.example;

import com.example.ClientResource.Dto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.concurrent.CompletionStage;

@Path("/")
public class ServerResource {

    @Inject
    @RestClient
    ClientResource client;

    @Inject
    Vertx vertx;

    @Inject
    @ConfigProperty(name = "quarkus.rest-client.my-client.url")
    URL clientBaseUrl;

    @Inject
    ObjectMapper objectMapper;

    @GET
    @Path("/rest-client/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Dto getWithRestClient(@PathParam("key") String key) {
        try {
            return client.get(key);
        } catch (Exception e) {
            return new Dto("error", e.getClass().getSimpleName());
        }
    }

    @GET
    @Path("/rest-client-cs/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<Dto> getWithRestClientCompletionStage(@PathParam("key") String key) {
        return client.getCompletionStage(key).exceptionally(e -> new Dto("error", e.getClass().getSimpleName()));
    }

    @GET
    @Path("/rest-client-uni/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Dto> getWithRestClientUni(@PathParam("key") String key) {
        return client.getUni(key).onFailure().recoverWithItem(e -> new Dto("error", e.getClass().getSimpleName()));
    }

    @GET
    @Path("/rest-client-uni-bulkhead/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Dto> getWithRestClientUniBulkhead(@PathParam("key") String key) {
        return client.getUniWithBulkhead(key).onFailure().recoverWithItem(e -> new Dto("error", e.getClass().getSimpleName()));
    }

    @GET
    @Path("/web-client/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Dto getWithWebClient(@PathParam("key") String key) {
        return WebClient.create(vertx).get(clientBaseUrl.getPort(), clientBaseUrl.getHost(), "/%s".formatted(key))
                .send()
                .map(response -> response.bodyAsJson(Dto.class))
                .otherwise(e -> new Dto("error", e.getClass().getSimpleName()))
                .toCompletionStage()
                .toCompletableFuture()
                .join();
    }

    @GET
    @Path("/http-client/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Dto getWithHttpClient(@PathParam("key") String key) {
        var client = vertx.createHttpClient();
        return client.request(HttpMethod.GET, clientBaseUrl.getPort(), clientBaseUrl.getHost(), "/%s".formatted(key))
                .flatMap(HttpClientRequest::send)
                .flatMap(HttpClientResponse::body)
                .map(body -> {
                    try {
                        return objectMapper.readValue(body.getBytes(), Dto.class);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .otherwise(e -> new Dto("error", e.getClass().getSimpleName()))
                .toCompletionStage()
                .toCompletableFuture()
                .join();
    }
}
