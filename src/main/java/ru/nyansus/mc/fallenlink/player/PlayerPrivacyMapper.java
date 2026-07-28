package ru.nyansus.mc.fallenlink.player;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.nyansus.mc.fallenlink.config.PrivacyPolicy;
import ru.nyansus.mc.fallenlink.model.Position;

public final class PlayerPrivacyMapper {

    public String worldName(World world, PrivacyPolicy privacy) {
        if (!privacy.isSendPrivateData()) {
            return privacy.getFakeWorld();
        }
        return world == null ? "" : world.getName();
    }

    public Position position(Location location, PrivacyPolicy privacy) {
        if (!privacy.isSendPrivateData()) {
            double coordinate = privacy.getFakeCoordinate();
            return new Position(coordinate, coordinate, coordinate);
        }
        return new Position(
                location == null ? 0.0D : round(location.getX()),
                location == null ? 0.0D : round(location.getY()),
                location == null ? 0.0D : round(location.getZ())
        );
    }

    public int level(Player player, PrivacyPolicy privacy) {
        return privacy.isSendPrivateData() ? player.getLevel() : privacy.getFakeLevel();
    }

    public double exp(Player player, PrivacyPolicy privacy) {
        return privacy.isSendPrivateData() ? round(player.getExp()) : privacy.getFakeExp();
    }

    public double health(Player player, PrivacyPolicy privacy) {
        return privacy.isSendPrivateData() ? round(player.getHealth()) : privacy.getFakeHealth();
    }

    public int food(Player player, PrivacyPolicy privacy) {
        return privacy.isSendPrivateData() ? player.getFoodLevel() : privacy.getFakeFood();
    }

    private double round(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }
}
