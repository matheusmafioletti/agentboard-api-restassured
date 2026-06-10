@invites @critical
Feature: Tenant Invites and Membership
  As a tenant admin
  I want to invite and manage members
  So that my team can collaborate on AgentBoard

  @smoke
  Scenario: TC-API-INV-001 Admin creates invite with token
    Given an admin user is registered and authenticated
    When the admin creates an invite for a new email
    Then the response status should be 201
    And the invite should have a token

  @high
  Scenario: TC-API-INV-002 USER cannot create invite
    Given an admin user is registered and authenticated
    And a member user has accepted an invite as USER
    When the member creates an invite for a new email
    Then the response status should be 403

  Scenario: TC-API-INV-003 Admin cancels pending invite
    Given an admin user is registered and authenticated
    And the admin creates an invite for a new email
    When the admin cancels the invite
    Then the response status should be 204

  @smoke
  Scenario: TC-API-INV-004 New user accepts invite and becomes USER
    Given an admin user is registered and authenticated
    And the admin creates an invite for a new email
    When a new user accepts the invite
    Then the response status should be 200
    And the accepted membership role should be "USER"

  @high
  Scenario: TC-API-INV-005 Existing user accepts invite without new tenant
    Given an admin user is registered and authenticated
    And an existing user is registered separately
    And the admin creates an invite for the existing user email
    When the existing user accepts the invite
    Then the response status should be 200
    And the accepted membership role should be "USER"

  Scenario: TC-API-INV-006 Invalid invite token returns 410
    When I accept an invite with an invalid token
    Then the response status should be 410

  Scenario: TC-API-INV-007 Admin revokes USER member
    Given an admin user is registered and authenticated
    And a member user has accepted an invite as USER
    When the admin revokes the member
    Then the response status should be 204

  Scenario: TC-API-INV-008 Admin cannot revoke the only admin
    Given an admin user is registered and authenticated
    When the admin tries to revoke themselves as the only admin
    Then the response status should be 409
