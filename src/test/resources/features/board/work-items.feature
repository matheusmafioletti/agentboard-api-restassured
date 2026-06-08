@smoke @board
Feature: Work Item Management
  As an authenticated board user
  I want to create and manage work items
  So that my team can track progress on the Kanban board

  Background:
    Given I register a new user with email "board-user@test.com" and password "secret123"
    And I login with email "board-user@test.com" and password "secret123"

  @create-work-item
  Scenario: Creating a work item returns it with a default status
    When I create a work item with title "Implement login page"
    Then the work item should be created successfully
    And the work item should have status "TODO"

  @create-work-item
  Scenario: Creating a work item with a long title succeeds
    When I create a work item with title "This is a very detailed title that describes the full scope of the task to be done by the team"
    Then the work item should be created successfully

  @list-work-items
  Scenario: Listing work items returns the previously created item
    When I create a work item with title "List test item"
    Then the work item should be created successfully
    When I list all work items
    Then the response status should be 200
    And the response should contain at least one work item

  @update-work-item
  Scenario: Updating a work item status moves it to the new column
    When I create a work item with title "Item to update"
    Then the work item should be created successfully
    When I update the work item status to "IN_PROGRESS"
    Then the response status should be 200
    And the work item should have status "IN_PROGRESS"

  @update-work-item
  Scenario: Completing a work item sets it to DONE
    When I create a work item with title "Item to complete"
    Then the work item should be created successfully
    When I update the work item status to "IN_PROGRESS"
    Then the response status should be 200
    When I update the work item status to "DONE"
    Then the response status should be 200
    And the work item should have status "DONE"
