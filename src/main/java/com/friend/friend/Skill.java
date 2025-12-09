package com.friend.friend;

/**
 * Skill interface for plugin-based commands and extensions.
 * Plugins should implement this interface and register their commands
 * with the provided CommandDispatcher in the `register` method.
 */
public interface Skill {
    /**
     * Called when the skill should register commands and hooks into the dispatcher.
     */
    void register(CommandDispatcher dispatcher);

    default void start() {}
    default void stop() {}

    default String getName() { return this.getClass().getSimpleName(); }
}
