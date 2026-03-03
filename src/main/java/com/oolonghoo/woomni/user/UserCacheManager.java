package com.oolonghoo.woomni.user;

import com.oolonghoo.woomni.WooOmni;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class UserCacheManager<T extends UserData> {
    
    protected final WooOmni plugin;
    protected final LoadingCache<UUID, T> cache;
    
    protected UserCacheManager(WooOmni plugin) {
        this.plugin = plugin;
        this.cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .softValues()
            .removalListener(this::onRemoval)
            .build(new CacheLoader<UUID, T>() {
                @Override
                public T load(UUID uuid) {
                    return loadFromStorage(uuid);
                }
            });
    }
    
    private void onRemoval(RemovalNotification<UUID, T> notification) {
        if (notification.wasEvicted()) {
            T data = notification.getValue();
            if (data != null && data.isDirty()) {
                saveData(data);
            }
        }
    }
    
    public T get(UUID uuid) {
        return cache.getUnchecked(uuid);
    }
    
    public T getIfPresent(UUID uuid) {
        return cache.getIfPresent(uuid);
    }
    
    public void invalidate(UUID uuid) {
        T data = cache.getIfPresent(uuid);
        if (data != null && data.isDirty()) {
            saveData(data);
        }
        cache.invalidate(uuid);
    }
    
    public void saveAll() {
        cache.asMap().values().stream()
            .filter(UserData::isDirty)
            .forEach(this::saveData);
    }
    
    public void shutdown() {
        saveAll();
        cache.invalidateAll();
    }
    
    protected abstract T loadFromStorage(UUID uuid);
    
    protected abstract void saveData(T data);
}
