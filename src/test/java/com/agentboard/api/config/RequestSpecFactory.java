package com.agentboard.api.config;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.aeonbits.owner.ConfigFactory;

public final class RequestSpecFactory {

  private static final Environment ENV = ConfigFactory.create(Environment.class, System.getProperties());

  private RequestSpecFactory() {}

  public static RequestSpecification authSpec() {
    return baseSpec(ENV.authBaseUrl()).build();
  }

  public static RequestSpecification boardSpec() {
    return baseSpec(ENV.boardBaseUrl()).build();
  }

  public static RequestSpecification authSpec(String token) {
    return baseSpec(ENV.authBaseUrl())
        .addHeader("Authorization", "Bearer " + token)
        .build();
  }

  public static RequestSpecification boardSpec(String token) {
    return baseSpec(ENV.boardBaseUrl())
        .addHeader("Authorization", "Bearer " + token)
        .build();
  }

  public static RequestSpecification boardSpec(String token, String tenantId) {
    return baseSpec(ENV.boardBaseUrl())
        .addHeader("Authorization", "Bearer " + token)
        .addHeader("X-Tenant-Id", tenantId)
        .build();
  }

  private static RequestSpecBuilder baseSpec(String baseUri) {
    return new RequestSpecBuilder()
        .setBaseUri(baseUri)
        .setContentType(ContentType.JSON)
        .setAccept(ContentType.JSON)
        .setConfig(
            io.restassured.config.RestAssuredConfig.config()
                .httpClient(
                    io.restassured.config.HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", ENV.requestTimeoutMs())
                        .setParam("http.socket.timeout", ENV.requestTimeoutMs())
                )
        );
  }
}
