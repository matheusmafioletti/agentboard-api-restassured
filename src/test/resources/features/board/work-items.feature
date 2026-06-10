@board @critical
Feature: Work Item Management
  As an authenticated board user
  I want to create and manage work items
  So that my team can track progress on the Kanban board

  Background:
    Given an authenticated user with a project

  @smoke
  Scenario: TC-API-WI-001 Create TASK returns 201 with status NEW
    Given a FEATURE work item exists in the current project
    And I create a USER_STORY under the current feature
    When I create a TASK with parent USER_STORY
    Then the response status should be 201
    And the work item type should be "TASK"
    And the work item status should be "NEW"

  @high
  Scenario: TC-API-WI-002 Create USER_STORY returns 201 with status READY
    Given a FEATURE work item exists in the current project
    When I create a USER_STORY under the current feature
    Then the response status should be 201
    And the work item type should be "USER_STORY"
    And the work item status should be "READY"

  @high
  Scenario: TC-API-WI-003 Create FEATURE returns 201 with status BACKLOG
    When I create a FEATURE work item with title "New Feature"
    Then the response status should be 201
    And the work item type should be "FEATURE"
    And the work item status should be "BACKLOG"

  Scenario: TC-API-WI-004 List work items returns 200
    When I create a FEATURE work item with title "List test item"
    Then the response status should be 201
    When I list work items for the current project
    Then the response status should be 200
    And the work item list should not be empty

  @smoke
  Scenario: TC-API-WI-005 Update work item status via PATCH
    Given a FEATURE work item exists in the current project
    And I create a USER_STORY under the current feature
    And I create a TASK under the current user story
    When I update the work item status to "ACTIVE"
    Then the response status should be 200
    And the work item status should be "ACTIVE"

  Scenario: TC-API-WI-006 Create TASK with USER_STORY parent
    Given a FEATURE work item exists in the current project
    And I create a USER_STORY under the current feature
    When I create a TASK with parent USER_STORY
    Then the response status should be 201
    And the work item type should be "TASK"
    And the work item parent id should match the user story

  Scenario: TC-API-WI-007 Invalid hierarchy TASK parent of TASK returns 400
    Given a FEATURE work item exists in the current project
    And I create a USER_STORY under the current feature
    And I create a TASK under the current user story
    When I try to create a TASK with invalid parent type
    Then the response status should be 400

  Scenario: TC-API-WI-008 Cross-tenant work item access returns 404
    Given two users from different tenants each with a work item
    When the first user requests the second user's work item by id
    Then the response status should be 404
