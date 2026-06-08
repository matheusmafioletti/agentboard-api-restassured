@smoke @auth
Feature: User Login
  As a registered user
  I want to log in to my account
  So that I can obtain a JWT token to access AgentBoard

  Background:
    Given I register a new user with email "logintest@test.com" and password "secret123"

  Scenario: Successful login returns a valid JWT token
    When I login with email "logintest@test.com" and password "secret123"
    Then the response status should be 200
    And I receive a valid JWT token

  Scenario: Login with wrong password returns 401
    When I login with email "logintest@test.com" and password "wrongpassword"
    Then the response status should be 401

  Scenario: Login with unknown email returns 401
    When I login with email "nobody@test.com" and password "secret123"
    Then the response status should be 401

  @tenant-selection
  Scenario: Authenticated user can select a tenant and receive a scoped token
    When I login with email "logintest@test.com" and password "secret123"
    Then the response status should be 200
    And I receive a valid JWT token
    When I select tenant "default"
    Then the response status should be 200
    And I receive a valid JWT token
