package cn.owen233666.customPrefix;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.entity.Player;

public class PrefixUtils {

    private static LuckPerms getLuckPerms() {
        return LuckPermsProvider.get();
    }

    public static void setPrefix(Player player, String prefix) {
        LuckPerms luckPerms = getLuckPerms();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        if (user != null) {
            // 移除所有现有的前缀
            user.data().clear(NodeType.PREFIX::matches);

            // 创建一个优先级为100的前缀节点
            PrefixNode prefixNode = PrefixNode.builder(prefix, 100).build();

            // 将前缀节点添加到用户
            user.data().add(prefixNode);

            // 保存更改
            luckPerms.getUserManager().saveUser(user);
        }
    }

    public static void removePrefix(Player player) {
        LuckPerms luckPerms = getLuckPerms();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        if (user != null) {
            // 移除所有优先级为100的前缀节点
            user.data().clear(node -> {
                if (node instanceof PrefixNode) {
                    PrefixNode prefixNode = (PrefixNode) node;
                    return prefixNode.getPriority() == 100; // 只移除优先级为100的前缀
                }
                return false;
            });

            // 保存更改
            luckPerms.getUserManager().saveUser(user);
        }
    }

    //获取玩家当前prefix
    public static String getPrefix(Player player) {

        LuckPerms luckPerms = getLuckPerms();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        // 查找前缀节点
        return user.getCachedData().getMetaData().getPrefix();
    }

}