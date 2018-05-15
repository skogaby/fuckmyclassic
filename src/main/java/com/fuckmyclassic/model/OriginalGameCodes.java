package com.fuckmyclassic.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple class to get the list of original games for each console type.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class OriginalGameCodes {

    /**
     * The list of default games for the NES Classic Mini.
     */
    public static final DefaultGame[] defaultNesGames = new DefaultGame[] {
            new DefaultGame("CLV-P-NAAAE", "Super Mario Bros."),
            new DefaultGame("CLV-P-NAACE", "Super Mario Bros. 3"),
            new DefaultGame("CLV-P-NAADE", "Super Mario Bros. 2"),
            new DefaultGame("CLV-P-NAAEE", "Donkey Kong"),
            new DefaultGame("CLV-P-NAAFE", "Donkey Kong Jr."),
            new DefaultGame("CLV-P-NAAHE", "Excitebike"),
            new DefaultGame("CLV-P-NAANE", "The Legend of Zelda"),
            new DefaultGame("CLV-P-NAAPE", "Kirby's Adventure"),
            new DefaultGame("CLV-P-NAAQE", "Metroid"),
            new DefaultGame("CLV-P-NAARE", "Balloon Fight"),
            new DefaultGame("CLV-P-NAASE", "Zelda II - The Adventure of Link"),
            new DefaultGame("CLV-P-NAATE", "Punch-Out!! Featuring Mr. Dream"),
            new DefaultGame("CLV-P-NAAUE", "Ice Climber"),
            new DefaultGame("CLV-P-NAAVE", "Kid Icarus"),
            new DefaultGame("CLV-P-NAAWE", "Mario Bros."),
            new DefaultGame("CLV-P-NAAXE", "Dr. MARIO"),
            new DefaultGame("CLV-P-NAAZE", "StarTropics"),
            new DefaultGame("CLV-P-NABBE", "MEGA MAN™ 2"),
            new DefaultGame("CLV-P-NABCE", "GHOSTS'N GOBLINS™"),
            new DefaultGame("CLV-P-NABJE", "FINAL FANTASY®"),
            new DefaultGame("CLV-P-NABKE", "BUBBLE BOBBLE" ),
            new DefaultGame("CLV-P-NABME", "PAC-MAN"),
            new DefaultGame("CLV-P-NABNE", "Galaga"),
            new DefaultGame("CLV-P-NABQE", "Castlevania"),
            new DefaultGame("CLV-P-NABRE", "GRADIUS"),
            new DefaultGame("CLV-P-NABVE", "Super C"),
            new DefaultGame("CLV-P-NABXE", "Castlevania II Simon's Quest"),
            new DefaultGame("CLV-P-NACBE", "NINJA GAIDEN"),
            new DefaultGame("CLV-P-NACDE", "TECMO BOWL"),
            new DefaultGame("CLV-P-NACHE", "DOUBLE DRAGON II: The Revenge")
    };

    /**
     * The list of default games for the NES Classic Mini.
     */
    public static final DefaultGame[] defaultFamicomGames = new DefaultGame[] {
            new DefaultGame("CLV-P-HAAAJ", "スーパーマリオブラザーズ"),
            new DefaultGame("CLV-P-HAACJ", "スーパーマリオブラザーズ３"),
            new DefaultGame("CLV-P-HAADJ", "スーパーマリオＵＳＡ"),
            new DefaultGame("CLV-P-HAAEJ", "ドンキーコング"),
            new DefaultGame("CLV-P-HAAHJ", "エキサイトバイク"),
            new DefaultGame("CLV-P-HAAMJ", "マリオオープンゴルフ"),
            new DefaultGame("CLV-P-HAANJ", "ゼルダの伝説"),
            new DefaultGame("CLV-P-HAAPJ", "星のカービィ　夢の泉の物語"),
            new DefaultGame("CLV-P-HAAQJ", "メトロイド"),
            new DefaultGame("CLV-P-HAARJ", "バルーンファイト"),
            new DefaultGame("CLV-P-HAASJ", "リンクの冒険"),
            new DefaultGame("CLV-P-HAAUJ", "アイスクライマー"),
            new DefaultGame("CLV-P-HAAWJ", "マリオブラザーズ"),
            new DefaultGame("CLV-P-HAAXJ", "ドクターマリオ"),
            new DefaultGame("CLV-P-HABBJ", "ロックマン®2 Dr.ワイリーの謎"),
            new DefaultGame("CLV-P-HABCJ", "魔界村®"),
            new DefaultGame("CLV-P-HABLJ", "ファイナルファンタジー®III"),
            new DefaultGame("CLV-P-HABMJ", "パックマン"),
            new DefaultGame("CLV-P-HABNJ", "ギャラガ"),
            new DefaultGame("CLV-P-HABQJ", "悪魔城ドラキュラ"),
            new DefaultGame("CLV-P-HABRJ", "グラディウス"),
            new DefaultGame("CLV-P-HABVJ", "スーパー魂斗羅"),
            new DefaultGame("CLV-P-HACAJ", "イー・アル・カンフー"),
            new DefaultGame("CLV-P-HACBJ", "忍者龍剣伝"),
            new DefaultGame("CLV-P-HACCJ", "ソロモンの鍵"),
            new DefaultGame("CLV-P-HACEJ", "つっぱり大相撲"),
            new DefaultGame("CLV-P-HACHJ", "ダブルドラゴンⅡ The Revenge"),
            new DefaultGame("CLV-P-HACJJ", "ダウンタウン熱血物語"),
            new DefaultGame("CLV-P-HACLJ", "ダウンタウン熱血行進曲 それゆけ大運動会"),
            new DefaultGame("CLV-P-HACPJ", "アトランチスの謎")
    };

    /**
     * The list of default games for the SNES Classic Mini.
     */
    public static final DefaultGame[] defaultSnesGames = new DefaultGame[] {
            new DefaultGame("CLV-P-SAAAE", "Super Mario World"),
            new DefaultGame("CLV-P-SAABE", "F-ZERO"),
            new DefaultGame("CLV-P-SAAEE", "The Legend of Zelda: A Link to the Past"),
            new DefaultGame("CLV-P-SAAFE", "Super Mario Kart"),
            new DefaultGame("CLV-P-SAAHE", "Super Metroid"),
            new DefaultGame("CLV-P-SAAJE", "EarthBound"),
            new DefaultGame("CLV-P-SAAKE", "Kirby's Dream Course"),
            new DefaultGame("CLV-P-SAALE", "Donkey Kong Country"),
            new DefaultGame("CLV-P-SAAQE", "Kirby Super Star"),
            new DefaultGame("CLV-P-SAAXE", "Super Punch-Out!!"),
            new DefaultGame("CLV-P-SABCE", "Mega Man X"),
            new DefaultGame("CLV-P-SABDE", "Super Ghouls'n Ghosts"),
            new DefaultGame("CLV-P-SABHE", "Street Fighter II Turbo: Hyper Fighting"),
            new DefaultGame("CLV-P-SABQE", "Super Mario RPG: Legend of the Seven Stars"),
            new DefaultGame("CLV-P-SABRE", "Secret of Mana"),
            new DefaultGame("CLV-P-SABTE", "Final Fantasy III"),
            new DefaultGame("CLV-P-SACBE", "Super Castlevania IV"),
            new DefaultGame("CLV-P-SACCE", "CONTRA III THE ALIEN WARS"),
            new DefaultGame("CLV-P-SADGE", "Star Fox"),
            new DefaultGame("CLV-P-SADJE", "Yoshi's Island"),
            new DefaultGame("CLV-P-SADKE", "Star Fox 2")
    };

    /**
     * The list of default games for the SFC Classic Mini.
     */
    public static final DefaultGame[] defaultSuperFamicomGames = new DefaultGame[] {
            new DefaultGame("CLV-P-VAAAJ", "スーパーマリオワールド"),
            new DefaultGame("CLV-P-VAABJ", "F-ZERO"),
            new DefaultGame("CLV-P-VAAEJ", "ゼルダの伝説 神々のトライフォース"),
            new DefaultGame("CLV-P-VAAFJ", "スーパーマリオカート"),
            new DefaultGame("CLV-P-VAAGJ", "ファイアーエムブレム 紋章の謎"),
            new DefaultGame("CLV-P-VAAHJ", "スーパーメトロイド"),
            new DefaultGame("CLV-P-VAALJ", "スーパードンキーコング"),
            new DefaultGame("CLV-P-VAAQJ", "星のカービィ スーパーデラックス"),
            new DefaultGame("CLV-P-VABBJ", "スーパーストリートファイターⅡ ザ ニューチャレンジャーズ"),
            new DefaultGame("CLV-P-VABCJ", "ロックマンX"),
            new DefaultGame("CLV-P-VABDJ", "超魔界村"),
            new DefaultGame("CLV-P-VABQJ", "スーパーマリオRPG"),
            new DefaultGame("CLV-P-VABRJ", "聖剣伝説2"),
            new DefaultGame("CLV-P-VABTJ", "ファイナルファンタジーVI"),
            new DefaultGame("CLV-P-VACCJ", "魂斗羅スピリッツ"),
            new DefaultGame("CLV-P-VACDJ", "がんばれゴエモン ゆき姫救出絵巻"),
            new DefaultGame("CLV-P-VADFJ", "スーパーフォーメーションサッカー"),
            new DefaultGame("CLV-P-VADGJ", "スターフォックス"),
            new DefaultGame("CLV-P-VADJJ", "スーパーマリオ ヨッシーアイランド"),
            new DefaultGame("CLV-P-VADKJ", "スターフォックス2"),
            new DefaultGame("CLV-P-VADZJ", "パネルでポン")
    };

    /**
     * A mapping of console types to original games.
     */
    public static final Map<ConsoleType, DefaultGame[]> gameCodeMappings;

    static {
        gameCodeMappings = new HashMap<>();

        gameCodeMappings.put(ConsoleType.NES_USA, defaultNesGames);
        gameCodeMappings.put(ConsoleType.NES_JPN, defaultFamicomGames);
        gameCodeMappings.put(ConsoleType.SNES_USA, defaultSnesGames);
        gameCodeMappings.put(ConsoleType.SNES_EUR, defaultSnesGames);
        gameCodeMappings.put(ConsoleType.SNES_JPN, defaultSuperFamicomGames);
    }

    /**
     * Gets the list of original games for the given ConsoleType.
     * @param consoleType The type of console to get the games for
     * @return The original games for the given console type
     */
    public static DefaultGame[] getGamesForConsoleType(final ConsoleType consoleType) {
        return gameCodeMappings.get(consoleType);
    }
}
