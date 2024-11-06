package adris.altoclef.butler;

import adris.altoclef.util.helpers.ConfigHelper;

public class ButlerConfig {

    private static ButlerConfig _instance = new ButlerConfig();

    static {
        ConfigHelper.loadConfig("configs/butler.json", ButlerConfig::new, ButlerConfig.class, newConfig -> _instance = newConfig);
    }

    /**
     * If true, will use blacklist for rejecting users from using your player as a butler
     */
    public boolean useButlerBlacklist = false;
    /**
     * If true, will use whitelist to only accept users from said whitelist.
     */
    public boolean useButlerWhitelist = true;
    public boolean autoStuckFix = false;
    public boolean debugChatParseResult = false;
    public boolean autoJoin = true;
    /**
     * Servers have different messaging plugins that change the way messages are displayed.
     * Rather than attempt to implement all of them and introduce a big security risk,
     * you may define custom whisper formats that the butler will watch out for.
     * <p>
     * Within curly brackets are three special parts:
     * <p>
     * {from}: Who the message was sent from
     * {to}: Who the message was sent to, butler will ignore if this is not your username.
     * {message}: The message.
     * <p>
     * <p>
     * WARNING: The butler will only accept non-chat messages as commands, but don't make this too lenient,
     * else you may risk unauthorized control to the bot. Basically, make sure that only whispers can
     * create the following messages.
     */
    public String[] whisperFormats = new String[]{
            "{from} {to} {message}"
    };
    /**
     * If set to true, will print information about whispers that are parsed and those
     * that have failed parsing.
     * <p>
     * Enable this if you need help setting up the whisper format.
     */
    public boolean whisperFormatDebug = false;
    /**
     * Determines if failure messages should be sent to a non-authorized entity attempting to use butler
     * <p>
     * Disable this if you need to stay undercover.
     */
    public boolean sendAuthorizationResponse = true;
    /**
     * The response sent in a failed execution due to non-authorization
     * {from}: the username of the player who triggered the failed authorization response
     */
    public String failedAuthorizationResposne = "Sorry {from} but you are not authorized!";
    /**
     * Use this to choose if the prefix should be required in messages
     * <p>
     * Disable this if you want to be able to send normal messages and not butler commands.
     */
    public boolean requirePrefixMsg = false;

    public String[][] chatFormats = new String[][]{
            {"universal","<{from}> {message}","survival"},
            {"mc.musteryworld.net","{starterPrefix} [{clan}] | [{rank}] {from} > {message}","survival"},
            {"mc.musteryworld.net","{starterPrefix} | [{rank}] {from} > {message}","survival"},
            {"mc.musteryworld.net","[⚑] {from}: {message}","bedwars"},
            {"mc.musteryworld.net","{from}: {message}","skywars"},
            {"mc.musteryworld.net","{rank}: {message}","skywars"},

            {"mc.musteryworld.net","[Всем] {from}: {message}","bedwars"},
            {"mc.musteryworld.net","SPEC: {from} > {message}","murdermystery"},
            {"mc.musteryworld.net","{from} > {message}","murdermystery"},

            {"mc.vimemc.net","{starterPrefix} [{global}] [{clan}] | ᖧ{rank}ᖨ {from}  > {message}","survival"},
            {"mc.vimemc.net","{starterPrefix} [{global}] | ᖧ{rank}ᖨ {from}  > {message}","survival"},
            {"mc.vimemc.net","{starterPrefix} [{global}] [{clan}] | ᖧ{rank}ᖨ {from} {suffix} > {message}","survival"},
            {"mc.vimemc.net","{starterPrefix} [{global}] | ᖧ{rank}ᖨ {from} {suffix} > {message}","survival"},
            {"mc.vimemc.net","[{rank}] [{team}] {from} > {message}","skywars"},
            {"mc.vimemc.net","[{rank}] ᖧ{donate}ᖨ {from} {suffix} > {message}","thepit"},
            {"mc.vimemc.net","[{rank}] ᖧ{donate}ᖨ {from}  > {message}","thepit"},
            {"mc.vimemc.net","[{rank}] {from}  > {message}","thepit"},
            // SKYWARS
            {"mc.vimemc.net","[{rank}] [{team}] {from} ⇨ {message}","skywars"},
            {"mc.vimemc.net","[{rank}] ᖧ{donate}ᖨ {from} {suffix} > {message}","thepit"},
            {"mc.vimemc.net","[{rank}] ᖧ{donate}ᖨ {from}  > {message}","thepit"},
            {"mc.vimemc.net","[{rank}] {from}  > {message}","thepit"},
            //MURDER MYSTERY nick ⇨ msg
            {"mc.vimemc.net","ᖧ{donate}ᖨ {from} {suffix} ⇨ {message}","murdermystery"},
            {"mc.vimemc.net","ᖧ{donate}ᖨ {from} ⇨ {message}","murdermystery"},
            {"mc.vimemc.net","{from} ⇨ {message}","murdermystery"},

            //gamestarting //[18 уб.] NetTyan ► ээм
            {"mc.vimemc.net","[{rank}] {from} > {message}","skywars"},

            //lobby //nick  > msg
            {"mc.vimemc.net","{from}  > {message}","skywars"},

            //funny mc
            {"funnymc.ru","{starterPrefix} {global} ({clan}) [{rank}] {from} ➯ {message}","survival"},
            {"funnymc.ru","{starterPrefix} {global} ({clan}) {rank} {from} ➯ {message}","survival"},
            {"funnymc.ru","{starterPrefix} {global} [{rank}] {from} ➯ {message}","survival"},
            {"funnymc.ru","{starterPrefix} {global} {rank} {from} ➯ {message}","survival"},


            {"funnymc.ru","{global} ({clan}) [{rank}] {from} ➯ {message}","survival"},
            {"funnymc.ru","{global} ({clan}) {rank} {from} ➯ {message}","survival"},
            {"funnymc.ru","{global} [{rank}] {from} ➯ {message}","survival"},
            {"funnymc.ru","{global} {rank} {from} ➯ {message}","survival"},

            {"mlegacy.net","[{rank}] {from}  » {message}","skywars"},
            {"mlegacy.net","({rank}) {from} > {message}","skywars"},
            {"funnymc.ru","[{rank}] {from}  » {message}","skywars"},
            {"funnymc.ru","({rank}) {from} > {message}","skywars"},

            {"funnymc.ru","{from} » {message}","mudermystery"},

            {"mc.4obabke.ru","{from} whispers to you: {message}","skywars"}
    };

    public static ButlerConfig getInstance() {
        return _instance;
    }
}
