package cn.owen233666.customPrefix.commands;

import cn.owen233666.customPrefix.CustomPrefix;
import cn.owen233666.customPrefix.PrefixUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class customprefixCommand_old implements TabExecutor, Listener {
    private static final    Plugin              PLUGIN                  = Bukkit.getPluginManager().getPlugin("CustomPrefix");
    private static final    Component           HINT_COMPONENT          = MiniMessage.miniMessage().deserialize("<reset><yellow>在上方的文本框中输入您的称号，点击结果栏中确认更改");
    private static final    Component           RESULT_LORE             = MiniMessage.miniMessage().deserialize("<reset><green>点击修改您的称号");
    private static final    NamespacedKey       IS_VIRTUAL_ANVIL_KEY    = new NamespacedKey(PLUGIN, "is_custom_prefix_anvil");
    private static final    NamespacedKey       IS_LEGAL_PREFIX_KEY     = new NamespacedKey(PLUGIN, "is_legal_prefix");
    private final           Map<UUID, String>   playerInput             = new HashMap<>();
    private final           Set<Inventory>      customAnvils            = new HashSet<>();

    public customprefixCommand_old() {
        registerPacketListener();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player) {
            if ((cmd.getName().equalsIgnoreCase("customprefix") || cmd.getName().equalsIgnoreCase("prefix")) && args.length > 0 && args[0].equalsIgnoreCase("upload")) {

                // 创建自定义铁砧界面并加入跟踪集合
                Inventory prefixInventory = Bukkit.createInventory(player, InventoryType.ANVIL, MiniMessage.miniMessage().deserialize("<reset>请输入您的称号"));
                customAnvils.add(prefixInventory);

                // 创建提示物品
                ItemStack   hint        =   new ItemStack(Material.PAPER);
                ItemMeta    hintMeta    =   hint.getItemMeta();
                hintMeta.customName(Component.empty());
                hintMeta.lore(Collections.singletonList(HINT_COMPONENT));
                PersistentDataContainer container = hintMeta.getPersistentDataContainer();
                container.set(IS_VIRTUAL_ANVIL_KEY,     PersistentDataType.BOOLEAN, true);
                container.set(new NamespacedKey(PLUGIN, CustomPrefix.NAMESPACE),    PersistentDataType.STRING,  player.getUniqueId().toString());
                hint.setItemMeta(hintMeta);
                prefixInventory.setItem(0, hint);

                // 初始化结果槽
                updateResultSlot(prefixInventory,       Component.empty(),  false);
                playerInput.put (player.getUniqueId(),  "");
                player.openInventory(prefixInventory);

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
            ItemStack clickedItem = event.getCurrentItem();

            // 检查点击的物品是否存在
            if (clickedItem == null || !clickedItem.hasItemMeta()) {
                return;
            }

            PersistentDataContainer clickedContainer = clickedItem.getItemMeta().getPersistentDataContainer();
            boolean isLegal = Boolean.TRUE.equals(clickedContainer.get(IS_LEGAL_PREFIX_KEY, PersistentDataType.BOOLEAN));

            if (isLegal && !inputText.isEmpty()) {
                PrefixUtils.setPrefix(player, inputText);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<reset><green>称号修改成功！"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0F, 1.0F);
                player.closeInventory();
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                if (inputText.isEmpty()) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<reset><red>请输入有效的称号！"));
                } else {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<reset><red>输入的称号不符合规则！"));
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            Inventory inv = event.getInventory();
            if (customAnvils.remove(inv)) {
                playerInput.remove(player.getUniqueId());
            }
        }
    }

    private void updateResultSlot(Inventory anvilInventory, Component inputText, boolean isLegalPrefixKey){
        ItemStack               result      =   new ItemStack(Material.PAPER);
        ItemMeta                resultMeta  =   result.getItemMeta();
        PersistentDataContainer container   =   resultMeta.getPersistentDataContainer();
        if (inputText == null) {
            container   .set            (IS_LEGAL_PREFIX_KEY, PersistentDataType.BOOLEAN, false);
            resultMeta  .displayName    (Component.text("请输入称号").color(NamedTextColor.GRAY));
        } else {
            container   .set            (IS_LEGAL_PREFIX_KEY, PersistentDataType.BOOLEAN, isLegalPrefixKey);
            resultMeta  .displayName    (inputText);
        }

        resultMeta      .lore           (Collections.singletonList(RESULT_LORE));
        result          .setItemMeta    (resultMeta);
        anvilInventory  .setItem        (2, result);
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
                if (player == null) return;

                // 检查玩家是否打开了自定义铁砧
                Inventory openInv = player.getOpenInventory().getTopInventory();
                if (openInv != null && customAnvils.contains(openInv)) {
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

                    // 处理输入文本
                    String processedText = inputText != null ? inputText.trim() : "";
                    boolean isLegal = !processedText.isEmpty() && PrefixLegalLookUp(processedText);

                    playerInput.put(player.getUniqueId(), processedText);

                    if (isLegal) {
                        updateResultSlot(openInv, MiniMessage.miniMessage().deserialize(processedText), true);
                    } else {
                        String message = processedText.isEmpty() ? "请输入有效的称号！" : "输入的称号不符合规则！";
                        updateResultSlot(openInv, MiniMessage.miniMessage().deserialize("<reset><red>" + message), false);
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

    /*
    #Decoration Name and Formats
    &l  =   <bold>          =   <b>             //legal
    &o  =   <italic>        =   <em>    =   <i> //legal
    &k  =   <obfuscated>    =   <obf>           //illegal
    &n  =   <underlined>    =   <u>             //legal
    &m  =   <strikethrough> =   <st>            //illegal
    &r  =   <reset>                             //legal

    #Actions - All illegal
    <click:*>

    #Hover - All legal
    <hover:*>

    #Insertion - All legal
    <insert:*> - </insert>/<reset>

    #Colors - Below are color formats, it's fricking muuuuucccchhhhh.

    ①Rainbow            <rainbow:*>
    ②Gradient           <gradient:[color_codes]:[color_codes]> or <gradient:[hex codes]:[hex codes]>
    ③NamedTextColor     &f=<white>      &7=<gray>       &8=<dark_gray>      &0=<black>
                        &4=<dark_red>   &c=<red>        &6=<gold>           &e=<yellow>
                        &2=<dark_green> &a=<green>      &3=<dark_aqua>      &b=<aqua>
                        &1=<dark_blue>  &9=<blue>       &5=<dark_purple>    &d=<light_purple>
    ④Hex Color          <#FFFFFF>
    ⑤Pride              <pride> or <pride:*> //illegal
     */
    public boolean PrefixLegalLookUp(String input){
        Pattern     ACTION_CODES_CLICK          =   Pattern.compile("<click:[^>]*>");
        Pattern     PRIDE_CODES                 =   Pattern.compile("<pride(?::[^>]*?)?>");
        Pattern     DECORATION_CODES            =   Pattern.compile("&[km]|<(obfuscated|obf|strikethrough|st)>");
        Matcher     ACTION_CODES_CLICK_MATCHER  =   ACTION_CODES_CLICK.matcher(input);
        Matcher     PRIDE_CODES_MATCHER         =   PRIDE_CODES.matcher(input);
        Matcher     DECORATION_CODES_MATCHER    =   DECORATION_CODES.matcher(input);

        return !(ACTION_CODES_CLICK_MATCHER.find() ||
                PRIDE_CODES_MATCHER.find() ||
                DECORATION_CODES_MATCHER.find());
    }

    public String PrefixFormatterAdventure(String input) {
        input = input.replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>");
        return input;
    }
    public String PrefixFormatterTrChat(String input){
        input = input.replace("<black>", "&0")
                .replace("<dark_blue>", "&1")
                .replace("<dark_green>", "&2")
                .replace("<dark_aqua>", "&3")
                .replace("<dark_red>", "&4")
                .replace("<dark_purple>", "&5")
                .replace("<gold>", "&6")
                .replace("<gray>", "&7")
                .replace("<dark_gray>", "&8")
                .replace("<blue>", "&9")
                .replace("<green>", "&a")
                .replace("<aqua>", "&b")
                .replace("<red>", "&c")
                .replace("<light_purple>", "&d")
                .replace("<yellow>", "&e")
                .replace("<white>", "&f");
        return input;
    }
}
