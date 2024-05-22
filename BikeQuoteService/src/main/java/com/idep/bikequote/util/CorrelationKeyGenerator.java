package com.idep.bikequote.util;

import java.util.UUID;

public class CorrelationKeyGenerator {
  public UUID getUniqueKey() {
    UUID uniqueKey = UUID.randomUUID();
    return uniqueKey;
  }
}
