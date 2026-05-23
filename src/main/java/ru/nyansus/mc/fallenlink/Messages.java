package ru.nyansus.mc.fallenlink;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Messages {

    private static final String FALLBACK_LOCALE = "en";
    private static final Map<String, String> LOCALE_MAP = Map.of(
            "ru", "ru",
            "en", "en",
            "ru_ru", "ru",
            "en_us", "en",
            "en_gb", "en"
    );

    private final JavaPlugin plugin;
    private final Map<String, YamlConfiguration> locales = new HashMap<>();
    private String defaultLocale = FALLBACK_LOCALE;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void reload() {
        load();
    }

    public String get(CommandSender sender, String key, String... replacements) {
        String locale = defaultLocale;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            locale = normalizeLocale(player.locale().toString());
        }
        return resolve(locale, key, replacements);
    }

    public String get(String key, String... replacements) {
        return resolve(defaultLocale, key, replacements);
    }

    private void load() {
        locales.clear();
        loadLocale("ru", "messages_ru.yml");
        loadLocale("en", "messages_en.yml");
        defaultLocale = plugin.getConfig().getString("default-locale", FALLBACK_LOCALE);
        if (!locales.containsKey(defaultLocale)) {
            defaultLocale = FALLBACK_LOCALE;
        }
    }

    private void loadLocale(String locale, String resource) {
        try (InputStream stream = plugin.getResource(resource)) {
            if (stream == null) {
                return;
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(stream, StandardCharsets.UTF_8));
            locales.put(locale, config);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING,
                    get("log.locale-load-failed", "{resource}", resource), e);
        }
    }

    private String resolve(String locale, String key, String... replacements) {
        String message = getFromLocale(locale, key);
        if (message == null && !FALLBACK_LOCALE.equals(locale)) {
            message = getFromLocale(FALLBACK_LOCALE, key);
        }
        if (message == null) {
            message = "[" + key + "]";
        }
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return message;
    }

    private String getFromLocale(String locale, String key) {
        YamlConfiguration config = locales.get(locale);
        if (config == null) {
            return null;
        }
        return config.getString(key);
    }

    private static String normalizeLocale(String clientLocale) {
        if (clientLocale == null || clientLocale.isEmpty()) {
            return FALLBACK_LOCALE;
        }
        String lower = clientLocale.toLowerCase(Locale.ROOT);
        return LOCALE_MAP.getOrDefault(lower, lower.split("_")[0]);
    }
}
