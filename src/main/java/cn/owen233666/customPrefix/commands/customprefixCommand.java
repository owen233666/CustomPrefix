package cn.owen233666.customPrefix.commands;

import cn.owen233666.customPrefix.PrefixUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomPrefixCommand implements TabExecutor, Listener {
    private static final Plugin PLUGIN = Bukkit.getPluginManager().getPlugin("CustomPrefix");
    private final Set<Inventory> CUSTOM_ANVILS = new HashSet<>();
    private ItemStack RESULT_ITEM = new ItemStack(Material.PAPER);
    private Map<UUID, String> PLAYER_INPUT = new HashMap<>();

    public CustomPrefixCommand(){
        resgisterPackerReceiver();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(sender instanceof Player player){
            if(args[0].equalsIgnoreCase("upload")){
                Inventory prefixInv = Bukkit.createInventory(player, InventoryType.ANVIL, Component.text("请输入您的前缀"));
                ItemStack HintItem = new ItemStack(Material.PAPER);
                ItemMeta HintMeta = HintItem.getItemMeta();
                HintMeta.lore(Collections.singletonList(MiniMessage.miniMessage().deserialize("<reset><yellow>在上方的文本框中输入您的称号，点击结果栏中确认更改")));
                PersistentDataContainer container = HintMeta.getPersistentDataContainer();
                container.set(new NamespacedKey(PLUGIN, "is_custom_prefix_anvil"), PersistentDataType.BOOLEAN, true);
                HintItem.setItemMeta(HintMeta);
                prefixInv.setItem(0, HintItem);
                CUSTOM_ANVILS.add(prefixInv);
                player.openInventory(prefixInv);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("upload", "clear", "preview", "help");
        }
        return Collections.emptyList();
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event){
        if(event.getWhoClicked() instanceof Player player){
            Inventory inv = event.getInventory();
            if(CUSTOM_ANVILS.contains(inv)){
                if(event.getRawSlot() == 2){
                    String input = PLAYER_INPUT.get(player.getUniqueId());
                    ItemStack clickedItem = event.getCurrentItem();
                    if(!(clickedItem == null) && clickedItem.hasItemMeta()){
                        PersistentDataContainer container = clickedItem.getItemMeta().getPersistentDataContainer();
                        boolean isLegal = Boolean.TRUE.equals(container.get(new NamespacedKey(PLUGIN, "is_legal_prefix"), PersistentDataType.BOOLEAN));
                        if(isLegal){
                            player.sendMessage(MiniMessage.miniMessage().deserialize("<bold><reset><green>称号修改成功!"));
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0F, 1.0F);
                            PrefixUtils.setPrefix(player,PrefixFormatterTrChat(input));
                            player.closeInventory();
                        }else {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            Inventory inv = event.getInventory();
            if (CUSTOM_ANVILS.remove(inv)) {
                PLAYER_INPUT.remove(player.getUniqueId());
            }
        }
    }

    public void updateResultSlot(Inventory inventory, Component component, boolean isLegal){
        ItemMeta ResultMeta = RESULT_ITEM.getItemMeta();
        PersistentDataContainer container = ResultMeta.getPersistentDataContainer();
        if(component == null){
            container.set(new NamespacedKey(PLUGIN, "is_legal_prefix"), PersistentDataType.BOOLEAN, false);
            ResultMeta.lore(Collections.singletonList(MiniMessage.miniMessage().deserialize("<bold><reset><red>输入前缀不能为空！")));
            ResultMeta.displayName(component);
            RESULT_ITEM.setItemMeta(ResultMeta);
        }else {
            if(isLegal){
                container.set(new NamespacedKey(PLUGIN, "is_legal_prefix"), PersistentDataType.BOOLEAN, true);
                ResultMeta.lore(Collections.singletonList(MiniMessage.miniMessage().deserialize("<bold><reset><green>点击修改您的前缀")));
                ResultMeta.displayName(component);
                RESULT_ITEM.setItemMeta(ResultMeta);
            }else {
                container.set(new NamespacedKey(PLUGIN, "is_legal_prefix"), PersistentDataType.BOOLEAN, false);
                ResultMeta.lore(Collections.singletonList(MiniMessage.miniMessage().deserialize("<bold><reset><red>输入的前缀包含不符合规则的字符，请修改！")));
                ResultMeta.displayName(component);
                RESULT_ITEM.setItemMeta(ResultMeta);
            }
        }
        inventory.setItem(2, RESULT_ITEM);
    }

    public void resgisterPackerReceiver(){
        if(PLUGIN == null) return;
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(PLUGIN, ListenerPriority.NORMAL, PacketType.Play.Client.ITEM_NAME) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if(player == null) return;
                Inventory openInv = player.getOpenInventory().getTopInventory();
                if(openInv != null && CUSTOM_ANVILS.contains(openInv)){
                    String input = "";
                    try {
                        if(event.getPacket().getStrings().size()>0){
                            input = event.getPacket().getStrings().read(0);
                        }
                    }catch (Exception e){
                        PLUGIN.getLogger().info(e.getMessage());
                        return;
                    }

                    PLAYER_INPUT.put(player.getUniqueId(), input);
                    //是否为空
                    if(!input.trim().isEmpty()){
                        if(PrefixLegalLookUp(input)){
                            //不是空且合法，返回正常值
                            updateResultSlot(openInv, MiniMessage.miniMessage().deserialize(PrefixFormatterAdventure(input)), true);
                        }else{
                            //不是空但不合法，返回错误值
                            updateResultSlot(openInv, Component.text(input), false);
                        }
                        //是空，返回输入值错误
                    }else {
                        updateResultSlot(openInv, Component.text(input), false);
                    }
                }
            }
        });
    }

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
        Pattern pattern = Pattern.compile("<#([A-Fa-f0-9]{6})>");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) matcher.appendReplacement(result, "&#" + matcher.group(1));
        matcher.appendTail(result);
        return result.toString();
    }
}
