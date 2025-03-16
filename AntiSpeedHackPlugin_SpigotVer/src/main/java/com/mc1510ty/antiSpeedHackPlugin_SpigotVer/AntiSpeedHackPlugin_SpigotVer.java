package com.mc1510ty.antiSpeedHackPlugin_SpigotVer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AntiSpeedHackPlugin_SpigotVer extends JavaPlugin implements Listener {

    private final Map<Player, Location> lastValidLocations = new HashMap<>();
    private static final double MAX_ALLOWED_SPEED = 0.8; // 許容される水平移動距離
    // ロケット花火使用中のプレイヤーのUUIDを管理するセット
    private final Set<UUID> rocketUsageSet = new HashSet<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiSpeedHackプラグインが有効になりました！");
    }

    @Override
    public void onDisable() {
        getLogger().info("AntiSpeedHackプラグインが無効になりました！");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // クリエイティブ・スペクテイターモードのプレイヤーは除外
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // エリトラでグライド中、または直近にロケット花火を使用している場合はチェック対象外にする
        if (player.isGliding() || rocketUsageSet.contains(player.getUniqueId())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        // 移動がない場合は無視
        if (to == null || from.equals(to)) {
            return;
        }

        // 水平移動のみを計算 (X, Z 軸)
        double deltaX = to.getX() - from.getX();
        double deltaZ = to.getZ() - from.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // 不正な速度を検出した場合の処理
        if (horizontalDistance > MAX_ALLOWED_SPEED) {
            Location lastValidLocation = lastValidLocations.get(player);
            if (lastValidLocation != null) {
                player.teleport(lastValidLocation);
                player.sendMessage("不正な速度が検出されました！");
            } else {
                event.setCancelled(true);
            }
        } else {
            // 正常な移動であれば有効な位置を記録
            lastValidLocations.put(player, from.clone());
        }
    }

    // ロケット花火使用を検知するイベントハンドラー例 (右クリックで使用)
    @EventHandler
    public void onPlayerUseFirework(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        // 右クリック（空中またはブロック）で、手に持っているアイテムがロケット花火の場合
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && player.getInventory().getItemInMainHand().getType() == Material.FIREWORK_ROCKET) {
            rocketUsageSet.add(player.getUniqueId());
            // 5秒後（100ティック後）にフラグを除去
            new BukkitRunnable() {
                @Override
                public void run() {
                    rocketUsageSet.remove(player.getUniqueId());
                }
            }.runTaskLater(this, 100L);
        }
    }
}
