package com.friend.friend;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PluginLoader: loads JARs from a `plugins/` folder and discovers implementations
 * of the `Skill` interface using the ServiceLoader mechanism.
 *
 * Plugins should include a `META-INF/services/com.friend.friend.Skill` file listing
 * the implementing class names.
 */
public class PluginLoader {
    private static final Logger logger = LoggerFactory.getLogger(PluginLoader.class);
    private final File pluginsDir;

    public PluginLoader(File pluginsDir) {
        this.pluginsDir = pluginsDir;
    }

    public List<Skill> loadPlugins(CommandDispatcher dispatcher) {
        List<Skill> loaded = new ArrayList<>();
        if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
            logger.info("Plugins directory does not exist: {}", pluginsDir.getAbsolutePath());
            return loaded;
        }

        File[] jars = pluginsDir.listFiles((d, name) -> name.toLowerCase().endsWith(".jar"));
        if (jars == null) return loaded;

        for (File jar : jars) {
            try {
                URL url = jar.toURI().toURL();
                URLClassLoader cl = new URLClassLoader(new URL[]{url}, this.getClass().getClassLoader());
                ServiceLoader<Skill> serviceLoader = ServiceLoader.load(Skill.class, cl);
                Iterator<Skill> it = serviceLoader.iterator();
                while (it.hasNext()) {
                    try {
                        Skill skill = it.next();
                        logger.info("Discovered skill: {} from {}", skill.getName(), jar.getName());
                        skill.register(dispatcher);
                        try { skill.start(); } catch (Throwable t) { logger.warn("Skill start failed", t); }
                        loaded.add(skill);
                    } catch (Throwable inner) {
                        logger.warn("Failed to instantiate Skill from {}: {}", jar.getName(), inner.getMessage());
                    }
                }
            } catch (Throwable t) {
                logger.error("Failed to load plugin jar: " + jar.getAbsolutePath(), t);
            }
        }

        return loaded;
    }
}
