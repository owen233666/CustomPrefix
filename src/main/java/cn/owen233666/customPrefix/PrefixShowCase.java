package cn.owen233666.customPrefix;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrefixShowCase {

    private static final MiniMessage miniMessage = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.color()) // 支持颜色
                    .resolver(StandardTags.decorations()) // 支持样式（粗体、斜体等）
                    .build())
            .build();


    /**
     *  &a&l This is the first Message &#FFFFFF&o This is the second Message <g:#ABCDEF:#FEDCBA>this is the gradient color Message
     */



    /**
     * 解析并发送消息给玩家
     *
     * @param player 玩家对象
     * @param input  输入的字符串
     */
    public static void sendParsedMessage(Player player, String input) {
        // 将输入字符串转换为 MiniMessage 格式
        String miniMessageFormat = "称号已更改为："+convertToMiniMessageFormat(input);
        // 解析并发送消息
        Component component = miniMessage.deserialize(miniMessageFormat);
        player.sendMessage(component);
    }
    public static void sendPresentPrefixMessage(Player player, String input){
        // 将输入字符串转换为 MiniMessage 格式
        String miniMessageFormat = "你当前的称号是："+convertToMiniMessageFormat(input);
        // 解析并发送消息
        Component component = miniMessage.deserialize(miniMessageFormat);
        player.sendMessage(component);
    }

    public static void sendPresentPrefixMessageAdmin(CommandSender sender,Player player, String input){
        // 将输入字符串转换为 MiniMessage 格式
        String miniMessageFormat = "你将"+sender.getName()+"的称号改为了:"+convertToMiniMessageFormat(input);
        // 解析并发送消息
        Component component = miniMessage.deserialize(miniMessageFormat);
        player.sendMessage(component);
    }

    /**
     * 将输入字符串转换为 MiniMessage 格式
     *
     * @param input 输入的字符串
     * @return 转换后的 MiniMessage 格式字符串
     */
    private static String convertToMiniMessageFormat(String input) {
        // 替换十六进制颜色代码
        input = input.replaceAll("&#([A-Fa-f0-9]{6})", "<#$1>");

        // 替换 Minecraft 颜色代码
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
        if(input.contains("&k")){
            input = input.replace("&k", "");
        }
        if (input.contains("&l")) {
            input = input.replace("&l", "");
        }
        if (input.contains("&m")) {
            input = "<strikethrough>" + input + "</strikethrough>";
            input = input.replace("&m", "");
        }
        if (input.contains("&n")) {
            input = "<underlined>" + input + "</underlined>";
            input = input.replace("&n", "");
        }
        if (input.contains("&o")) {
            input = "<italic>" + input + "</italic>";
            input = input.replace("&o", "");
        }
        input = "<#FFFFFF>[</#FFFFFF>" + input + "<#FFFFFF>]";

        //将渐变色代码转换为minimessage形式
        input = input.replace("<g:#", "<gradient:#");
        return input;
    }
}