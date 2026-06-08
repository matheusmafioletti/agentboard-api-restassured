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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions covering user registration, login, and tenant selection flows.
 *
 * <p>All state is persisted to {@link ScenarioContext} so subsequent steps and other
 * step-definition classes can access the JWT token and tenant ID without coupling.
 */
public class AuthSteps {

  private static final String KEY_RESPONSE = "authResponse";
  private static final String KEY_EMAIL = "regEmail";
  private static final String KEY_PASSWORD = "regPassword";

  /** Stores valid registration credentials in the scenario context. */
  @Given("I have valid registration data with email {string} and password {string}")
  public void iHaveValidRegistrationData(String email, String password) {
    ScenarioContext.set(KEY_EMAIL, email);
    ScenarioContext.set(KEY_PASSWORD, password);
  }

  /** Stores arbitrary registration credentials (may be invalid) in the scenario context. */
  @Given("I have registration data with email {string} and password {string}")
  public void iHaveRegistrationData(String email, String password) {
    ScenarioContext.set(KEY_EMAIL, email);
    ScenarioContext.set(KEY_PASSWORD, password);
  }

  /** Sends a POST to the auth-service using credentials stored in the context. */
  @When("I send a POST request to {string}")
  public void iSendAPostRequestTo(String path) {
    String email = ScenarioContext.get(KEY_EMAIL, String.class);
    String password = ScenarioContext.get(KEY_PASSWORD, String.class);

    Response response = given()
        .spec(RequestSpecFactory.authSpec())
        .body(Map.of("email", email != null ? email : "", "password", password != null ? password : ""))
        .when()
        .post(path)
        .then()
        .extract()
        .response();

    ScenarioContext.set(KEY_RESPONSE, response);
    ScenarioContext.set("lastResponseBody", response.getBody().asString());
  }

  /** Performs a full register request in a single step. */
  @Given("I register a new user with email {string} and password {string}")
  public void iRegisterANewUser(String email, String password) {
    Response response = given()
        .spec(RequestSpecFactory.authSpec())
        .body(Map.of("email", email, "password", password))
        .when()
        .post("/auth/register")
        .then()
        .extract()
        .response();

    ScenarioContext.set(KEY_RESPONSE, response);
    ScenarioContext.set("lastResponseBody", response.getBody().asString());

    if (response.statusCode() == 201 || response.statusCode() == 200) {
      String token = response.jsonPath().getString("accessToken");
      if (token != null) {
        TokenStore.saveToken(token);
      }
    }
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

    ScenarioContext.set(KEY_RESPONSE, response);
    ScenarioContext.set("lastResponseBody", response.getBody().asString());

    if (response.statusCode() == 200) {
      String token = response.jsonPath().getString("accessToken");
      if (token != null) {
        TokenStore.saveToken(token);
      }
      String tenantId = response.jsonPath().getString("tenantId");
      if (tenantId != null) {
        TokenStore.saveTenantId(tenantId);
      }
    }
  }

  /** Asserts that the stored response has the expected HTTP status code. */
  @Then("the response status should be {int}")
  public void theResponseStatusShouldBe(int expectedStatus) {
    Response response = ScenarioContext.get(KEY_RESPONSE, Response.class);
    assertEquals(
        expectedStatus,
        response.statusCode(),
        () -> "Expected status " + expectedStatus + " but got " + response.statusCode()
            + ". Body: " + response.getBody().asString()
    );
  }

  /** Asserts that a non-blank JWT token was captured in the scenario context. */
  @Then("I receive a valid JWT token")
  public void iReceiveAValidJwtToken() {
    String token = TokenStore.getToken();
    assertThat("Expected a non-blank JWT token", token, not(emptyOrNullString()));
  }

  /** Asserts that the response body contains the given substring. */
  @Then("the response body should contain {string}")
  @And("the response body should contain {string}")
  public void theResponseBodyShouldContain(String substring) {
    Response response = ScenarioContext.get(KEY_RESPONSE, Response.class);
    String body = response.getBody().asString();
    assertTrue(
        body.contains(substring),
        () -> "Expected response body to contain \"" + substring + "\" but got: " + body
    );
  }

  /** Selects a tenant using the stored token and saves the resulting scoped token. */
  @When("I select tenant {string}")
  public void iSelectTenant(String tenantId) {
    String token = TokenStore.getToken();

    Response response = given()
        .spec(RequestSpecFactory.authSpec())
        .header("Authorization", "Bearer " + token)
        .body(Map.of("tenantId", tenantId))
        .when()
        .post("/auth/select-tenant")
        .then()
        .extract()
        .response();

    ScenarioContext.set(KEY_RESPONSE, response);
    ScenarioContext.set("lastResponseBody", response.getBody().asString());

    if (response.statusCode() == 200) {
      String scopedToken = response.jsonPath().getString("accessToken");
      if (scopedToken != null) {
        TokenStore.saveToken(scopedToken);
        TokenStore.saveTenantId(tenantId);
      }
    }
  }
}
