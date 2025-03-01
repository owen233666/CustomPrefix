package cn.owen233666.customPrefix;

import cn.owen233666.customPrefix.Commands.Commands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomPrefix extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Plugin luckperms = Bukkit.getPluginManager().getPlugin("LuckPerms");
        if (luckperms != null && luckperms.isEnabled()) {
            getLogger().info("已检测到 LuckPerms，版本: " + luckperms.getDescription().getVersion()); //输出lpversion
        } else {
            getLogger().warning("未找到 LuckPerms！插件无法加载。");
            // 处理没有 LuckPerms 的情况
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.getCommand("customprefix").setExecutor(new Commands());
        this.getCommand("customprefixadmin").setExecutor(new Commands());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
