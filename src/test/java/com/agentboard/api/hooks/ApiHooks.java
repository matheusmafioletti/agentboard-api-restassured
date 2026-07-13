package com.agentboard.api.hooks;

import com.agentboard.api.support.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;

public class ApiHooks {

  private static final String KEY_LAST_RESPONSE_BODY = "lastResponseBody";

  @Before
  public void setUp(Scenario scenario) {
    ScenarioContext.clear();
  }

  @After
  public void tearDown(Scenario scenario) {
    System.out.printf("[API] %s — %s%n", scenario.getName(), scenario.getStatus());

    if (scenario.isFailed() && ScenarioContext.contains(KEY_LAST_RESPONSE_BODY)) {
      String body = ScenarioContext.get(KEY_LAST_RESPONSE_BODY, String.class);
      Allure.addAttachment(
          "Last HTTP Response Body",
          "application/json",
          body,
          ".json"
      );
    }
  }
}
