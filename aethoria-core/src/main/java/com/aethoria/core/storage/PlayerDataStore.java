package com.aethoria.core.storage;

import com.aethoria.core.model.PlayerProfile;
import java.util.Collection;
import java.util.UUID;

public interface PlayerDataStore extends AutoCloseable {
    void initialize() throws Exception;

    PlayerProfile load(UUID playerId, String defaultClass) throws Exception;

    void save(PlayerProfile profile) throws Exception;

    void saveAll(Collection<PlayerProfile> profiles) throws Exception;

    String getStorageName();

    @Override
    void close() throws Exception;
}