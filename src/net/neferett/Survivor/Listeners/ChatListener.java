package net.neferett.Survivor.Listeners;

import net.neferett.Survivor.Utils.Lang;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener
implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setFormat(Lang.get("CHAT_FORMAT").replaceAll("<player>", "%1\\$s").replaceAll("<message>", "%2\\$s"));
    }
}

