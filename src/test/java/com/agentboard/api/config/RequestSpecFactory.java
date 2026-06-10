package com.agentboard.api.config;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.aeonbits.owner.ConfigFactory;

/**
 * Builds pre-configured {@link RequestSpecification} instances for each backend service.
 *
 * <p>All specs share a common baseline (Content-Type, Accept, timeout) and differ only
 * in the target {@code baseUri}.
 */
public final class RequestSpecFactory {

  private static final Environment ENV = ConfigFactory.create(Environment.class, System.getProperties());

  private RequestSpecFactory() {}

  /**
   * Returns a {@link RequestSpecification} targeting the auth-service base URL.
   *
   * @return ready-to-use spec for auth endpoints
   */
  public static RequestSpecification authSpec() {
    return baseSpec(ENV.authBaseUrl()).build();
  }

  /**
   * Returns a {@link RequestSpecification} targeting the board-service base URL.
   *
   * @return ready-to-use spec for board endpoints
   */
  public static RequestSpecification boardSpec() {
    return baseSpec(ENV.boardBaseUrl()).build();
  }

  /**
   * Returns a {@link RequestSpecification} targeting the auth-service with a Bearer token.
   *
   * @param token JWT Bearer token
   * @return authenticated spec for protected auth endpoints
   */
  public static RequestSpecification authSpec(String token) {
    return baseSpec(ENV.authBaseUrl())
        .addHeader("Authorization", "Bearer " + token)
        .build();
  }

  /**
   * Returns a {@link RequestSpecification} targeting the board-service with a Bearer token.
   *
   * @param token JWT Bearer token (tenant is resolved from the JWT claim)
   * @return authenticated spec for board endpoints
   */
  public static RequestSpecification boardSpec(String token) {
    return baseSpec(ENV.boardBaseUrl())
        .addHeader("Authorization", "Bearer " + token)
        .build();
  }

  /**
   * Returns a {@link RequestSpecification} targeting the board-service with a Bearer token
   * and tenant header already set.
   *
   * @param token    JWT Bearer token
   * @param tenantId tenant identifier to send as {@code X-Tenant-Id}
   * @return authenticated spec for board endpoints
   */
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
