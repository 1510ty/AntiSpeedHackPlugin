package com.mc1510ty.antiSpeedHackPlugin_PaperVer;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class AntiSpeedHackPlugin_PaperVer extends JavaPlugin implements Listener {

    private final Map<Player, Location> lastValidLocations = new HashMap<>();
    private static final double MAX_ALLOWED_SPEED = 0.8; // ダッシュジャンプ時の最大速度程度

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiSpeedHackプラグイン (Paper版) が有効になりました！");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("AntiSpeedHackプラグイン (Paper版) が無効になりました！");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // クリエイティブ・スペクテイターモードのプレイヤーは除外
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        // 移動がない場合、または下方向の移動は無視
        if (to == null || from.equals(to) || to.getY() < from.getY()) {
            return;
        }

        double distance = from.distance(to);

        if (distance > MAX_ALLOWED_SPEED) {
            Location lastValidLocation = lastValidLocations.get(player);
            if (lastValidLocation != null) {
                // Paper/Folia環境では、グローバルリージョンスケジューラを利用して安全に実行
                if (Bukkit.getGlobalRegionScheduler() != null) {
                    Bukkit.getGlobalRegionScheduler().run(this, task -> {
                        player.teleport(lastValidLocation);
                        player.sendMessage("不正な速度が検出されました！");
                    });
                } else {
                    // 万が一グローバルリージョンスケジューラが利用できない場合（通常はありえません）
                    player.teleport(lastValidLocation);
                    player.sendMessage("不正な速度が検出されました！");
                }
            } else {
                event.setCancelled(true);
            }
        } else {
            // 有効な位置として記録
            lastValidLocations.put(player, from.clone());
        }
    }
}