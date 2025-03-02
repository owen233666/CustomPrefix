package cn.owen233666.customPrefix.Commands;

import cn.owen233666.customPrefix.PrefixShowCase;
import cn.owen233666.customPrefix.PrefixUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class Commands implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CustomPrefix");
        if (cmd.getName().equalsIgnoreCase("customprefix") || cmd.getName().equalsIgnoreCase("prefix")) {
            if (args.length == 0) {
                sender.sendMessage("输入 /prefix help 来获取帮助");
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                // 使用帮助
                if (args[0].equalsIgnoreCase("help")) {
                    MiniMessage miniMessage = MiniMessage.builder().tags(TagResolver.builder().resolver(StandardTags.color()).resolver(StandardTags.decorations()).build()).build();
                    Component component = miniMessage.deserialize(
                            "<#FFFFFF>-----------<#3DFF3D>称号帮助</#3DFF3D>-----------\n" +
                                    "<#FFE521>prefix help</#FFE521> - 查看此帮助。\n" +
                                    "<#FFE521>/prefix present</#FFE521> - 查看当前称号\n" +
                                    "<#FFE521>/prefix upload</#FFE521> <称号内容> - 设置称号\n" +
                                    "关于<bold>称号格式</bold>:\n" +
                                    "1.可以使用原版颜色代码,例如\"<green>&a</green>\"\n" +
                                    "2.可以使用原版格式代码,例如\"<italic>&o</italic>\"\n" +
                                    "3.可以使用6位16进制颜色代码,例如\"<#ABCDEF>&#ABCDEF</#ABCDEF>\"\n" +
                                    "4.可以使用渐变色代码，例如\"<g:#ABCDEF:#ABCDEF>\"\n" +
                                    "5.这是一个例子,假如执行<#FFE521>/prefix upload &#ABCDEF&l&o这是一个测试称号</#FFE521>,那么它看起来应该长这样：\n<#FFFFFF>[</#FFFFFF><#ABCDEF><bold><italic>这是一个测试称号</#ABCDEF><#FFFFFF>]</#FFFFFF>");
                    sender.sendMessage(component);
                }
                // 清除称号
                else if (args[0].equalsIgnoreCase("clear")) {
                    PrefixUtils.removePrefix(player);
                    sender.sendMessage("称号清除成功！");
                }
                // 获取当前称号
                else if (args[0].equalsIgnoreCase("present")) {
                    String currentPrefix = new PrefixUtils().getPrefix(player).replace("&#FFFFFF[", "").replace("&#FFFFFF]", "");
                    if (currentPrefix != null && !currentPrefix.isEmpty()) {
                        PrefixShowCase.sendPresentPrefixMessage((Player) sender, currentPrefix);
                    } else {
                        sender.sendMessage("你当前没有设置前缀。");
                    }
                } else if (args[0].equalsIgnoreCase("upload")) {
                    // 设置前缀的逻辑
                    String prefix = "&#FFFFFF[" + args[1].replace("&k", "").replace("&l", "") + "&#FFFFFF]";
                    String showprefix = args[1];
                    PrefixUtils.setPrefix((Player) sender, prefix);
                    PrefixShowCase.sendParsedMessage((Player) sender, showprefix);
                }

            } else {
                sender.sendMessage("未知指令！请输入/prefix help来查看使用方法");
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("customprefixadmin") || cmd.getName().equalsIgnoreCase("prefixadmin")) {
            if (args[0].equalsIgnoreCase("upload")) {
                Player player = Bukkit.getPlayer(args[1]);
                if (player.isOp()) {
                    if (player != null) {
                        String prefix = "&#FFFFFF[" + args[2] + "&#FFFFFF]";
                        PrefixUtils.setPrefix(player, prefix);
                        PrefixShowCase.sendPresentPrefixMessageAdmin(sender, player, args[2]);
                    } else {
                        sender.sendMessage("玩家不在线！");
                    }

                }else{
                    sender.sendMessage("你不是管理员！");
                }

            }
        } else {
            sender.sendMessage("只有玩家才能执行此命令");
            return false;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("prefix")) {
            completions.add("help");
            completions.add("clear");
            completions.add("&a");
            completions.add("&#FFFFFF");
        }
        // 过滤补全列表，只返回与输入匹配的部分
        if (args.length > 0) {
            String input = args[args.length - 1].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(input));
        }

        return completions;
    }
}