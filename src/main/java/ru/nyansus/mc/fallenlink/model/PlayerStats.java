package ru.nyansus.mc.fallenlink.model;

public final class PlayerStats {

    private final int playtimeSeconds;
    private final int deaths;
    private final int playerKills;
    private final int mobKills;
    private final int jumps;
    private final int damageDealt;
    private final int damageTaken;
    private final int distanceCm;
    private final int blocksMined;
    private final int animalsBred;
    private final int fishCaught;
    private final int level;
    private final double exp;
    private final double health;
    private final int food;

    public PlayerStats(
            int playtimeSeconds,
            int deaths,
            int playerKills,
            int mobKills,
            int jumps,
            int damageDealt,
            int damageTaken,
            int distanceCm,
            int blocksMined,
            int animalsBred,
            int fishCaught,
            int level,
            double exp,
            double health,
            int food
    ) {
        this.playtimeSeconds = playtimeSeconds;
        this.deaths = deaths;
        this.playerKills = playerKills;
        this.mobKills = mobKills;
        this.jumps = jumps;
        this.damageDealt = damageDealt;
        this.damageTaken = damageTaken;
        this.distanceCm = distanceCm;
        this.blocksMined = blocksMined;
        this.animalsBred = animalsBred;
        this.fishCaught = fishCaught;
        this.level = level;
        this.exp = exp;
        this.health = health;
        this.food = food;
    }

    public int getPlaytimeSeconds() {
        return playtimeSeconds;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getPlayerKills() {
        return playerKills;
    }

    public int getMobKills() {
        return mobKills;
    }

    public int getJumps() {
        return jumps;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public int getDamageTaken() {
        return damageTaken;
    }

    public int getDistanceCm() {
        return distanceCm;
    }

    public int getBlocksMined() {
        return blocksMined;
    }

    public int getAnimalsBred() {
        return animalsBred;
    }

    public int getFishCaught() {
        return fishCaught;
    }

    public int getLevel() {
        return level;
    }

    public double getExp() {
        return exp;
    }

    public double getHealth() {
        return health;
    }

    public int getFood() {
        return food;
    }
}
