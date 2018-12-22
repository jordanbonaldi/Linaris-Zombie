
package net.neferett.Survivor.Utils;

import net.neferett.Survivor.Survivor;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {
    private Map<String, String> m_lang = new LinkedHashMap<String, String>();
    private File m_file;
    private FileConfiguration m_yaml;
    private static Lang instance = null;

    public static void init() {
        if (instance == null) {
            instance = new Lang();
        }
    }

    public static String get(String key) {
        Lang.init();
        if (!Lang.instance.m_lang.containsKey(key)) {
            return "Key \"" + key + "\" unfound !";
        }
        return ChatColor.translateAlternateColorCodes((char)'&', (String)Lang.instance.m_lang.get(key).replaceAll("<egrave>", "\u00c3\u00a8").replaceAll("<eacute>", "\u00c3\u00a9").replaceAll("<ecirc>", "\u00c3\u00aa").replaceAll("<euml>", "\u00c3\u00ab").replaceAll("<uacute>", "\u00c3\u00b9").replaceAll("<agrave>", "\u00c3\u00a0").replaceAll("<check>", "\u00e2\u0153\u201c").replaceAll("<check_bold>", "\u00e2\u0153\u201d").replaceAll("<cross>", "\u00e2\u0153\u2022").replaceAll("<cross_bold>", "\u00e2\u0153\u2013").replaceAll("<plus>", "\u00e2\u0153\u0161").replaceAll("<star>", "\u00e2\u0153\u00aa"));
    }

    private Lang() {
        try {
            File dirs = new File("plugins/" + Survivor.getInstance().getName());
            dirs.mkdirs();
            this.m_lang.put("SECOND_SINGULAR", "second");
            this.m_lang.put("SECOND_PLURAL", "seconds");
            this.m_lang.put("MINUTE_SINGULAR", "minute");
            this.m_lang.put("MINUTE_PLURAL", "minutes");
            this.m_lang.put("MOTD_LOBBY_WAIT", "Waiting player ...");
            this.m_lang.put("MOTD_LOBBY_FULL", "Lobby full !");
            this.m_lang.put("MOTD_LOBBY_TIME", "<sec>s");
            this.m_lang.put("MOTD_GAME", "Vague <round> !");
            this.m_lang.put("JOIN_MESSAGE", "&e<player> join the game (<players>/<maxplayers>)");
            this.m_lang.put("KICK_RESTART", "&cLa partie est fini !");
            this.m_lang.put("START_GAME", "&6La partie commence, pr\u00e9parez vous !!");
            this.m_lang.put("START_ROUND", "&6Debut de la vague &b<round> &6!");
            this.m_lang.put("END_ROUND", "&6Fin de la vague &c<round> &6!");
            this.m_lang.put("END_GAME_WIN", "&6Vous avez gagnez la partie !");
            this.m_lang.put("END_GAME_LOOSE", "&cVous avez perdu au round <round> !");
            this.m_lang.put("CHAT_FORMAT", "<player> : <message>");
            this.m_lang.put("INV_SPECTATOR_TP", "Teleport to a player");
            this.m_lang.put("ITEM_SPECTATOR_TELEPORT", "&6Teleport to a player");
            this.m_lang.put("INV_CHOOSE_KIT", "Choose kit");
            this.m_lang.put("ITEM_CHOOSE_KIT", "&6Choose a kit");
            this.m_lang.put("ITEM_OPEN_DOOR", "&r<door>");
            this.m_lang.put("KIT_CHOOSE", "&cYou have choose the kit <kit> !");
            this.m_lang.put("KIT_UNPERM", "&cYou cannot choose this kit !");
            this.m_lang.put("OBJECTIVE_LOBBY_NAME", "Survivor");
            this.m_lang.put("OBJECTIVE_LOBBY_PLAYERS", "Players <x>/<max>");
            this.m_lang.put("OBJECTIVE_LOBBY_START", "Start in <sec>s");
            this.m_lang.put("OBJECTIVE_LOBBY_WAIT", "Attente");
            this.m_lang.put("OBJECTIVE_GAME_ROUND_NAME", "&4Vague&c: &f<x>&0/&c<max>");
            this.m_lang.put("OBJECTIVE_GAME_PREPARATION_NAME", "&4Vague&c: &f<x>&0/&c<max> &a- <min>:<sec>");
            this.m_lang.put("OBJECTIVE_GAME_ENTITIES", "&4Zombies: &c<x>");
            this.m_lang.put("OBJECTIVE_GAME_PLAYER", "&a<player> &b<money>&e$");
            this.m_lang.put("PLAYER_HURT", "&e<player> &cest au sol !");
            this.m_lang.put("PLAYER_DEAD", "&e<player> &cest mort !");
            this.m_lang.put("PLAYER_HEALTH", "&e<player> &c\u00e0 r\u00e9anim\u00e9 <hurted> !");
            this.m_lang.put("REANIM_YOU", "&e<player> &avous r\u00e9anime !");
            this.m_lang.put("REANIM_HIM", "&aVous r\u00e9animez &e<player> !");
            this.m_lang.put("DOOR_OPEN", "&a<player> &b\u00e0 ouvert la porte <door>, la prochaine coute <price>");
            this.m_lang.put("NOT_ENOUGH_MONEY", "&cVous avez pas assez d'argent");
            this.m_lang.put("USE_MAIN_SLOT", "&cVous devez remplacer une arme existante");
            this.m_lang.put("BONUS_ALREADY_BUY", "&cVous avez d\u00e9j\u00e0 acheter ce bonus");
            this.m_lang.put("MUNO_FULL", "&cVous avez d\u00e9j\u00e0 les munitions au maximum pour <weapon>");
            this.m_lang.put("WEAPON_MYSTERY", "&2<player> &a\u00e0 gagn\u00e9 un &e<weapon> &adans la boite magique !");
            this.m_lang.put("WEAPON_BUY", "&2<player> &a\u00e0 achet\u00e9 un &e<weapon>");
            this.m_lang.put("WEAPON_UP", "&2<player> &a\u00e0 am\u00e9lior\u00e9 son &e<weapon> &aau niveau &6<level>");
            this.m_lang.put("MUNO_BUY", "&2<player> &a\u00e0 achet\u00e9 des munitions pour &e<weapon>");
            this.m_lang.put("BONUS_BUY", "&2<player> &a\u00e0 achet\u00e9 &e<bonus>");
            this.m_lang.put("NUKE_LAUNCH", "&2<player> &clance la nuke !");
            this.m_lang.put("NUKE_TIME", "&cLa nuke explose dans <sec> <SECOND> !");
            this.m_lang.put("NUKE_EXPLODE", "&cLa nuke explose !");
            this.m_lang.put("MUNO_MAX", "&aMunitions maximum !");
            this.m_lang.put("MORT_INSTANT", "&aLa mort instantan\u00e9e est activ\u00e9 !");
            this.m_lang.put("LAST_ZOMBIE_TP", "&6Les 5 derniers zombies ont \u00e9t\u00e9 t\u00e9l\u00e9porter au centre !");
            this.m_file = new File("plugins/" + Survivor.getInstance().getName() + "/lang.yml");
            if (!this.m_file.exists()) {
                this.m_file.createNewFile();
            }
            this.m_yaml = YamlConfiguration.loadConfiguration((File)this.m_file);
            LinkedHashMap<String, String> tmpLang = new LinkedHashMap<String, String>();
            for (String key : this.m_lang.keySet()) {
                if (this.m_yaml.contains(key)) {
                    tmpLang.put(key, this.m_yaml.getString(key));
                    continue;
                }
                this.m_yaml.set(key, (Object)this.m_lang.get(key));
                tmpLang.put(key, this.m_lang.get(key));
            }
            this.m_lang = tmpLang;
            this.m_yaml.save(this.m_file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

