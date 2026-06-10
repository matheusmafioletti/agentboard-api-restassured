@auth @critical
Feature: User Login and Tenant Selection
  As a registered user
  I want to authenticate and select a tenant
  So that I can obtain a scoped JWT for AgentBoard

  @smoke
  Scenario: TC-API-AUTH-003 Single-tenant login returns token directly
    Given a user is registered with prepared data
    When I login with the prepared credentials
    Then the response status should be 200
    And the response should have a valid token
    And the response role should be "ADMIN"

  @high
  Scenario: TC-API-AUTH-004 Multi-tenant login returns tenant selection payload
    Given a user is registered with prepared data
    And the user creates a second tenant
    When I login with the prepared credentials
    Then the response status should be 200
    And the response should require tenant selection
    And the response should have 2 memberships

  @high
  Scenario: TC-API-AUTH-005 Select-tenant returns token for chosen tenant
    Given a user is registered with prepared data
    And the user creates a second tenant
    When I select the second tenant with prepared credentials
    Then the response status should be 200
    And the response should have a valid token
    And the response should have a tenant id

  @smoke
  Scenario: TC-API-AUTH-006 Invalid credentials return 401
    Given a user is registered with prepared data
    When I login with email "nobody@example.com" and password "wrong-password"
    Then the response status should be 401
