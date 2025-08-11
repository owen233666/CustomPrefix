package cn.owen233666.customPrefix.commands;

import cn.owen233666.customPrefix.CustomPrefix;
import cn.owen233666.customPrefix.PrefixUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class customprefixCommand implements TabExecutor, Listener {
    private static final    Plugin              PLUGIN          = Bukkit.getPluginManager().getPlugin("CustomPrefix");
    private static final    Component           HINT_COMPONENT  = Component.text("在上方的文本框中输入您的称号，点击结果栏中确认更改").color(NamedTextColor.YELLOW);
    private static final    Component           RESULT_LORE     = Component.text("点击修改您的称号").color(NamedTextColor.GREEN);
    private static final    NamespacedKey       KEY             = new NamespacedKey(PLUGIN, "is_custom_prefix_anvil");
    private final           Map<UUID, String>   playerInput     = new HashMap<>();
    private final           Set<Inventory>      customAnvils    = new HashSet<>();
    private static          Boolean             isPrefixLegal   = false;

    public customprefixCommand() {
        PLUGIN.getLogger().info("Protocollib注册器已注册"); //for debug
        registerPacketListener();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player) {
            if ((cmd.getName().equalsIgnoreCase("customprefix") || cmd.getName().equalsIgnoreCase("prefix")) &&
                    args.length > 0 && args[0].equalsIgnoreCase("upload")) {

                // 创建自定义铁砧界面并加入跟踪集合
                Inventory prefixInventory = Bukkit.createInventory(player, InventoryType.ANVIL, "请输入您的称号");
                customAnvils.add(prefixInventory);

                // 创建提示物品
                ItemStack hint = new ItemStack(Material.PAPER);
                ItemMeta hintMeta = hint.getItemMeta();
                hintMeta.lore(Collections.singletonList(HINT_COMPONENT));

                PersistentDataContainer container = hintMeta.getPersistentDataContainer();
                container.set(KEY, PersistentDataType.BOOLEAN, true);
                container.set(new NamespacedKey(PLUGIN, CustomPrefix.NAMESPACE),
                        PersistentDataType.STRING, player.getUniqueId().toString());

                hint.setItemMeta(hintMeta);
                prefixInventory.setItem(0, hint);

                // 初始化结果槽
                updateResultSlot(prefixInventory, Component.empty());
                playerInput.put(player.getUniqueId(), "");

                player.openInventory(prefixInventory);

//                PLUGIN.getLogger().info("onCommand(),args[0] == upload被触发"); // for debug
                PLUGIN.getLogger().info("玩家"+player.getName()+" "+player.getUniqueId()+"的playerInput是"+playerInput.get(player.getUniqueId()));//fot debug

                return true;
            } else if (args.length > 0 && args[0].equalsIgnoreCase("clear")) {
                PrefixUtils.setPrefix(player, "");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>称号已清除！"));
                return true;
            } else if (args.length > 0 && args[0].equalsIgnoreCase("preview")) {
                return true;
            } else if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
                sendHelpMessage(player);
                return true;
            }
        } else {
            if (PLUGIN != null) {
                PLUGIN.getLogger().info("只有玩家才能使用此命令！");
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
//            PLUGIN.getLogger().info("onTabComplete()被触发"); //for debug
            return Arrays.asList("upload", "clear", "preview", "help");
        }
        return Collections.emptyList();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        if (!customAnvils.contains(inv)) return;

        event.setCancelled(true);

        // 处理结果槽点击（索引2）
        if (event.getRawSlot() == 2) {
            String inputText = playerInput.getOrDefault(player.getUniqueId(), "");
            PLUGIN.getLogger().info("符合点击位置但没判定playerInput的onInventory()被触发"); //for debug
            if (!inputText.isEmpty()) {
                PrefixUtils.setPrefix(player, inputText);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>称号修改成功！"));
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>请输入有效的称号！"));
            }
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            Inventory inv = event.getInventory();
            if (customAnvils.remove(inv)) {
                PLUGIN.getLogger().info("onInventoryClose()被触发"); //for debug
                playerInput.remove(player.getUniqueId());
            }
        }
    }

    // 更新结果槽物品
    private void updateResultSlot(Inventory anvilInventory, Component inputText) {
        ItemStack result = new ItemStack(Material.PAPER);
        ItemMeta resultMeta = result.getItemMeta();

        if (inputText == null) {
            resultMeta.displayName(Component.text("请输入称号").color(NamedTextColor.GRAY));
        } else {
            resultMeta.displayName(inputText);
        }

        resultMeta.lore(Collections.singletonList(RESULT_LORE));
        result.setItemMeta(resultMeta);
        anvilInventory.setItem(2, result);
    }

    // 注册数据包监听器获取铁砧输入
    private void registerPacketListener() {
        if (PLUGIN == null) return;

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(
                PLUGIN,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.ITEM_NAME) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
//                PLUGIN.getLogger().info("onPacketReceiving()触发"); //for debug
//                PLUGIN.getLogger().info("玩家："+player.getName()); //for debug
                if (player == null) return;

                // 检查玩家是否打开了自定义铁砧
                Inventory openInv = player.getOpenInventory().getTopInventory();
                if (openInv != null && customAnvils.contains(openInv)) {
//                    PLUGIN.getLogger().info("玩家 "+ player.getName() +" 打开的是自定义铁砧");// for debug
                    // 获取输入的文本
                    String inputText = "";
                    try {
                        if (event.getPacket().getStrings().size() > 0) {
                            inputText = event.getPacket().getStrings().read(0);
                        }
                    } catch (Exception e) {
                        if (PLUGIN != null) {
                            PLUGIN.getLogger().warning("获取输入文本失败: " + e.getMessage());
                        }
                        return;
                    }

                    // 过滤纯空白输入
                    if (inputText != null && !inputText.trim().isEmpty()) {
                        playerInput.put(player.getUniqueId(), inputText);
                        updateResultSlot(openInv, MiniMessage.miniMessage().deserialize(inputText));
                    } else {
                        playerInput.put(player.getUniqueId(), "");
                        updateResultSlot(openInv, MiniMessage.miniMessage().deserialize("<red>输入的称号不符合规则！"));
                    }
                    event.setCancelled(true);
                }
            }
        });
    }

    // 发送帮助信息
    private void sendHelpMessage(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>===== 称号命令帮助 ====="));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>/prefix upload</green> <gray>- 打开界面设置称号"));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>/prefix clear</green> <gray>- 清除当前称号"));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>/prefix preview</green> <gray>- 预览当前称号"));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>/prefix help</green> <gray>- 显示帮助信息"));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>======================"));
    }

    // 检查物品是否有自定义铁砧标记
    private boolean hasCustomAnvilMarker(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(KEY, PersistentDataType.BOOLEAN);
    }
}
