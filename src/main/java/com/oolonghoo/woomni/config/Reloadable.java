package com.oolonghoo.woomni.config;

/**
 * 可重载接口
 * 实现此接口的类可以在配置重载时被通知
 */
public interface Reloadable {
    
    /**
     * 重载配置
     */
    void reload();
}
