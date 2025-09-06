package de.example.autarnis;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public class AutoArnisGenerator extends JavaPlugin implements Listener {
    private String arnisCmd;
    private double radiusMeters;
    private String worldName;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.arnisCmd = getConfig().getString("arnis-path", "/usr/local/bin/arnis");
        this.radiusMeters = getConfig().getDouble("radius-meters", 500);
        this.worldName = getConfig().getString("world-name", "world");

        installHeightDatapack();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("AutoArnisGenerator enabled");
    }

    private void installHeightDatapack() {
        File dpRoot = new File(Bukkit.getWorldContainer(), "datapacks/height");
        File mcmeta = new File(dpRoot, "pack.mcmeta");
        File dimDir = new File(dpRoot, "data/minecraft/dimension_type");
        File overworld = new File(dimDir, "overworld.json");

        try {
            if (!mcmeta.exists()) {
                dpRoot.mkdirs();
                dimDir.mkdirs();

                String mcmetaText = "{\n" +
                        "  \"pack\": {\n" +
                        "    \"pack_format\": 32,\n" +
                        "    \"description\": \"Max Height Datapack\"\n" +
                        "  }\n" +
                        "}\n";
                java.nio.file.Files.writeString(mcmeta.toPath(), mcmetaText);

                String dimJson = "{\n" +
                        "  \"ultrawarm\": false,\n" +
                        "  \"natural\": true,\n" +
                        "  \"coordinate_scale\": 1.0,\n" +
                        "  \"has_skylight\": true,\n" +
                        "  \"has_ceiling\": false,\n" +
                        "  \"ambient_light\": 0.0,\n" +
                        "  \"fixed_time\": null,\n" +
                        "  \"piglin_safe\": false,\n" +
                        "  \"bed_works\": true,\n" +
                        "  \"respawn_anchor_works\": false,\n" +
                        "  \"min_y\": -2048,\n" +
                        "  \"height\": 4096,\n" +
                        "  \"logical_height\": 4096,\n" +
                        "  \"infiniburn\": \"#minecraft:infiniburn_overworld\",\n" +
                        "  \"effects\": \"minecraft:overworld\",\n" +
                        "  \"monster_spawn_light_level\": {\n" +
                        "    \"type\": \"minecraft:uniform\",\n" +
                        "    \"value\": [0, 7]\n" +
                        "  },\n" +
                        "  \"monster_spawn_block_light_limit\": 0\n" +
                        "}\n";
                java.nio.file.Files.writeString(overworld.toPath(), dimJson);

                getLogger().info("Height Datapack installiert. Bitte /reload ausführen.");
            }
        } catch (IOException e) {
            getLogger().severe("Konnte Height Datapack nicht schreiben: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent ev) {
        Location from = ev.getFrom();
        Location to = ev.getTo();
        if (to == null) return;
        if (from.getChunk().getX() == to.getChunk().getX() &&
            from.getChunk().getZ() == to.getChunk().getZ()) return;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        // Platzhalter: einfache Näherung
        double lat = to.getZ() * 0.00001;
        double lon = to.getX() * 0.00001;

        double degLat = radiusMeters / 111320.0;
        double degLon = radiusMeters / (111320.0 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - degLat;
        double maxLat = lat + degLat;
        double minLon = lon - degLon;
        double maxLon = lon + degLon;

        String bboxArg = String.format("%f,%f,%f,%f", minLon, minLat, maxLon, maxLat);
        String outputDir = Bukkit.getWorldContainer().getAbsolutePath() + "/" + worldName;

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                        arnisCmd,
                        "generate",
                        "--bbox", bboxArg,
                        "--output", outputDir
                    );
                    pb.redirectErrorStream(true);
                    Process p = pb.start();
                    int exit = p.waitFor();
                    if (exit == 0) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                int cx = to.getChunk().getX();
                                int cz = to.getChunk().getZ();
                                for (int dx=-1; dx<=1; dx++) {
                                    for (int dz=-1; dz<=1; dz++) {
                                        world.loadChunk(cx+dx, cz+dz, true);
                                    }
                                }
                            }
                        }.runTask(AutoArnisGenerator.this);
                    } else {
                        getLogger().warning("Arnis exited with code " + exit);
                    }
                } catch (IOException | InterruptedException ex) {
                    getLogger().severe("AutoArnis error: " + ex.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
    }
          }
