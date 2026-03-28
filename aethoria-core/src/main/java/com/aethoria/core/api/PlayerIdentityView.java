package com.aethoria.core.api;

import java.util.UUID;

public record PlayerIdentityView(
    UUID uniqueId,
    String name,
    boolean online
) {
}
