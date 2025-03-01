package cn.owen233666.customPrefix;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Collection;

public class PrefixUploader {

    public static void upload(Player player, String prefix) {
        // 获取 LuckPerms 服务
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider == null) {
            // 如果 LuckPerms 未找到，直接返回
            return;
        }

        LuckPerms luckPerms = provider.getProvider();

        // 调用设置前缀的方法
        setPrefix(player, luckPerms, prefix);
    }

    private static void setPrefix(Player player, LuckPerms luckPerms, String prefix) {
        // 获取玩家对应的 User 对象
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            // 如果用户不存在，可能是数据未加载
            return;
        }

        // 获取用户的所有节点
        Collection<Node> nodes = user.data().toCollection();

        // 移除所有优先级为100的前缀节点
        for (Node node : nodes) {
            if (node instanceof PrefixNode) {
                PrefixNode prefixNode = (PrefixNode) node;
                if (prefixNode.getPriority() == 100) {
                    user.data().remove(prefixNode);
                }
            }
        }

        // 创建一个新的前缀节点
        Node prefixNode = PrefixNode.builder(prefix, 100).build();

        // 将前缀节点添加到用户的数据中
        user.data().add(prefixNode);

        // 保存更改
        luckPerms.getUserManager().saveUser(user);

        // 通知 LuckPerms 更新玩家的权限
        luckPerms.getUserManager().loadUser(user.getUniqueId());
    }
}