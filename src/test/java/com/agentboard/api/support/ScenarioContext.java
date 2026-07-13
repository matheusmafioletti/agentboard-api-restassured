package com.agentboard.api.support;

import java.util.HashMap;
import java.util.Map;

public final class ScenarioContext {

  public static final String KEY_LAST_RESPONSE = "lastResponse";

  private static final ThreadLocal<Map<String, Object>> CONTEXT =
      ThreadLocal.withInitial(HashMap::new);

  private ScenarioContext() {}

  public static void set(String key, Object value) {
    CONTEXT.get().put(key, value);
  }

  public static <T> T get(String key, Class<T> type) {
    return type.cast(CONTEXT.get().get(key));
  }

  public static boolean contains(String key) {
    return CONTEXT.get().containsKey(key);
  }

  public static void clear() {
    CONTEXT.get().clear();
  }
}
