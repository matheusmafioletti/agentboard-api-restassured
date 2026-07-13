package com.agentboard.api.support;

import java.util.UUID;

public final class TestDataFactory {

  private TestDataFactory() {}

  public static String generateEmail() {
    return "user-" + UUID.randomUUID() + "@example.com";
  }

  public static String generateTenantName() {
    return "Tenant " + UUID.randomUUID();
  }

  public static String defaultPassword() {
    return "S3cret-password";
  }

  public static String generateName() {
    return "Test User";
  }

  public static String generateProjectName() {
    return "Project " + UUID.randomUUID();
  }
}
