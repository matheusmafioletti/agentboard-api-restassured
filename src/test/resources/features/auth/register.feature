@smoke @auth
Feature: User Registration
  As a new user
  I want to register an account
  So that I can access AgentBoard

  Scenario: Successful registration creates a new tenant
    Given I have valid registration data with email "alice@test.com" and password "secret123"
    When I send a POST request to "/auth/register"
    Then the response status should be 201
    And I receive a valid JWT token
    And the response body should contain "ADMIN"

  Scenario: Registration with a duplicate email fails
    Given I have valid registration data with email "duplicate@test.com" and password "secret123"
    When I send a POST request to "/auth/register"
    Then the response status should be 201
    Given I have valid registration data with email "duplicate@test.com" and password "secret123"
    When I send a POST request to "/auth/register"
    Then the response status should be 409

  Scenario Outline: Registration fails with invalid data
    Given I have registration data with email "<email>" and password "<password>"
    When I send a POST request to "/auth/register"
    Then the response status should be 400

    Examples:
      | email         | password |
      | invalid-email | pass123  |
      |               | pass123  |
      | valid@test.com|          |
