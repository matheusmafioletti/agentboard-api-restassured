package com.agentboard.api.support;

/**
 * Convenience facade over {@link ScenarioContext} for JWT-related values.
 *
 * <p>Centralises key names so all steps reference the same constants, avoiding typo-driven bugs.
 */
public final class TokenStore {

  /** {@link ScenarioContext} key for the raw JWT access token. */
  public static final String KEY_ACCESS_TOKEN = "accessToken";

  /** {@link ScenarioContext} key for the selected tenant ID. */
  public static final String KEY_TENANT_ID = "tenantId";

  private TokenStore() {}

  /**
   * Saves the JWT access token in the current scenario context.
   *
   * @param token raw JWT string (without {@code Bearer} prefix)
   */
  public static void saveToken(String token) {
    ScenarioContext.set(KEY_ACCESS_TOKEN, token);
  }

  /**
   * Returns the JWT access token stored in the current scenario context.
   *
   * @return stored token, or {@code null} if not yet set
   */
  public static String getToken() {
    return ScenarioContext.get(KEY_ACCESS_TOKEN, String.class);
  }

  /**
   * Saves the active tenant ID in the current scenario context.
   *
   * @param tenantId tenant identifier returned by the auth-service
   */
  public static void saveTenantId(String tenantId) {
    ScenarioContext.set(KEY_TENANT_ID, tenantId);
  }

  /**
   * Returns the active tenant ID stored in the current scenario context.
   *
   * @return tenant ID, or {@code null} if not yet set
   */
  public static String getTenantId() {
    return ScenarioContext.get(KEY_TENANT_ID, String.class);
  }
}
