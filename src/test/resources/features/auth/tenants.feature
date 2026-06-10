@auth @high
Feature: Tenant Management
  As an authenticated user
  I want to create additional tenants
  So that I can manage multiple workspaces

  Scenario: TC-API-AUTH-007 Creating a second tenant returns ADMIN role
    Given a user is registered with prepared data
    When I create a tenant with a unique name
    Then the response status should be 201
    And the created tenant role should be "ADMIN"

  Scenario: TC-API-AUTH-008 Duplicate tenant name returns 409
    Given a user is registered with prepared data
    When I create a tenant with the same name as the registered tenant
    Then the response status should be 409
