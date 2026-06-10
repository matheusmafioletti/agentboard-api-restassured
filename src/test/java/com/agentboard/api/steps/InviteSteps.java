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
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for tenant invite and membership management flows.
 */
public class InviteSteps {

  private static final String KEY_INVITE_ID = "inviteId";
  private static final String KEY_INVITE_TOKEN = "inviteToken";
  private static final String KEY_INVITEE_EMAIL = "inviteeEmail";
  private static final String KEY_MEMBER_USER_ID = "memberUserId";
  private static final String KEY_MEMBER_TOKEN = "memberToken";

  /** Registers an admin user and stores the authenticated session. */
  @Given("an admin user is registered and authenticated")
  public void anAdminUserIsRegisteredAndAuthenticated() {
    AuthSteps authSteps = new AuthSteps();
    authSteps.freshRegistrationDataIsPrepared();
    authSteps.aUserIsRegisteredWithPreparedData();
  }

  /** Creates an invite for a freshly generated email address. */
  @When("the admin creates an invite for a new email")
  @Given("the admin creates an invite for a new email")
  public void theAdminCreatesAnInviteForANewEmail() {
    String inviteeEmail = TestDataFactory.generateEmail();
    ScenarioContext.set(KEY_INVITEE_EMAIL, inviteeEmail);
    createInvite(inviteeEmail, TokenStore.getToken(), TokenStore.getTenantId());
  }

  /** Creates an invite targeting the separately registered existing user. */
  @Given("the admin creates an invite for the existing user email")
  public void theAdminCreatesAnInviteForTheExistingUserEmail() {
    String inviteeEmail = ScenarioContext.get("existingUserEmail", String.class);
    createInvite(inviteeEmail, TokenStore.getToken(), TokenStore.getTenantId());
  }

  /** A USER-role member attempts to create an invite and expects forbidden. */
  @When("the member creates an invite for a new email")
  public void theMemberCreatesAnInviteForANewEmail() {
    String inviteeEmail = TestDataFactory.generateEmail();
    createInvite(inviteeEmail, ScenarioContext.get(KEY_MEMBER_TOKEN, String.class), TokenStore.getTenantId());
  }

  /** Cancels the pending invite created earlier in the scenario. */
  @When("the admin cancels the invite")
  public void theAdminCancelsTheInvite() {
    Response response = given()
        .spec(RequestSpecFactory.authSpec(TokenStore.getToken()))
        .when()
        .delete("/auth/tenants/" + TokenStore.getTenantId() + "/invites/"
            + ScenarioContext.get(KEY_INVITE_ID, String.class))
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
  }

  /** Accepts the pending invite as a brand-new user. */
  @When("a new user accepts the invite")
  public void aNewUserAcceptsTheInvite() {
    acceptInvite(Map.of(
        "name", TestDataFactory.generateName(),
        "password", TestDataFactory.defaultPassword()));
  }

  /** Accepts the pending invite as the pre-registered existing user. */
  @When("the existing user accepts the invite")
  public void theExistingUserAcceptsTheInvite() {
    acceptInvite(Map.of(
        "email", ScenarioContext.get("existingUserEmail", String.class),
        "password", ScenarioContext.get("existingUserPassword", String.class)));
  }

  /** Attempts to accept an invite using a random invalid token. */
  @When("I accept an invite with an invalid token")
  public void iAcceptAnInviteWithAnInvalidToken() {
    Response response = given()
        .spec(RequestSpecFactory.authSpec())
        .body(Map.of(
            "name", TestDataFactory.generateName(),
            "password", TestDataFactory.defaultPassword()))
        .when()
        .post("/auth/invites/" + UUID.randomUUID() + "/accept")
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
  }

  /** Revokes a USER member from the tenant. */
  @When("the admin revokes the member")
  public void theAdminRevokesTheMember() {
    Response response = given()
        .spec(RequestSpecFactory.authSpec(TokenStore.getToken()))
        .when()
        .delete("/auth/tenants/" + TokenStore.getTenantId() + "/members/"
            + ScenarioContext.get(KEY_MEMBER_USER_ID, String.class))
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
  }

  /** Attempts to revoke the only admin member of the tenant. */
  @When("the admin tries to revoke themselves as the only admin")
  public void theAdminTriesToRevokeThemselvesAsTheOnlyAdmin() {
    Response response = given()
        .spec(RequestSpecFactory.authSpec(TokenStore.getToken()))
        .when()
        .delete("/auth/tenants/" + TokenStore.getTenantId() + "/members/"
            + ScenarioContext.get("userId", String.class))
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
  }

  /** Invites and accepts a new user, storing their USER session for later steps. */
  @Given("a member user has accepted an invite as USER")
  public void aMemberUserHasAcceptedAnInviteAsUser() {
    theAdminCreatesAnInviteForANewEmail();
    assertEquals(201, AuthSteps.lastResponse().statusCode());
    aNewUserAcceptsTheInvite();
    assertEquals(200, AuthSteps.lastResponse().statusCode());
    ScenarioContext.set(KEY_MEMBER_USER_ID, AuthSteps.lastResponse().jsonPath().getString("userId"));
    ScenarioContext.set(KEY_MEMBER_TOKEN, AuthSteps.lastResponse().jsonPath().getString("token"));
  }

  /** Asserts that the invite response contains a parseable token. */
  @Then("the invite should have a token")
  public void theInviteShouldHaveAToken() {
    String inviteToken = ScenarioContext.get(KEY_INVITE_TOKEN, String.class);
    assertThat("Expected invite token to be stored", inviteToken, not(emptyOrNullString()));
  }

  /** Asserts the accepted invite session carries the expected role. */
  @Then("the accepted membership role should be {string}")
  @And("the accepted membership role should be {string}")
  public void theAcceptedMembershipRoleShouldBe(String expectedRole) {
    assertEquals(expectedRole, AuthSteps.lastResponse().jsonPath().getString("role"));
  }

  private void createInvite(String inviteeEmail, String token, String tenantId) {
    Response response = given()
        .spec(RequestSpecFactory.authSpec(token))
        .body(Map.of("email", inviteeEmail))
        .when()
        .post("/auth/tenants/" + tenantId + "/invites")
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
    if (response.statusCode() == 201) {
      ScenarioContext.set(KEY_INVITE_ID, response.jsonPath().getString("id"));
      String inviteUrl = response.jsonPath().getString("inviteUrl");
      String inviteToken = inviteUrl.substring(inviteUrl.lastIndexOf("/invite/") + "/invite/".length());
      ScenarioContext.set(KEY_INVITE_TOKEN, inviteToken);
    }
  }

  private void acceptInvite(Map<String, String> body) {
    Response response = given()
        .spec(RequestSpecFactory.authSpec())
        .body(body)
        .when()
        .post("/auth/invites/" + ScenarioContext.get(KEY_INVITE_TOKEN, String.class) + "/accept")
        .then()
        .extract()
        .response();
    AuthSteps.storeResponse(response);
  }
}
