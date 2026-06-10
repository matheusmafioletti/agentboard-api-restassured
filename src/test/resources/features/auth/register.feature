@auth @critical
Feature: User Registration
  As a new user
  I want to register an account
  So that I can access AgentBoard

  @smoke
  Scenario: TC-API-AUTH-001 Valid registration returns token, ADMIN role and tenantId
    Given fresh registration data is prepared
    When I register with the prepared data
    Then the response status should be 201
    And the response should have a valid token
    And the response role should be "ADMIN"
    And the response should have a tenant id

  @high
  Scenario: TC-API-AUTH-002 Duplicate email registration returns 409
    Given a user is registered with prepared data
    When I register again with the same email
    Then the response status should be 409

  Scenario Outline: Registration fails with invalid data
    Given I have registration data with email "<email>" and password "<password>"
    When I send a POST request to "/auth/register"
    Then the response status should be 400

    Examples:
      | email          | password |
      | invalid-email  | pass123  |
      |                | pass123  |
      | valid@test.com |          |
