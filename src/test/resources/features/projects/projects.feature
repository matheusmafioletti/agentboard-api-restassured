@projects @critical
Feature: Project Management
  As an authenticated tenant user
  I want to manage projects
  So that I can organize work items

  @smoke
  Scenario: TC-API-PROJ-001 Authenticated user creates a project
    Given a user is registered with prepared data
    When I login with the prepared credentials
    Then the response status should be 200
    When I create a project with a unique name
    Then the response status should be 201

  @high
  Scenario: TC-API-PROJ-002 List projects returns array for tenant
    Given an authenticated user with a project
    When I list projects
    Then the response status should be 200
    And the project list should not be empty

  Scenario: TC-API-PROJ-003 Unauthenticated access returns 401
    When I list projects without authentication
    Then the response status should be 401

  Scenario: TC-API-PROJ-004 Cross-tenant project access returns 404
    Given two users from different tenants each with a project
    When the first user requests the second user's project by id
    Then the response status should be 404
