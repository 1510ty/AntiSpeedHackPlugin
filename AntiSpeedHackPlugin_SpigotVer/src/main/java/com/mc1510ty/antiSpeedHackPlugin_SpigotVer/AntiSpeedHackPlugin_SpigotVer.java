package com.mc1510ty.antiSpeedHackPlugin_SpigotVer;

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

public class AntiSpeedHackPlugin_SpigotVer extends JavaPlugin implements Listener {

    private final Map<Player, Location> lastValidLocations = new HashMap<>();
    private static final double MAX_ALLOWED_SPEED = 0.8; // ダッシュジャンプ時の最大速度程度

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiSpeedHackプラグインが有効になりました！");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("AntiSpeedHackプラグインが無効になりました！");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // クリエイティブ・スペクテイターモードのプレイヤーを除外
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        // 移動がない場合は無視
        if (to == null || from.equals(to)) {
            return;
        }

        // 下方向の移動は無視
        if (to.getY() < from.getY()) {
            return;
        }

        // 移動距離を計算
        double distance = from.distance(to);

        // 不正な速度を検出した場合
        if (distance > MAX_ALLOWED_SPEED) {
            // プレイヤーを直前の有効な位置に戻す
            Location lastValidLocation = lastValidLocations.get(player);
            if (lastValidLocation != null) {
                player.teleport(lastValidLocation);
                player.sendMessage("不正な速度が検出されました！");
            } else {
                // 最初のケースでは、イベントをキャンセル
                event.setCancelled(true);
            }
        } else {
            // 有効な位置として記録
            lastValidLocations.put(player, from.clone());
        }
    }
}