package ru.nyansus.mc.fallenlink.support;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class TestPlayers {

    private TestPlayers() {
    }

    public static Player player(String name, List<String> messages) {
        return player(name, messages, Map.of());
    }

    public static Player player(String name, List<String> messages, Map<String, Integer> statistics) {
        UUID uuid = UUID.nameUUIDFromBytes(name.getBytes());
        World world = world("world");
        Location location = new Location(world, 1.234D, 64.0D, -2.345D);
        Map<String, Object> values = new HashMap<>();
        values.put("getUniqueId", uuid);
        values.put("getName", name);
        values.put("getLevel", 12);
        values.put("getExp", 0.456F);
        values.put("getHealth", 19.876D);
        values.put("getFoodLevel", 18);
        values.put("getFirstPlayed", 0L);
        values.put("getWorld", world);
        values.put("getLocation", location);

        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> {
                    if ("sendMessage".equals(method.getName()) && args != null && args.length > 0) {
                        collectMessages(messages, args);
                        return null;
                    }
                    if ("getStatistic".equals(method.getName())) {
                        return statistic(statistics, args);
                    }
                    if ("toString".equals(method.getName())) {
                        return "TestPlayer[" + name + "]";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return uuid.hashCode();
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    if (values.containsKey(method.getName())) {
                        return values.get(method.getName());
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    public static World world(String name) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    if ("getName".equals(method.getName())) {
                        return name;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static int statistic(Map<String, Integer> statistics, Object[] args) {
        if (args == null || args.length == 0) {
            return 0;
        }
        String key = args[0].toString();
        if (args.length > 1) {
            key += ":" + args[1].toString();
        }
        return statistics.getOrDefault(key, 0);
    }

    private static void collectMessages(List<String> messages, Object[] args) {
        for (Object argument : args) {
            if (argument instanceof String) {
                messages.add((String) argument);
            } else if (argument instanceof String[]) {
                for (String message : (String[]) argument) {
                    messages.add(message);
                }
            }
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0.0F;
        }
        return 0.0D;
    }

    public static List<String> messages() {
        return new ArrayList<>();
    }
}
