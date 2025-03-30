package net.kinoko2k.bowAttack.utils;


import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {
    public static Location getStartLocation1() {
        return new Location(Bukkit.getWorld("world"), 132,  -57, 99);
    }

    public static Location getStartLocation2() {
        return new Location(Bukkit.getWorld("world"), 100, -57, 99);
    }

    public static Location getLobbyLocation() {
        return new Location(Bukkit.getWorld("world"), 117, -60, 75);
    }
}