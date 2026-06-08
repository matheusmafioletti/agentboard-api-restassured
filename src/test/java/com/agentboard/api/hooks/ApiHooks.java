package com.agentboard.api.hooks;

import com.agentboard.api.support.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import java.nio.charset.StandardCharsets;

/**
 * Cucumber lifecycle hooks that manage scenario isolation and Allure reporting.
 */
public class ApiHooks {

  private static final String KEY_LAST_RESPONSE_BODY = "lastResponseBody";

  /**
   * Resets the per-thread scenario context before each scenario runs.
   *
   * @param scenario current Cucumber scenario (unused, required by Cucumber API)
   */
  @Before
  public void setUp(Scenario scenario) {
    ScenarioContext.clear();
  }

  /**
   * Logs the scenario result and, on failure, attaches the last HTTP response body to
   * the Allure report for diagnostics.
   *
   * @param scenario completed Cucumber scenario
   */
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
