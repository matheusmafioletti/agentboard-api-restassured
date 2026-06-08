package com.agentboard.api.steps;

import com.agentboard.api.config.RequestSpecFactory;
import com.agentboard.api.support.ScenarioContext;
import com.agentboard.api.support.TokenStore;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Step definitions for board-service work-item operations.
 *
 * <p>Depends on {@link AuthSteps} having already populated {@link TokenStore} via an
 * upstream {@code Given I am authenticated as} step.
 */
public class BoardSteps {

  private static final String KEY_RESPONSE = "boardResponse";
  private static final String KEY_WORK_ITEM_ID = "workItemId";
  private static final String KEY_PROJECT_ID = "projectId";

  /**
   * Authenticates as the given user by performing a fresh login and saving the resulting
   * token and tenant ID into the scenario context.
   *
   * <p>This step assumes the user was already registered in a prior {@code @Background} or
   * dependent scenario. The password is fixed to the integration-test default.
   *
   * @param email the user's email address
   */
  @Given("I am authenticated as {string}")
  public void iAmAuthenticatedAs(String email) {
    Response loginResponse = given()
        .spec(RequestSpecFactory.authSpec())
        .body(Map.of("email", email, "password", "secret123"))
        .when()
        .post("/auth/login")
        .then()
        .extract()
        .response();

    ScenarioContext.set("lastResponseBody", loginResponse.getBody().asString());

    if (loginResponse.statusCode() == 200) {
      String token = loginResponse.jsonPath().getString("accessToken");
      String tenantId = loginResponse.jsonPath().getString("tenantId");
      if (token != null) {
        TokenStore.saveToken(token);
      }
      if (tenantId != null) {
        TokenStore.saveTenantId(tenantId);
      }
    }
  }

  /**
   * Sends a POST request to create a work item with the given title under the currently
   * active project. The project ID must already be stored in the scenario context under
   * key {@code projectId}.
   *
   * @param title the work-item title
   */
  @When("I create a work item with title {string}")
  public void iCreateAWorkItemWithTitle(String title) {
    String token = TokenStore.getToken();
    String tenantId = TokenStore.getTenantId();
    String projectId = ScenarioContext.get(KEY_PROJECT_ID, String.class);

    Map<String, Object> body = projectId != null
        ? Map.of("title", title, "projectId", projectId)
        : Map.of("title", title);

    Response response = given()
        .spec(RequestSpecFactory.boardSpec(token, tenantId))
        .body(body)
        .when()
        .post("/api/work-items")
        .then()
        .extract()
        .response();

    ScenarioContext.set(KEY_RESPONSE, response);
    ScenarioContext.set("lastResponseBody", response.getBody().asString());

    if (response.statusCode() == 201) {
      String workItemId = response.jsonPath().getString("id");
      if (workItemId != null) {
        ScenarioContext.set(KEY_WORK_ITEM_ID, workItemId);
      }
    }
  }

  /** Asserts that the most recent work-item creation returned HTTP 201. */
  @Then("the work item should be created successfully")
  public void theWorkItemShouldBeCreatedSuccessfully() {
    Response response = ScenarioContext.get(KEY_RESPONSE, Response.class);
    assertEquals(
        201,
        response.statusCode(),
        () -> "Expected 201 Created but got " + response.statusCode()
            + ". Body: " + response.getBody().asString()
    );
    String id = response.jsonPath().getString("id");
    assertNotNull(id, "Expected a work-item ID in the response body");
  }

  /** Asserts that the returned work item carries the expected status value. */
  @Then("the work item should have status {string}")
  public void theWorkItemShouldHaveStatus(String expectedStatus) {
    Response response = ScenarioContext.get(KEY_RESPONSE, Response.class);
    String actualStatus = response.jsonPath().getString("status");
    assertEquals(
        expectedStatus,
        actualStatus,
        () -> "Expected work-item status \"" + expectedStatus + "\" but got \""
            + actualStatus + "\". Body: " + response.getBody().asString()
    );
  }

  /** Updates the status of the previously created work item. */
  @When("I update the work item status to {string}")
  public void iUpdateTheWorkItemStatusTo(String newStatus) {
    String token = TokenStore.getToken();
    String tenantId = TokenStore.getTenantId();
    String workItemId = ScenarioContext.get(KEY_WORK_ITEM_ID, String.class);

    assertNotNull(workItemId, "No work-item ID in context — create a work item first");

    Response response = given()
        .spec(RequestSpecFactory.boardSpec(token, tenantId))
        .body(Map.of("status", newStatus))
        .when()
        .patch("/api/work-items/" + workItemId)
        .then()
        .extract()
        .response();

    ScenarioContext.set(KEY_RESPONSE, response);
    ScenarioContext.set("lastResponseBody", response.getBody().asString());
  }

  /** Lists all work items visible to the authenticated user. */
  @When("I list all work items")
  public void iListAllWorkItems() {
    String token = TokenStore.getToken();
    String tenantId = TokenStore.getTenantId();

    Response response = given()
        .spec(RequestSpecFactory.boardSpec(token, tenantId))
        .when()
        .get("/api/work-items")
        .then()
        .extract()
        .response();

    ScenarioContext.set(KEY_RESPONSE, response);
    ScenarioContext.set("lastResponseBody", response.getBody().asString());
  }

  /** Asserts that the list response contains at least one work item. */
  @Then("the response should contain at least one work item")
  @And("the response should contain at least one work item")
  public void theResponseShouldContainAtLeastOneWorkItem() {
    Response response = ScenarioContext.get(KEY_RESPONSE, Response.class);
    assertEquals(200, response.statusCode());
    int size = response.jsonPath().getList("$").size();
    assertNotNull(size > 0, "Expected at least one work item in the list");
  }
}
