package com.agentboard.api.steps;

import com.agentboard.api.config.RequestSpecFactory;
import com.agentboard.api.support.ScenarioContext;
import com.agentboard.api.support.TestDataFactory;
import com.agentboard.api.support.TokenStore;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for board-service project management flows.
 */
public class ProjectSteps {

  private static final String KEY_PROJECT_ID = "projectId";
  private static final String KEY_OTHER_PROJECT_ID = "otherProjectId";
  private static final String KEY_OTHER_USER_TOKEN = "otherUserToken";

  /**
   * Authenticates as the given user by performing a fresh login and saving the resulting
   * token and tenant ID into the scenario context.
   *
   * @param email the user's email address
   */
  @Given("I am authenticated as {string}")
  public void iAmAuthenticatedAs(String email) {
    Response loginResponse = given()
        .spec(RequestSpecFactory.authSpec())
        .body(Map.of("email", email, "password", TestDataFactory.defaultPassword()))
        .when()
        .post("/auth/login")
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(loginResponse);
    saveSession(loginResponse);
  }

  /** Registers, logs in, and creates a project for the current user. */
  @Given("an authenticated user with a project")
  public void anAuthenticatedUserWithAProject() {
    provisionAuthenticatedProject();
  }

  /** Provisions two isolated tenants each with their own project. */
  @Given("two users from different tenants each with a project")
  public void twoUsersFromDifferentTenantsEachWithAProject() {
    provisionAuthenticatedProject();
    ScenarioContext.set("firstUserToken", TokenStore.getToken());
    ScenarioContext.set("firstProjectId", ScenarioContext.get(KEY_PROJECT_ID, String.class));
    freshSecondTenantWithProject();
    ScenarioContext.set(KEY_OTHER_PROJECT_ID, ScenarioContext.get(KEY_PROJECT_ID, String.class));
    ScenarioContext.set(KEY_OTHER_USER_TOKEN, TokenStore.getToken());
  }

  /** Registers a second tenant user and creates a project for isolation scenarios. */
  public void freshSecondTenantWithProject() {
    AuthSteps authSteps = new AuthSteps();
    authSteps.freshRegistrationDataIsPrepared();
    authSteps.aUserIsRegisteredWithPreparedData();
    authSteps.iLoginWithThePreparedCredentials();
    createProject(TestDataFactory.generateProjectName(), TokenStore.getToken());
    assertEquals(201, AuthSteps.lastResponse().statusCode());
  }

  /** Creates a project with a unique generated name. */
  @When("I create a project with a unique name")
  public void iCreateAProjectWithAUniqueName() {
    createProject(TestDataFactory.generateProjectName(), TokenStore.getToken());
  }

  /** Lists all projects visible to the authenticated user. */
  @When("I list projects")
  public void iListProjects() {
    Response response = given()
        .spec(RequestSpecFactory.boardSpec(TokenStore.getToken()))
        .when()
        .get("/api/v1/projects")
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
  }

  /** Lists projects without providing an authentication token. */
  @When("I list projects without authentication")
  public void iListProjectsWithoutAuthentication() {
    Response response = given()
        .spec(RequestSpecFactory.boardSpec())
        .when()
        .get("/api/v1/projects")
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
  }

  /** Requests a foreign tenant's project by identifier. */
  @When("the first user requests the second user's project by id")
  public void theFirstUserRequestsTheSecondUsersProjectById() {
    String firstToken = ScenarioContext.get("firstUserToken", String.class);
    if (firstToken == null) {
      firstToken = restoreFirstUserToken();
    }
    Response response = given()
        .spec(RequestSpecFactory.boardSpec(firstToken))
        .when()
        .get("/api/v1/projects/" + ScenarioContext.get(KEY_OTHER_PROJECT_ID, String.class))
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
  }

  /** Asserts that the project list response is a non-empty array. */
  @Then("the project list should not be empty")
  public void theProjectListShouldNotBeEmpty() {
    assertTrue(AuthSteps.lastResponse().jsonPath().getList("$").size() > 0);
  }

  private void provisionAuthenticatedProject() {
    AuthSteps authSteps = new AuthSteps();
    authSteps.freshRegistrationDataIsPrepared();
    authSteps.aUserIsRegisteredWithPreparedData();
    authSteps.iLoginWithThePreparedCredentials();
    createProject(TestDataFactory.generateProjectName(), TokenStore.getToken());
    assertEquals(201, AuthSteps.lastResponse().statusCode());
  }

  private void createProject(String name, String token) {
    Response response = given()
        .spec(RequestSpecFactory.boardSpec(token))
        .body(Map.of("name", name))
        .when()
        .post("/api/v1/projects")
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
    if (response.statusCode() == 201) {
      ScenarioContext.set(KEY_PROJECT_ID, response.jsonPath().getString("id"));
    }
  }

  private String restoreFirstUserToken() {
    String firstToken = ScenarioContext.get("firstUserToken", String.class);
    ScenarioContext.set(KEY_PROJECT_ID, ScenarioContext.get("firstProjectId", String.class));
    TokenStore.saveToken(firstToken);
    return firstToken;
  }

  private void saveSession(Response response) {
    if (response.statusCode() == 200) {
      TokenStore.saveToken(response.jsonPath().getString("token"));
      TokenStore.saveTenantId(response.jsonPath().getString("tenantId"));
    }
  }
}
