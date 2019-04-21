package com.dekitateserver.nekoadvertisement.command

import com.dekitateserver.nekoadvertisement.NekoAdvertisementPlugin
import com.dekitateserver.nekolib.command.BaseCommand
import com.dekitateserver.nekolib.command.CommandArgument
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class AdvertisementCommand(plugin: NekoAdvertisementPlugin) : BaseCommand("advertisement") {

    private companion object {
        val ARGUMENT_LIST = listOf(
            CommandArgument("set"),
            CommandArgument("unset"),
            CommandArgument("confirm"),
            CommandArgument("preview"),
            CommandArgument("info"),
            CommandArgument("list"),
            CommandArgument("freq", "[freq]"),
            CommandArgument("help"),
            CommandArgument("sync", permission = "")
        )
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty() || args[0] == "help") {
            sender.sendHelp()
        }

        when (args[0]) {

        }

        return false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        if (args.size == 1) {
            return ARGUMENT_LIST.filter(sender, args[0])
        }

        val commandArg = ARGUMENT_LIST.findOrNull(sender, args[0]) ?: return emptyList()
        val prefix = args.last()

        return when (commandArg.get(args)) {
            "[freq]" -> getPlayerNameList(prefix)
            else -> emptyList()
        }
    }

    private fun CommandSender.sendHelp() {
        sendMessage("---------- 広告コマンドヘルプ ----------")
        sendMessage("| §d/ad set <日数> <内容> §r広告を登録")
        sendMessage("| §d/ad unset §r広告を消去")
        sendMessage("| §d/ad preview <内容> §r広告をプレビュー")
        sendMessage("| §d/ad info §r広告を確認")
        sendMessage("| §d/ad list §r全プレイヤーの広告を確認")
        sendMessage("| §d/ad freq <off|low|middle|high> §r広告の受信頻度を変更")
        sendMessage("| §d/ad help §rヘルプを表示")
        sendMessage("| §7'&'を使用して装飾コードを利用できます.")
        sendMessage("---------------------------------------")
    }
}