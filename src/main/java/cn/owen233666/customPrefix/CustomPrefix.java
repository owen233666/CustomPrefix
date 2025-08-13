package cn.owen233666.customPrefix;

import cn.owen233666.customPrefix.commands.CustomPrefixCommand;
import cn.owen233666.customPrefix.commands.customprefixCommand_old;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomPrefix extends JavaPlugin {

    public static final String NAMESPACE = "customprefix";

    @Override
    public void onEnable() {
        // 1. 先检测LuckPerms，再初始化其他逻辑
        Plugin luckperms = Bukkit.getPluginManager().getPlugin("LuckPerms");
        if (luckperms == null || !luckperms.isEnabled()) {
            getLogger().warning("未找到 LuckPerms！插件无法加载。");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }else{
            getLogger().info("Luckperms已找到！");
        }

        // 2. 创建唯一的命令实例
        CustomPrefixCommand command = new CustomPrefixCommand();

        // 3. 注册事件和命令
        getServer().getPluginManager().registerEvents(command, this);
        if (getCommand("customprefix") != null) {
            getCommand("customprefix").setExecutor(command);
        }
        if (getCommand("customprefixadmin") != null) {
            getCommand("customprefixadmin").setExecutor(command);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("插件已卸载");
    }
}
