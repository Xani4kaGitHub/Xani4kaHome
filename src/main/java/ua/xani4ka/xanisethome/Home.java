package ua.xani4ka.xanisethome;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class Home {
    private final String name;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public Home(String name, String worldName, double x, double y, double z, float yaw, float pitch) {
        this.name = name;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static Home fromLocation(String name, Location location) {
        World world = location.getWorld();
        String worldName = world == null ? "unknown" : world.getName();
        return new Home(name, worldName, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public String getName() {
        return this.name;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public boolean isInWorld(String worldName) {
        return this.worldName.equalsIgnoreCase(worldName);
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(this.worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
    }
}
