package com.dekitateserver.nekoadvertisement.command

import com.dekitateserver.nekoadvertisement.NekoAdvertisementPlugin
import com.dekitateserver.nekoadvertisement.data.model.AdvertiseFrequency
import com.dekitateserver.nekolib.command.BaseCommand
import com.dekitateserver.nekolib.command.CommandArgument
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class AdvertisementCommand(plugin: NekoAdvertisementPlugin) : BaseCommand("advertisement") {

    private companion object {
        val ARGUMENT_LIST = listOf(
            CommandArgument("set"),
            CommandArgument("unset"),
            CommandArgument("info"),
            CommandArgument("list"),
            CommandArgument("freq", "[freq]"),
            CommandArgument("help")
        )
    }

    private val controller = plugin.advertisementController

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isHelpCommand()) {
            sender.sendHelp()
            return true
        }

        when (args[0]) {
            "set" -> sender.doIfPlayerAndArguments(args, 2) { player ->
                controller.addAdSetConfirmTask(player, args[1], args.drop(2))
            }
            "unset" -> sender.doIfPlayer { controller.addAdUnSetConfirmTask(it) }
            "info" -> sender.doIfPlayer { controller.sendInfo(it) }
            "list" -> controller.sendList(sender, args.getOrNull(1))
            "freq" -> sender.doIfPlayerAndArguments(args, 1) { controller.updateAdvertiseFrequency(it, args[1]) }
            "confirm" -> sender.doIfPlayer { controller.confirm(it) }
            "cancel" -> sender.doIfPlayer { controller.cancel(it) }
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        if (args.size == 1) {
            return ARGUMENT_LIST.filter(sender, args[0])
        }

        val commandArg = ARGUMENT_LIST.findOrNull(sender, args[0]) ?: return emptyList()
        val prefix = args.last()

        return when (commandArg.get(args)) {
            "[freq]" -> AdvertiseFrequency.values()
                .filter { it.name.startsWith(prefix, ignoreCase = true) }
                .map { it.name.toLowerCase() }
            else -> emptyList()
        }
    }

    private fun CommandSender.sendHelp() {
        sendMessage("---------- 広告コマンドヘルプ ----------")
        sendMessage("| §d/ad set <日数> <内容> §r広告を登録")
        sendMessage("| §d/ad unset §r広告を消去")
        sendMessage("| §d/ad info §r広告を確認")
        sendMessage("| §d/ad list §r全プレイヤーの広告を確認")
        sendMessage("| §d/ad freq <off|low|middle|high> §r広告の受信頻度を変更")
        sendMessage("| §d/ad help §rヘルプを表示")
        sendMessage("| §7'&'を使用して装飾コードを利用できます.")
        sendMessage("---------------------------------------")
    }
}