package com.example;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

import static java.time.temporal.ChronoUnit.SECONDS;

@Timeout(value = 5, unit = SECONDS)
@RegisterRestClient(configKey = "my-client")
public interface ClientResource {

    record Dto(String key, String value) {
    }

    @GET
    @Path("/{key}")
    @Produces("application/json")
    Dto get(@PathParam("key") String key);

    @GET
    @Path("/{key}")
    @Produces("application/json")
    CompletionStage<Dto> getCompletionStage(@PathParam("key") String key);

    @GET
    @Path("/{key}")
    @Produces("application/json")
    Uni<Dto> getUni(@PathParam("key") String key);

    @GET
    @Path("/{key}")
    @Produces("application/json")
    @Bulkhead(value = 3, waitingTaskQueue = 1)
    Uni<Dto> getUniWithBulkhead(@PathParam("key") String key);
}
