/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.core;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.epic.exoticgarden.EpicExoticGarden;
import com.epic.exoticgarden.compat.BlockIds;
import com.epic.exoticgarden.compat.ItemTags;
import com.epic.exoticgarden.compat.Materials;

/**
 * Persistent per-block id storage backed by SQLite ({@code blocks.db}).
 *
 * <p>Standalone replacement for Slimefun's {@code BlockStorage}: maps block locations to the
 * EpicExoticGarden item id that was planted/grown there. Reads are served from an in-memory
 * cache; every mutation is written through to the database immediately (WAL journal), so a
 * server crash can no longer wipe minutes of placements the way the old periodic
 * {@code blocks.yml} rewrite did. On first start after the upgrade, any legacy
 * {@code blocks.yml} is imported and renamed to {@code blocks.yml.migrated}.</p>
 */
public final class Blocks {

    private Blocks() {}

    private static final Map<String, String> DATA = new ConcurrentHashMap<>();

    private static Logger logger;
    private static Connection db;
    private static PreparedStatement upsert;
    private static PreparedStatement delete;

    public static void init(@Nonnull EpicExoticGarden plugin) {
        logger = plugin.getLogger();
        DATA.clear();
        closeQuietly();

        try {
            Class.forName("org.sqlite.JDBC");
            File dir = new File(plugin.getDataFolder(), "blocksdata");
            if (!dir.exists() && !dir.mkdirs()) {
                logger.warning("无法创建目录: " + dir.getPath());
            }
            relocateLegacyDatabase(plugin.getDataFolder(), dir);
            File file = new File(dir, "blocks.db");
            db = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());

            try (Statement st = db.createStatement()) {
                // FULL: fsync after every transaction so committed rows survive even a
                // hard process kill or power loss (NORMAL could drop the last batch when
                // the console window is closed without /stop).
                st.execute("PRAGMA journal_mode=WAL");
                st.execute("PRAGMA synchronous=FULL");
                st.execute("PRAGMA busy_timeout=5000");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS blocks ("
                        + "world TEXT NOT NULL, x INTEGER NOT NULL, y INTEGER NOT NULL, z INTEGER NOT NULL, "
                        + "id TEXT NOT NULL, PRIMARY KEY (world, x, y, z))");
            }

            migrateLegacyYaml(plugin);

            try (Statement st = db.createStatement();
                 ResultSet rs = st.executeQuery("SELECT world, x, y, z, id FROM blocks")) {
                while (rs.next()) {
                    DATA.put(rs.getString(1) + ";" + rs.getInt(2) + ";" + rs.getInt(3) + ";" + rs.getInt(4),
                            rs.getString(5));
                }
            }

            upsert = db.prepareStatement(
                    "INSERT OR REPLACE INTO blocks (world, x, y, z, id) VALUES (?, ?, ?, ?, ?)");
            delete = db.prepareStatement("DELETE FROM blocks WHERE world=? AND x=? AND y=? AND z=?");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "无法初始化 blocks.db，方块数据本次运行将不会持久化", e);
            closeQuietly();
        }
    }

    public static void shutdown(@Nonnull EpicExoticGarden plugin) {
        closeQuietly();
    }

    /**
     * Moves a {@code blocks.db} created by an older version (directly in the plugin folder) into
     * {@code blocksdata/}, along with any WAL/SHM sidecar files. No-op when the new file already
     * exists or there is nothing to move.
     */
    private static void relocateLegacyDatabase(@Nonnull File dataFolder, @Nonnull File dir) {
        File oldFile = new File(dataFolder, "blocks.db");
        File newFile = new File(dir, "blocks.db");
        if (!oldFile.exists() || newFile.exists()) {
            return;
        }
        if (oldFile.renameTo(newFile)) {
            new File(dataFolder, "blocks.db-wal").renameTo(new File(dir, "blocks.db-wal"));
            new File(dataFolder, "blocks.db-shm").renameTo(new File(dir, "blocks.db-shm"));
            logger.info("已将 blocks.db 迁移至 blocksdata/ 目录");
        } else {
            logger.warning("无法将旧的 blocks.db 移动到 blocksdata/ 目录，将使用空数据库");
        }
    }

    /**
     * Imports the legacy {@code blocks.yml} (keys {@code world;x;y;z}) into the database, then
     * renames the file so the migration runs only once. Existing rows win (INSERT OR IGNORE).
     */
    private static void migrateLegacyYaml(@Nonnull EpicExoticGarden plugin) throws SQLException {
        File legacy = new File(plugin.getDataFolder(), "blocks.yml");
        if (!legacy.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(legacy);
        ConfigurationSection section = yaml.getConfigurationSection("blocks");
        if (section == null) {
            return;
        }

        int migrated = 0;
        db.setAutoCommit(false);
        try (PreparedStatement insert = db.prepareStatement(
                "INSERT OR IGNORE INTO blocks (world, x, y, z, id) VALUES (?, ?, ?, ?, ?)")) {
            for (String key : section.getKeys(false)) {
                String id = section.getString(key);
                if (id == null) {
                    continue;
                }
                // Parse from the end so world names containing ';' still split correctly.
                int i3 = key.lastIndexOf(';');
                int i2 = i3 < 0 ? -1 : key.lastIndexOf(';', i3 - 1);
                int i1 = i2 < 0 ? -1 : key.lastIndexOf(';', i2 - 1);
                try {
                    if (i1 < 0) {
                        throw new NumberFormatException("bad key");
                    }
                    insert.setString(1, key.substring(0, i1));
                    insert.setInt(2, Integer.parseInt(key.substring(i1 + 1, i2)));
                    insert.setInt(3, Integer.parseInt(key.substring(i2 + 1, i3)));
                    insert.setInt(4, Integer.parseInt(key.substring(i3 + 1)));
                    insert.setString(5, id);
                    insert.addBatch();
                    migrated++;
                } catch (NumberFormatException e) {
                    logger.warning("跳过无法解析的 blocks.yml 条目: " + key);
                }
            }
            insert.executeBatch();
            db.commit();
        } finally {
            db.setAutoCommit(true);
        }

        File backup = new File(plugin.getDataFolder(), "blocks.yml.migrated");
        if (backup.exists()) {
            backup.delete();
        }
        if (legacy.renameTo(backup)) {
            logger.info("已将 blocks.yml 迁移至 blocks.db（" + migrated + " 条），原文件重命名为 blocks.yml.migrated");
        } else {
            logger.warning("blocks.yml 已导入 blocks.db（" + migrated + " 条），但重命名失败，请手动删除该文件");
        }
    }

    @Nonnull
    private static String key(@Nonnull Location loc) {
        World world = loc.getWorld();
        return (world == null ? "?" : world.getName()) + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }

    @Nullable
    public static String idAt(@Nullable Block block) {
        if (block == null) {
            return null;
        }
        String id = DATA.get(key(block.getLocation()));
        if (id != null) {
            return id;
        }

        // Self-heal: a ripe fruit head carries its id on its own tile state. If the database
        // record went missing (piston move, failed write, external edit) recover the id from
        // the block itself and re-populate both cache and database.
        if (block.getType() == Materials.PLAYER_HEAD.material) {
            String tag = BlockIds.getId(block);
            if (tag != null) {
                DATA.put(key(block.getLocation()), tag);
                write(block.getLocation(), tag);
                return tag;
            }
        }
        return null;
    }

    public static void store(@Nonnull Block block, @Nonnull String id) {
        Location loc = block.getLocation();
        DATA.put(key(loc), id);
        write(loc, id);

        // Duplicate the id onto the fruit head itself so it stays identifiable even if the
        // coordinate record is lost. Must run after the block is actually a head (callers
        // set the block type before storing); on non-head blocks this is a harmless no-op.
        if (block.getType() == Materials.PLAYER_HEAD.material) {
            BlockIds.setId(block, id);
        }
    }

    public static void store(@Nonnull Block block, @Nullable ItemStack item) {
        String id = ItemTags.getId(item);
        if (id != null) {
            store(block, id);
        }
    }

    public static void remove(@Nonnull Block block) {
        Location loc = block.getLocation();
        DATA.remove(key(loc));

        if (block.getType() == Materials.PLAYER_HEAD.material) {
            BlockIds.removeId(block);
        }

        if (delete == null) {
            return;
        }
        try {
            fill(delete, loc);
            delete.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "删除方块数据失败: " + key(loc), e);
        }
    }

    private static void write(@Nonnull Location loc, @Nonnull String id) {
        if (upsert == null) {
            return;
        }
        try {
            fill(upsert, loc);
            upsert.setString(5, id);
            upsert.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "保存方块数据失败: " + key(loc), e);
        }
    }

    private static void fill(@Nonnull PreparedStatement st, @Nonnull Location loc) throws SQLException {
        World world = loc.getWorld();
        st.setString(1, world == null ? "?" : world.getName());
        st.setInt(2, loc.getBlockX());
        st.setInt(3, loc.getBlockY());
        st.setInt(4, loc.getBlockZ());
    }

    /**
     * Returns a fresh copy of the item stored at this block, or {@code null} for plain blocks.
     */
    @Nullable
    public static ItemStack retrieve(@Nonnull Block block) {
        String id = idAt(block);
        return id == null ? null : com.epic.exoticgarden.item.Items.get(id);
    }

    public static int count() {
        return DATA.size();
    }

    /**
     * Merges committed frames from the WAL file into the main database file. Running this
     * periodically means the data no longer lives only in {@code blocks.db-wal}: even if the
     * sidecar file is lost (backup tooling, panel file manager) or the process is killed
     * without {@code /stop}, the main database file already contains every block record.
     */
    public static void checkpoint() {
        if (db == null) {
            return;
        }
        try (Statement st = db.createStatement()) {
            st.execute("PRAGMA wal_checkpoint(PASSIVE)");
        } catch (SQLException e) {
            // A concurrent reader can briefly block a checkpoint; the next run covers it.
            if (logger != null) {
                logger.log(Level.FINE, "blocks.db checkpoint 失败", e);
            }
        }
    }

    /**
     * Registers periodic durability tasks: WAL checkpoints merge recent transactions into the
     * main database file before they can be trapped in the sidecar log.
     */
    public static void scheduleMaintenance(@Nonnull EpicExoticGarden plugin) {
        long period = 20L * 60L; // once per minute
        plugin.getServer().getScheduler().runTaskTimer(plugin, Blocks::checkpoint, period, period);
    }

    private static void closeQuietly() {
        try {
            if (upsert != null) {
                upsert.close();
            }
            if (delete != null) {
                delete.close();
            }
            if (db != null) {
                db.close();
            }
        } catch (SQLException e) {
            if (logger != null) {
                logger.log(Level.WARNING, "关闭 blocks.db 失败", e);
            }
        }
        upsert = null;
        delete = null;
        db = null;
    }
}
