package com.dekitateserver.nekoadvertisement.controller

import com.dekitateserver.nekoadvertisement.NekoAdvertisementPlugin
import com.dekitateserver.nekoadvertisement.data.AccountRepository
import com.dekitateserver.nekoadvertisement.data.AdvertisementRepository
import com.dekitateserver.nekoadvertisement.data.model.AdvertiseFrequency
import com.dekitateserver.nekoadvertisement.data.model.Advertisement
import com.dekitateserver.nekolib.util.sendFooterMessage
import com.dekitateserver.nekolib.util.sendHeaderMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext

class AdvertisementController(
    private val plugin: NekoAdvertisementPlugin
) : CoroutineScope {

    private companion object {
        const val COST_PER_DAY = 100.0

        const val MIN_CONTENT_LENGTH = 10
        const val MAX_CONTENT_LENGTH = 250

        const val CONFIRM_RESET_TICK = 20L * 30

        const val PREFIX = "§7[§d広告設定§7]§r "

        val YMD_HM_FORMAT = SimpleDateFormat("yyyy/MM/dd HH:mm")
    }

    private val adRepository = AdvertisementRepository(plugin)
    private val accountRepository = AccountRepository(plugin)
    private val confirmTaskMap: MutableMap<Player, ConfirmTask> = ConcurrentHashMap()
    private val playerMap: MutableMap<Player, AdvertiseFrequency> = ConcurrentHashMap()

    private val adCacheList: MutableList<Advertisement> = CopyOnWriteArrayList()

    private val controllerJob = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + controllerJob

    init {
        plugin.bukkitEventListener.subscribePlayerPreLoginEvent(accountRepository::createAccountIfNeeded)

        Bukkit.getOnlinePlayers().forEach(this::updatePlayerMap)
        plugin.bukkitEventListener.subscribePlayerJoinEvent {
            launch { updatePlayerMap(it) }
        }

        adCacheList.addAll(adRepository.getAdvertisementList())

        scheduleBroadcastTask(AdvertiseFrequency.HIGH)
        scheduleBroadcastTask(AdvertiseFrequency.MIDDLE)
        scheduleBroadcastTask(AdvertiseFrequency.LOW)
    }

    fun cancelJob() = controllerJob.cancel()

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

        player.sendHeaderMessage("§d ⬤ §n広告登録確認", ChatColor.GOLD)
        player.sendMessage("")
        player.sendMessage(" 内容： $content")
        player.sendMessage(" 日数： ${days}日")
        player.sendMessage(" 料金： §c${plugin.economy.format(cost)}")
        player.sendMessage("")
        player.spigot().sendMessage(buildAlertTextLine("登録する"))
        player.sendFooterMessage(ChatColor.GOLD)

        confirmTaskMap[player] = SetTask(player, content, days, cost)
        scheduleConfirmTaskReset(player)
    }

    fun addAdUnSetConfirmTask(player: Player) {
        launch {
            val ad = adRepository.getAdvertisement(player) ?: let {
                player.sendWarnMessage("広告が登録されていません.")
                return@launch
            }

            player.sendHeaderMessage("§d ⬤ §n広告消去確認", ChatColor.GOLD)
            player.sendMessage("")
            player.sendMessage(" 内容： ${ad.content}")
            player.sendMessage("")
            player.spigot().sendMessage(buildAlertTextLine("消去する"))
            player.sendFooterMessage(ChatColor.GOLD)

            confirmTaskMap[player] = UnSetTask(player, ad)
            scheduleConfirmTaskReset(player)
        }
    }

    fun sendInfo(player: Player) {
        launch {
            val ad = adRepository.getAdvertisement(player)
            val account = accountRepository.getAccount(player)
            if (account == null) {
                player.sendWarnMessage("アカウントの取得に失敗しました.")
                return@launch
            }

            player.sendHeaderMessage("§d ⬤ §n広告情報", ChatColor.GOLD)
            player.sendMessage("")
            player.sendMessage(" 受信頻度: ${account.frequency.displayName}")
            player.sendMessage(" 登録広告: ${ad?.content ?: "§8なし"}")
            if (ad != null) {
                player.sendMessage(" 配信期限: §c${YMD_HM_FORMAT.format(ad.expiredDate)}§7まで")
            }
            player.sendMessage("")
            player.sendFooterMessage(ChatColor.GOLD)
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
            player.sendSuccessMessage("キャンセルしました.")
        } else {
            player.sendWarnMessage("キャンセルが必要な処理はありません.")
        }
    }

    private fun updatePlayerMap(player: Player) {
        playerMap[player] = accountRepository.getAdvertiseFrequency(player)
    }

    private fun buildAlertTextLine(okText: String) = TextComponent(" ").apply {
        TextComponent("§b[$okText]").also {
            it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("クリックして$okText").create())
            it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ad confirm")
            addExtra(it)
        }
        TextComponent("  §c[キャンセル]").also {
            it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("クリックしてキャンセルする").create())
            it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ad cancel")
            addExtra(it)
        }
    }

    private fun scheduleConfirmTaskReset(player: Player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
            confirmTaskMap.remove(player)
        }, CONFIRM_RESET_TICK)
    }

    private fun scheduleBroadcastTask(freq: AdvertiseFrequency) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, BroadcastRunnable(freq), 0L, freq.intervalTick)
    }


    private inner class BroadcastRunnable(
        private val frequency: AdvertiseFrequency
    ) : Runnable {

        private var index = 0

        override fun run(): Unit = adCacheList.run {
            removeIf { it.expiredDate.time <= System.currentTimeMillis() }
            if (isEmpty()) {
                return
            }

            if (size <= index) {
                index = 0
            }

            val content = get(index++).formattedContent
            playerMap.filterValues { it == frequency }
                .forEach { (player, _) -> player.sendMessage(content) }
        }

    }


    private interface ConfirmTask : Runnable

    private inner class SetTask(
        private val player: Player,
        private val content: String,
        private val days: Int,
        private val cost: BigDecimal
    ) : ConfirmTask {
        override fun run() {
            if (adRepository.hasAdvertisement(player)) {
                player.sendWarnMessage("すでに広告が登録されています.")
                player.sendWarnMessage("/ad unset で以前の広告を消去して下さい.")
                return
            }

            if (plugin.economy.withdraw(player.uniqueId, cost) == null) {
                player.sendWarnMessage("所持金が不足しています.")
                return
            }

            val expiredDate = Date(System.currentTimeMillis() + (days.dayToMills()))

            if (adRepository.addAdvertisement(player, content, expiredDate)) {
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
            if (adRepository.deleteAdvertisement(ad)) {
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

}