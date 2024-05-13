package adris.altoclef.butler;

import adris.altoclef.util.helpers.ConfigHelper;

public class ButlerConfig {

    private static ButlerConfig _instance = new ButlerConfig();
    static {
        ConfigHelper.loadConfig("configs/butler.json", ButlerConfig::new, ButlerConfig.class, newConfig -> _instance = newConfig);
    }
    public static ButlerConfig getInstance() {
        return _instance;
    }

    /**
     * If true, will use blacklist for rejecting users from using your player as a butler
     */
    public boolean useButlerBlacklist = false;
    /**
     * If true, will use whitelist to only accept users from said whitelist.
     */
    public boolean useButlerWhitelist = true;
    public boolean autoStuckFix = true;
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
            "{from} whispers to you: {message}",
            "{from} whispers: {message}",
            "\\[{from} -> {to}\\] {message}"
    };

    public String[][] chatFormats = new String[][]{
            //Команда не найдена.
            //[NetTyan -> я] 1
            {"universal","<{from}> {message}","survival"},
            //"? ? [пвапва] | [аыва] Khushin фвыыв ? 14234".
            {"mc.musteryworld.net","{starterPrefix} [{clan}] | [{rank}] {from} > {message}","survival"},
            {"mc.musteryworld.net","{starterPrefix} | [{rank}] {from} > {message}","survival"},
            {"mc.musteryworld.net","[⚑] {from}: {message}","bedwars"},
            {"mc.musteryworld.net","[{rank}] <{from}> {message}","skywars"},
            {"mc.musteryworld.net","{from} ⋙ {message}","murdermystery"},
            // murder
            // BEDWARS
            //[⚑] NetTyan: аа

            //[Всем] NetTyan:  е
            {"mc.musteryworld.net","[Всем] {from}: {message}","bedwars"},
            //162onmyhead ⋙ ник е*****
            {"mc.musteryworld.net","SPEC: {from} > {message}","murdermystery"},
            {"mc.musteryworld.net","{from} > {message}","murdermystery"},




            // VIME MC MESSAGES TYPES
            //ღ [G] §8[§f§f§lппп_IVANBANAN§8] | ᖧШУТᖨ ~koshmarik9090 Утопленник › Блин блинский
            //(i) bxmew наложил мут на игрока apipka228 по причине: попрошайничество [ПОДРОБНЕЕ]
            //ღ [G] | ᖧИмператорᖨ _twistyyyy  › Какой аыва
            //ღ [G] | ᖧStaffᖨ ~explyko  › :33
            //ღ [G] | ᖧunxyᖨ bexzsm1slzn ✔ 私と緒にいて › Тишее
            //ღ [G] §8[§f§f§lппп_IVANBANAN§8] | ᖧШУТᖨ ~koshmarik9090 Утопленник › Ъхапъхапхъа пвапвап снятый
            //ღ [G] §8[§f§f§oNyak§e§oy§8] | ᖧYouTubeᖨ HDemonH  › ХАХАХ папап ору дима
            //ღ [G] | ᖧModerᖨ bxmew ✔ 私と緒にいて › Довели
            //ღ [L] | ᖧДелюксᖨ Oliver_1445  › Сказал же помоги мне с деньгами
            //ღ [G] | ᖧИгрокᖨ wqhtxly Samurai ›

            {"mc.vimemc.net","{starterPrefix} [{global}] [{clan}] | ᖧ{rank}ᖨ {from}  > {message}","survival"},
            {"mc.vimemc.net","{starterPrefix} [{global}] | ᖧ{rank}ᖨ {from}  > {message}","survival"},
            {"mc.vimemc.net","{starterPrefix} [{global}] [{clan}] | ᖧ{rank}ᖨ {from} {suffix} > {message}","survival"},
            {"mc.vimemc.net","{starterPrefix} [{global}] | ᖧ{rank}ᖨ {from} {suffix} > {message}","survival"},
            // THE PIT
            //[36] NetTyan  › 1
            //ingame //[18 уб.] [КОМАНДЕ/всем] NetTyan ► ээм
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


            {"funnymc.ru","[{rank}] {from}  » {message}","skywars"},
            {"funnymc.ru","({rank}) {from} > {message}","skywars"},

            {"funnymc.ru","{from} » {message}","mudermystery"},

            {"mc.4obabke.ru","{from} whispers to you: {message}","skywars"}
    };

    /**
     * If set to true, will print information about whispers that are parsed and those
     * that have failed parsing.
     *
     * Enable this if you need help setting up the whisper format.
     */
    public boolean whisperFormatDebug = false;

    /**
    * Determines if failure messages should be sent to a non-authorized entity attempting to use butler
    *
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
     * 
     * Disable this if you want to be able to send normal messages and not butler commands.
     */
    public boolean requirePrefixMsg = false;
}
