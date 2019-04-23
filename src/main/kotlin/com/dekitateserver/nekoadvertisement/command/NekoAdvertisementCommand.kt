package com.dekitateserver.nekoadvertisement.command

import com.dekitateserver.nekoadvertisement.NekoAdvertisementPlugin
import com.dekitateserver.nekolib.command.BaseCommand
import com.dekitateserver.nekolib.command.CommandArgument
import com.dekitateserver.nekolib.util.sendSuccessMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class NekoAdvertisementCommand(
    private val plugin: NekoAdvertisementPlugin
) : BaseCommand("nekoadvertisement") {

    private companion object {
        val ARGUMENT_LIST = listOf(
            CommandArgument("sync"),
            CommandArgument("reload")
        )
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        when (args.getOrNull(0)) {
            "sync" -> {
                plugin.advertisementController.syncAdvertisementsCache()
                sender.sendSuccessMessage("広告を同期しました.")
            }
            "reload" -> {
                plugin.configuration.reload()
                sender.sendSuccessMessage("設定を更新しました.")
            }
            else -> return false
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        return if (args.size == 1) {
            ARGUMENT_LIST.filter(sender, args[0])
        } else {
            emptyList()
        }
    }
}