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
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions covering user registration, login, tenant management, and shared assertions.
 */
public class AuthSteps {

  private static final String KEY_EMAIL = "email";
  private static final String KEY_PASSWORD = "password";
  private static final String KEY_NAME = "name";
  private static final String KEY_TENANT_NAME = "tenantName";
  private static final String KEY_SECOND_TENANT_ID = "secondTenantId";
  private static final String KEY_SECOND_TENANT_NAME = "secondTenantName";

  /** Prepares unique registration credentials in the scenario context. */
  @Given("fresh registration data is prepared")
  public void freshRegistrationDataIsPrepared() {
    storeCredentials(
        TestDataFactory.generateEmail(),
        TestDataFactory.defaultPassword(),
        TestDataFactory.generateName(),
        TestDataFactory.generateTenantName());
  }

  /** Stores valid registration credentials in the scenario context. */
  @Given("I have valid registration data with email {string} and password {string}")
  public void iHaveValidRegistrationData(String email, String password) {
    storeCredentials(email, password, TestDataFactory.generateName(), TestDataFactory.generateTenantName());
  }

  /** Stores arbitrary registration credentials (may be invalid) in the scenario context. */
  @Given("I have registration data with email {string} and password {string}")
  public void iHaveRegistrationData(String email, String password) {
    storeCredentials(email, password, TestDataFactory.generateName(), TestDataFactory.generateTenantName());
  }

  /** Registers a user with the credentials stored in the scenario context. */
  @When("I register with the prepared data")
  public void iRegisterWithThePreparedData() {
    registerWithStoredCredentials();
  }

  /** Performs a full register request in a single step. */
  @Given("I register a new user with email {string} and password {string}")
  public void iRegisterANewUser(String email, String password) {
    storeCredentials(email, password, TestDataFactory.generateName(), TestDataFactory.generateTenantName());
    registerWithStoredCredentials();
  }

  /** Registers a user and stores the session when successful. */
  @Given("a user is registered with prepared data")
  public void aUserIsRegisteredWithPreparedData() {
    freshRegistrationDataIsPrepared();
    registerWithStoredCredentials();
    assertEquals(201, lastResponse().statusCode());
    saveSessionFromResponse(lastResponse());
  }

  /** Registers the same email again to trigger a conflict. */
  @When("I register again with the same email")
  public void iRegisterAgainWithTheSameEmail() {
    registerWithStoredCredentials();
  }

  /** Sends a POST to the auth-service using credentials stored in the context. */
  @When("I send a POST request to {string}")
  public void iSendAPostRequestTo(String path) {
    Map<String, String> body = path.contains("register") ? registerBody() : loginBody();
    Response response = given()
        .spec(RequestSpecFactory.authSpec())
        .body(body)
        .when()
        .post(path)
        .then()
        .extract()
        .response();
    storeResponse(response);
  }

  /** Performs a login with credentials from the scenario context. */
  @When("I login with the prepared credentials")
  public void iLoginWithThePreparedCredentials() {
    loginWithStoredCredentials();
  }

  /** Performs a full login request in a single step. */
  @When("I login with email {string} and password {string}")
  public void iLoginWith(String email, String password) {
    Response response = given()
        .spec(RequestSpecFactory.authSpec())
        .body(Map.of("email", email, "password", password))
        .when()
        .post("/auth/login")
        .then()
        .extract()
        .response();
    storeResponse(response);
    saveSessionFromResponse(response);
  }

  /** Creates a second tenant for the currently authenticated user. */
  @Given("the user creates a second tenant")
  @When("the user creates a second tenant")
  public void theUserCreatesASecondTenant() {
    String secondTenantName = TestDataFactory.generateTenantName();
    ScenarioContext.set(KEY_SECOND_TENANT_NAME, secondTenantName);
    Response response = given()
        .spec(RequestSpecFactory.authSpec(TokenStore.getToken()))
        .body(Map.of("tenantName", secondTenantName))
        .when()
        .post("/auth/tenants")
        .then()
        .extract()
        .response();
    storeResponse(response);
    if (response.statusCode() == 201) {
      String secondTenantId = response.jsonPath().getString("session.tenantId");
      ScenarioContext.set(KEY_SECOND_TENANT_ID, secondTenantId);
      String scopedToken = response.jsonPath().getString("session.token");
      if (scopedToken != null) {
        TokenStore.saveToken(scopedToken);
        TokenStore.saveTenantId(secondTenantId);
      }
    }
  }

  /** Selects the second tenant using email and password credentials. */
  @When("I select the second tenant with prepared credentials")
  public void iSelectTheSecondTenantWithPreparedCredentials() {
    String tenantId = ScenarioContext.get(KEY_SECOND_TENANT_ID, String.class);
    Response response = given()
        .spec(RequestSpecFactory.authSpec())
        .body(Map.of(
            "email", ScenarioContext.get(KEY_EMAIL, String.class),
            "password", ScenarioContext.get(KEY_PASSWORD, String.class),
            "tenantId", tenantId))
        .when()
        .post("/auth/select-tenant")
        .then()
        .extract()
        .response();
    storeResponse(response);
    saveSessionFromResponse(response);
  }

  /** Creates a tenant with a unique generated name. */
  @When("I create a tenant with a unique name")
  public void iCreateATenantWithAUniqueName() {
    String tenantName = TestDataFactory.generateTenantName();
    ScenarioContext.set(KEY_SECOND_TENANT_NAME, tenantName);
    Response response = given()
        .spec(RequestSpecFactory.authSpec(TokenStore.getToken()))
        .body(Map.of("tenantName", tenantName))
        .when()
        .post("/auth/tenants")
        .then()
        .extract()
        .response();
    storeResponse(response);
  }

  /** Attempts to create a tenant using the same name as the registration tenant. */
  @When("I create a tenant with the same name as the registered tenant")
  public void iCreateATenantWithTheSameNameAsTheRegisteredTenant() {
    Response response = given()
        .spec(RequestSpecFactory.authSpec(TokenStore.getToken()))
        .body(Map.of("tenantName", ScenarioContext.get(KEY_TENANT_NAME, String.class)))
        .when()
        .post("/auth/tenants")
        .then()
        .extract()
        .response();
    storeResponse(response);
  }

  /** Asserts that the stored response has the expected HTTP status code. */
  @Then("the response status should be {int}")
  public void theResponseStatusShouldBe(int expectedStatus) {
    Response response = lastResponse();
    assertEquals(
        expectedStatus,
        response.statusCode(),
        () -> "Expected status " + expectedStatus + " but got " + response.statusCode()
            + ". Body: " + response.getBody().asString());
  }

  /** Asserts that a non-blank JWT token was captured in the scenario context. */
  @Then("I receive a valid JWT token")
  @And("the response should have a valid token")
  public void iReceiveAValidJwtToken() {
    String token = TokenStore.getToken();
    assertThat("Expected a non-blank JWT token", token, not(emptyOrNullString()));
  }

  /** Asserts that the response body contains the given substring. */
  @Then("the response body should contain {string}")
  @And("the response body should contain {string}")
  public void theResponseBodyShouldContain(String substring) {
    String body = lastResponse().getBody().asString();
    assertTrue(
        body.contains(substring),
        () -> "Expected response body to contain \"" + substring + "\" but got: " + body);
  }

  /** Asserts that the response role field matches the expected value. */
  @Then("the response role should be {string}")
  @And("the response role should be {string}")
  public void theResponseRoleShouldBe(String expectedRole) {
    String role = lastResponse().jsonPath().getString("role");
    if (role == null) {
      role = lastResponse().jsonPath().getString("session.role");
    }
    assertEquals(expectedRole, role);
  }

  /** Asserts that the created tenant role in a nested session matches the expected value. */
  @Then("the created tenant role should be {string}")
  public void theCreatedTenantRoleShouldBe(String expectedRole) {
    assertEquals(expectedRole, lastResponse().jsonPath().getString("session.role"));
  }

  /** Asserts that the response contains a non-null tenant identifier. */
  @Then("the response should have a tenant id")
  public void theResponseShouldHaveATenantId() {
    String tenantId = lastResponse().jsonPath().getString("tenantId");
    if (tenantId == null) {
      tenantId = lastResponse().jsonPath().getString("session.tenantId");
    }
    assertThat("Expected tenantId in response", tenantId, not(emptyOrNullString()));
  }

  /** Asserts that login returned a tenant-selection payload. */
  @Then("the response should require tenant selection")
  public void theResponseShouldRequireTenantSelection() {
    assertEquals(true, lastResponse().jsonPath().getBoolean("requiresTenantSelection"));
    assertThat(lastResponse().jsonPath().getString("token"), emptyOrNullString());
  }

  /** Asserts the number of memberships returned during multi-tenant login. */
  @Then("the response should have {int} memberships")
  public void theResponseShouldHaveMemberships(int expectedCount) {
    assertEquals(expectedCount, lastResponse().jsonPath().getList("memberships").size());
  }

  /** Selects a tenant using the stored token and saves the resulting scoped token. */
  @When("I select tenant {string}")
  public void iSelectTenant(String tenantId) {
    Response response = given()
        .spec(RequestSpecFactory.authSpec())
        .header("Authorization", "Bearer " + TokenStore.getToken())
        .body(Map.of("tenantId", tenantId))
        .when()
        .post("/auth/select-tenant")
        .then()
        .extract()
        .response();
    storeResponse(response);
    saveSessionFromResponse(response);
  }

  /** Registers a separate existing user for invite acceptance scenarios. */
  @Given("an existing user is registered separately")
  public void anExistingUserIsRegisteredSeparately() {
    String email = TestDataFactory.generateEmail();
    String password = TestDataFactory.defaultPassword();
    Response response = given()
        .spec(RequestSpecFactory.authSpec())
        .body(Map.of(
            "name", TestDataFactory.generateName(),
            "email", email,
            "password", password,
            "tenantName", TestDataFactory.generateTenantName()))
        .when()
        .post("/auth/register")
        .then()
        .extract()
        .response();
    storeResponse(response);
    assertEquals(201, response.statusCode());
    ScenarioContext.set("existingUserEmail", email);
    ScenarioContext.set("existingUserPassword", password);
  }

  private void registerWithStoredCredentials() {
    Response response = given()
        .spec(RequestSpecFactory.authSpec())
        .body(Map.of(
            "name", ScenarioContext.get(KEY_NAME, String.class),
            "email", ScenarioContext.get(KEY_EMAIL, String.class),
            "password", ScenarioContext.get(KEY_PASSWORD, String.class),
            "tenantName", ScenarioContext.get(KEY_TENANT_NAME, String.class)))
        .when()
        .post("/auth/register")
        .then()
        .extract()
        .response();
    storeResponse(response);
    saveSessionFromResponse(response);
  }

  private void loginWithStoredCredentials() {
    Response response = given()
        .spec(RequestSpecFactory.authSpec())
        .body(loginBody())
        .when()
        .post("/auth/login")
        .then()
        .extract()
        .response();
    storeResponse(response);
    saveSessionFromResponse(response);
  }

  private Map<String, String> loginBody() {
    return Map.of(
        "email", ScenarioContext.get(KEY_EMAIL, String.class) != null
            ? ScenarioContext.get(KEY_EMAIL, String.class) : "",
        "password", ScenarioContext.get(KEY_PASSWORD, String.class) != null
            ? ScenarioContext.get(KEY_PASSWORD, String.class) : "");
  }

  private Map<String, String> registerBody() {
    return Map.of(
        "name", ScenarioContext.get(KEY_NAME, String.class) != null
            ? ScenarioContext.get(KEY_NAME, String.class) : "",
        "email", ScenarioContext.get(KEY_EMAIL, String.class) != null
            ? ScenarioContext.get(KEY_EMAIL, String.class) : "",
        "password", ScenarioContext.get(KEY_PASSWORD, String.class) != null
            ? ScenarioContext.get(KEY_PASSWORD, String.class) : "",
        "tenantName", ScenarioContext.get(KEY_TENANT_NAME, String.class) != null
            ? ScenarioContext.get(KEY_TENANT_NAME, String.class) : "");
  }

  private void storeCredentials(String email, String password, String name, String tenantName) {
    ScenarioContext.set(KEY_EMAIL, email);
    ScenarioContext.set(KEY_PASSWORD, password);
    ScenarioContext.set(KEY_NAME, name);
    ScenarioContext.set(KEY_TENANT_NAME, tenantName);
  }

  private void saveSessionFromResponse(Response response) {
    if (response.statusCode() != 200 && response.statusCode() != 201) {
      return;
    }
    String token = response.jsonPath().getString("token");
    if (token == null) {
      token = response.jsonPath().getString("session.token");
    }
    if (token != null) {
      TokenStore.saveToken(token);
    }
    String tenantId = response.jsonPath().getString("tenantId");
    if (tenantId == null) {
      tenantId = response.jsonPath().getString("session.tenantId");
    }
    if (tenantId != null) {
      TokenStore.saveTenantId(tenantId);
    }
    String userId = response.jsonPath().getString("userId");
    if (userId != null) {
      ScenarioContext.set("userId", userId);
    }
  }

  static void storeResponse(Response response) {
    ScenarioContext.set(ScenarioContext.KEY_LAST_RESPONSE, response);
    ScenarioContext.set("lastResponseBody", response.getBody().asString());
  }

  static Response lastResponse() {
    Response response = ScenarioContext.get(ScenarioContext.KEY_LAST_RESPONSE, Response.class);
    assertNotNull(response, "No HTTP response stored in scenario context");
    return response;
  }
}
