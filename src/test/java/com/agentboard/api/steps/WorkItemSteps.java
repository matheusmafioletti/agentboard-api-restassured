package com.agentboard.api.steps;

import com.agentboard.api.config.RequestSpecFactory;
import com.agentboard.api.support.ScenarioContext;
import com.agentboard.api.support.TestDataFactory;
import com.agentboard.api.support.TokenStore;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for board-service work-item lifecycle and hierarchy flows.
 */
public class WorkItemSteps {

  private static final String KEY_PROJECT_ID = "projectId";
  private static final String KEY_WORK_ITEM_ID = "workItemId";
  private static final String KEY_FEATURE_ID = "featureId";
  private static final String KEY_USER_STORY_ID = "userStoryId";
  private static final String KEY_OTHER_WORK_ITEM_ID = "otherWorkItemId";
  private static final String KEY_OTHER_USER_TOKEN = "otherUserToken";

  /** Creates a work item of the given type with the given title. */
  @When("I create a {word} work item with title {string}")
  public void iCreateAWorkItemWithTypeAndTitle(String type, String title) {
    createWorkItem(type, title, null, TokenStore.getToken());
  }

  /** Creates a FEATURE work item used as a parent for hierarchy scenarios. */
  @Given("a FEATURE work item exists in the current project")
  @When("I create a FEATURE work item for hierarchy setup")
  public void aFeatureWorkItemExistsInTheCurrentProject() {
    createWorkItem("FEATURE", "Parent Feature", null, TokenStore.getToken());
    assertEquals(201, AuthSteps.lastResponse().statusCode());
    ScenarioContext.set(KEY_FEATURE_ID, ScenarioContext.get(KEY_WORK_ITEM_ID, String.class));
  }

  /** Creates a USER_STORY under the current FEATURE parent. */
  @When("I create a USER_STORY under the current feature")
  public void iCreateAUserStoryUnderTheCurrentFeature() {
    createWorkItem("USER_STORY", "Child User Story",
        ScenarioContext.get(KEY_FEATURE_ID, String.class), TokenStore.getToken());
    assertEquals(201, AuthSteps.lastResponse().statusCode());
    ScenarioContext.set(KEY_USER_STORY_ID, ScenarioContext.get(KEY_WORK_ITEM_ID, String.class));
  }

  /** Creates a TASK linked to the current USER_STORY parent. */
  @When("I create a TASK with parent USER_STORY")
  public void iCreateATaskWithParentUserStory() {
    createWorkItem("TASK", "Child Task",
        ScenarioContext.get(KEY_USER_STORY_ID, String.class), TokenStore.getToken());
  }

  /** Creates a TASK under the current USER_STORY to use as an invalid parent. */
  @When("I create a TASK under the current user story")
  public void iCreateATaskUnderTheCurrentUserStory() {
    createWorkItem("TASK", "Parent Task",
        ScenarioContext.get(KEY_USER_STORY_ID, String.class), TokenStore.getToken());
    assertEquals(201, AuthSteps.lastResponse().statusCode());
    ScenarioContext.set("parentTaskId", ScenarioContext.get(KEY_WORK_ITEM_ID, String.class));
  }

  /** Attempts to create a TASK with another TASK as parent (invalid hierarchy). */
  @When("I try to create a TASK with invalid parent type")
  public void iTryToCreateATaskWithInvalidParentType() {
    createWorkItem("TASK", "Invalid Child Task",
        ScenarioContext.get("parentTaskId", String.class), TokenStore.getToken());
  }

  /** Lists work items for the active project. */
  @When("I list work items for the current project")
  @When("I list all work items")
  public void iListWorkItemsForTheCurrentProject() {
    Response response = given()
        .spec(RequestSpecFactory.boardSpec(TokenStore.getToken()))
        .queryParam("projectId", ScenarioContext.get(KEY_PROJECT_ID, String.class))
        .when()
        .get("/api/v1/work-items")
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
  }

  /** Updates the status of the most recently created work item. */
  @When("I update the work item status to {string}")
  public void iUpdateTheWorkItemStatusTo(String newStatus) {
    String workItemId = ScenarioContext.get(KEY_WORK_ITEM_ID, String.class);
    assertNotNull(workItemId, "No work-item ID in context — create a work item first");
    Response response = given()
        .spec(RequestSpecFactory.boardSpec(TokenStore.getToken()))
        .body(Map.of("status", newStatus))
        .when()
        .patch("/api/v1/work-items/" + workItemId + "/status")
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
  }

  /** Provisions two tenants each with a work item for isolation checks. */
  @Given("two users from different tenants each with a work item")
  public void twoUsersFromDifferentTenantsEachWithAWorkItem() {
    ProjectSteps projectSteps = new ProjectSteps();
    projectSteps.anAuthenticatedUserWithAProject();
    iCreateAWorkItemWithTypeAndTitle("FEATURE", "Tenant A Feature");
    assertEquals(201, AuthSteps.lastResponse().statusCode());
    ScenarioContext.set("firstUserToken", TokenStore.getToken());

    projectSteps.freshSecondTenantWithProject();
    iCreateAWorkItemWithTypeAndTitle("FEATURE", "Tenant B Feature");
    assertEquals(201, AuthSteps.lastResponse().statusCode());
    ScenarioContext.set(KEY_OTHER_WORK_ITEM_ID, ScenarioContext.get(KEY_WORK_ITEM_ID, String.class));
    ScenarioContext.set(KEY_OTHER_USER_TOKEN, TokenStore.getToken());
  }

  /** Requests a foreign tenant work item by identifier. */
  @When("the first user requests the second user's work item by id")
  public void theFirstUserRequestsTheSecondUsersWorkItemById() {
    Response response = given()
        .spec(RequestSpecFactory.boardSpec(ScenarioContext.get("firstUserToken", String.class)))
        .when()
        .get("/api/v1/work-items/" + ScenarioContext.get(KEY_OTHER_WORK_ITEM_ID, String.class))
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
  }

  /** Asserts that the most recent work-item creation returned HTTP 201. */
  @Then("the work item should be created successfully")
  public void theWorkItemShouldBeCreatedSuccessfully() {
    assertEquals(201, AuthSteps.lastResponse().statusCode());
    assertNotNull(AuthSteps.lastResponse().jsonPath().getString("id"));
  }

  /** Asserts that the returned work item carries the expected status value. */
  @Then("the work item should have status {string}")
  @And("the work item status should be {string}")
  public void theWorkItemShouldHaveStatus(String expectedStatus) {
    assertEquals(expectedStatus, AuthSteps.lastResponse().jsonPath().getString("status"));
  }

  /** Asserts that the returned work item carries the expected type value. */
  @Then("the work item type should be {string}")
  public void theWorkItemTypeShouldBe(String expectedType) {
    assertEquals(expectedType, AuthSteps.lastResponse().jsonPath().getString("type"));
  }

  /** Asserts that the returned work item references the expected parent. */
  @Then("the work item parent id should match the user story")
  public void theWorkItemParentIdShouldMatchTheUserStory() {
    assertEquals(
        ScenarioContext.get(KEY_USER_STORY_ID, String.class),
        AuthSteps.lastResponse().jsonPath().getString("parentId"));
  }

  /** Asserts that the list response contains at least one work item. */
  @Then("the response should contain at least one work item")
  @And("the work item list should not be empty")
  public void theResponseShouldContainAtLeastOneWorkItem() {
    assertEquals(200, AuthSteps.lastResponse().statusCode());
    assertTrue(AuthSteps.lastResponse().jsonPath().getList("$").size() > 0);
  }

  /** Legacy step kept for backward compatibility with older feature wording. */
  @When("I create a work item with title {string}")
  public void iCreateAWorkItemWithTitle(String title) {
    iCreateAWorkItemWithTypeAndTitle("FEATURE", title);
  }

  private void createWorkItem(String type, String title, String parentId, String token) {
    Map<String, Object> body = new HashMap<>();
    body.put("type", type);
    body.put("title", title);
    body.put("priority", 5);
    if (parentId != null) {
      body.put("parentId", parentId);
    }

    Response response = given()
        .spec(RequestSpecFactory.boardSpec(token))
        .queryParam("projectId", ScenarioContext.get(KEY_PROJECT_ID, String.class))
        .body(body)
        .when()
        .post("/api/v1/work-items")
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
    if (response.statusCode() == 201) {
      ScenarioContext.set(KEY_WORK_ITEM_ID, response.jsonPath().getString("id"));
    }
  }
}
