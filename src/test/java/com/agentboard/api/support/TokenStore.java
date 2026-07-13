package com.agentboard.api.support;

public final class TokenStore {

  public static final String KEY_ACCESS_TOKEN = "accessToken";
  public static final String KEY_TENANT_ID = "tenantId";

  private TokenStore() {}

  public static void saveToken(String token) {
    ScenarioContext.set(KEY_ACCESS_TOKEN, token);
  }

  public static String getToken() {
    return ScenarioContext.get(KEY_ACCESS_TOKEN, String.class);
  }

  public static void saveTenantId(String tenantId) {
    ScenarioContext.set(KEY_TENANT_ID, tenantId);
  }

  public static String getTenantId() {
    return ScenarioContext.get(KEY_TENANT_ID, String.class);
  }
}
