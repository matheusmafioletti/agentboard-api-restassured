package com.agentboard.api.support;

import java.util.UUID;

/**
 * Generates unique test data for API scenarios to avoid collisions across parallel runs.
 */
public final class TestDataFactory {

  private TestDataFactory() {}

  /**
   * Returns a globally unique email address.
   *
   * @return random {@code @example.com} email
   */
  public static String generateEmail() {
    return "user-" + UUID.randomUUID() + "@example.com";
  }

  /**
   * Returns a globally unique tenant name.
   *
   * @return random workspace name
   */
  public static String generateTenantName() {
    return "Tenant " + UUID.randomUUID();
  }

  /**
   * Returns the default password used across API test scenarios.
   *
   * @return shared test password meeting auth-service validation rules
   */
  public static String defaultPassword() {
    return "S3cret-password";
  }

  /**
   * Returns a display name for newly registered users.
   *
   * @return human-readable user name
   */
  public static String generateName() {
    return "Test User";
  }

  /**
   * Returns a unique project name.
   *
   * @return random project title
   */
  public static String generateProjectName() {
    return "Project " + UUID.randomUUID();
  }
}
