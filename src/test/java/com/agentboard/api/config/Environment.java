package com.agentboard.api.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources("classpath:environments/${env}.properties")
public interface Environment extends Config {

  @Key("base.url.auth")
  @DefaultValue("http://localhost:8080")
  String authBaseUrl();

  @Key("base.url.board")
  @DefaultValue("http://localhost:8081")
  String boardBaseUrl();

  @Key("request.timeout.ms")
  @DefaultValue("5000")
  int requestTimeoutMs();
}
