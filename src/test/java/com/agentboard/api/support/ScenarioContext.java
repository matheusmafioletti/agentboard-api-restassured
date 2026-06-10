package com.agentboard.api.support;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe key/value store shared across Cucumber step definitions within a single scenario.
 *
 * <p>Each thread gets its own isolated {@link Map}, enabling parallel scenario execution without
 * cross-contamination. Must be cleared in a {@code @Before} hook before each scenario.
 */
public final class ScenarioContext {

  /** Key for the most recent HTTP response in the current scenario. */
  public static final String KEY_LAST_RESPONSE = "lastResponse";

  private static final ThreadLocal<Map<String, Object>> CONTEXT =
      ThreadLocal.withInitial(HashMap::new);

  private ScenarioContext() {}

  /**
   * Stores a value under the given key.
   *
   * @param key   context key
   * @param value value to store
   */
  public static void set(String key, Object value) {
    CONTEXT.get().put(key, value);
  }

  /**
   * Retrieves and casts a previously stored value.
   *
   * @param key  context key
   * @param type expected type of the stored value
   * @param <T>  return type
   * @return the stored value, or {@code null} if the key is absent
   */
  public static <T> T get(String key, Class<T> type) {
    return type.cast(CONTEXT.get().get(key));
  }

  /**
   * Returns {@code true} if a non-null value is stored under the given key.
   *
   * @param key context key
   * @return whether the key is present
   */
  public static boolean contains(String key) {
    return CONTEXT.get().containsKey(key);
  }

  /** Removes all entries from the current thread's context. */
  public static void clear() {
    CONTEXT.get().clear();
  }
}
