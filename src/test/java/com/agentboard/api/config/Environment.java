package com.agentboard.api.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

/**
 * Provides environment-specific configuration loaded from {@code environments/${env}.properties}.
 *
 * <p>Set the {@code env} system property to switch between environments (e.g. {@code -Denv=staging}).
 * Falls back to {@code local} defaults when the property file is absent.
 */
@Sources("classpath:environments/${env}.properties")
public interface Environment extends Config {

  /** Base URL for the auth-service (default: {@code http://localhost:8080}). */
  @Key("base.url.auth")
  @DefaultValue("http://localhost:8080")
  String authBaseUrl();

  /** Base URL for the board-service (default: {@code http://localhost:8081}). */
  @Key("base.url.board")
  @DefaultValue("http://localhost:8081")
  String boardBaseUrl();

  /**
   * Maximum time in milliseconds to wait for an HTTP response.
   *
   * @return timeout in milliseconds
   */
  @Key("request.timeout.ms")
  @DefaultValue("5000")
  int requestTimeoutMs();
}
