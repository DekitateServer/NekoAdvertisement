package com.dekitateserver.nekoadvertisement.controller

import com.dekitateserver.nekoadvertisement.NekoAdvertisementPlugin
import com.dekitateserver.nekoadvertisement.data.AdvertisementRepository
import com.dekitateserver.nekoadvertisement.data.model.Advertisement
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AdvertisementController(
    private val plugin: NekoAdvertisementPlugin
) {

    private companion object {
        const val COST_PER_DAY = 100.0

        const val MIN_CONTENT_LENGTH = 10
        const val MAX_CONTENT_LENGTH = 250

        const val CONFIRM_RESET_TICK = 20L * 30

        const val PREFIX = "§7[§d広告設定§7]§r "
    }

    private val repository = AdvertisementRepository(plugin)
    private val confirmTaskMap: MutableMap<Player, ConfirmTask> = ConcurrentHashMap()

    fun addAdSetConfirmTask(player: Player, strDays: String, contents: List<String>) {
        val days = strDays.toIntOrNull() ?: let {
            player.sendWarnMessage("$strDays は正しい日数ではありません.")
            return
        }

        if (days !in 1..30) {
            player.sendWarnMessage("日数は 1-30 の範囲で入力して下さい.")
            return
        }

        val cost = BigDecimal.valueOf(days * COST_PER_DAY)

        val content: String = ChatColor.translateAlternateColorCodes('&', contents.joinToString(separator = " "))

        if (content.length < MIN_CONTENT_LENGTH) {
            player.sendWarnMessage("広告の内容が短すぎます.")
            return
        }
        if (content.length > MAX_CONTENT_LENGTH) {
            player.sendWarnMessage("${MAX_CONTENT_LENGTH}文字を超える広告は登録できません.")
            return
        }

        player.sendMessage("§d█████ 広告登録確認 █████")
        player.sendMessage("§7| §r内容： $content")
        player.sendMessage("§7| §r日数： ${days}日")
        player.sendMessage("§7| §r料金： §c${plugin.economy.format(cost)}")
        player.spigot().sendMessage(buildAlertTextLine("登録する"))

        confirmTaskMap[player] = SetTask(player, content, days, cost)
        scheduleConfirmTaskReset(player)
    }

    fun addAdUnSetConfirmTask(player: Player) {
        GlobalScope.launch {
            val ad = repository.getAdvertisement(player) ?: let {
                player.sendWarnMessage("広告が登録されていません.")
                return@launch
            }

            player.sendMessage("§d█████ 広告消去確認 █████")
            player.sendMessage("§7| §r内容： ${ad.content}")
            player.spigot().sendMessage(buildAlertTextLine("消去する"))

            confirmTaskMap[player] = UnSetTask(player, ad)
            scheduleConfirmTaskReset(player)
        }
    }

    fun confirm(player: Player) {
        val task = confirmTaskMap.remove(player) ?: let {
            player.sendWarnMessage("認証が必要な処理はありません.")
            return
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, task)
    }

    fun cancel(player: Player) {
        if (confirmTaskMap.remove(player) != null) {
            player.sendSuccessMessage("登録をキャンセルしました.")
        } else {
            player.sendWarnMessage("キャンセルが必要な処理はありません.")
        }
    }

    private fun buildAlertTextLine(okText: String) = TextComponent("§7| ").apply {
        TextComponent("§b[$okText]").also {
            it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ad confirm")
            addExtra(it)
        }
        TextComponent(" §c[キャンセル]").also {
            it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ad cancel")
            addExtra(it)
        }
    }

    private fun scheduleConfirmTaskReset(player: Player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
            confirmTaskMap.remove(player)
        }, CONFIRM_RESET_TICK)
    }


    private interface ConfirmTask : Runnable

    private inner class SetTask(
        private val player: Player,
        private val content: String,
        private val days: Int,
        private val cost: BigDecimal
    ) : ConfirmTask {
        override fun run() {
            if (repository.hasAdvertisement(player)) {
                player.sendWarnMessage("すでに広告が登録されています.")
                player.sendWarnMessage("/ad unset で以前の広告を消去して下さい.")
                return
            }

            if (plugin.economy.withdraw(player.uniqueId, cost) == null) {
                player.sendWarnMessage("所持金が不足しています.")
                return
            }

            val expiredDate = Date(System.currentTimeMillis() + (days.dayToMills()))

            if (repository.addAdvertisement(player, content, expiredDate)) {
                player.sendSuccessMessage("登録しました.")
            } else {
                plugin.economy.deposit(player.uniqueId, cost)
                player.sendErrorMessage("登録に失敗しました.")
            }
        }
    }

    private inner class UnSetTask(
        private val player: Player,
        private val ad: Advertisement
    ) : ConfirmTask {
        override fun run() {
            if (repository.deleteAdvertisement(ad)) {
                player.sendSuccessMessage("消去しました.")
            } else {
                player.sendErrorMessage("消去に失敗しました.")
            }
        }
    }

    private fun Player.sendSuccessMessage(message: String) = sendMessage("${PREFIX}§a$message")

    private fun Player.sendWarnMessage(message: String) = sendMessage("${PREFIX}§e$message")

    private fun Player.sendErrorMessage(message: String) = sendMessage("${PREFIX}§c$message")

    fun Int.dayToMills(): Long = this * 86400000L

    fun Long.millsToDay(): Double = this / 86400000.0
}