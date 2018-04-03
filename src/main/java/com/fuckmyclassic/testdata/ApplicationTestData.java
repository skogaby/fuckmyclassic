package com.fuckmyclassic.testdata;

import com.fuckmyclassic.management.Application;
import com.fuckmyclassic.management.Folder;
import javafx.scene.control.TreeItem;

import java.time.LocalDate;

/**
 * Dumb class to help me test UI ideas until I get a database
 * implementation in place for the custom games.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class ApplicationTestData {

    /**
     * Gets a set of TreeItems filled with test data to show in the main window.
     * @return
     */
    public static TreeItem<Application> getTestApplicationData() {
        final TreeItem<Application> rootItem = new TreeItem<>();
        final Folder homeFolder = new Folder("HOME");
        homeFolder.setApplicationName("HOME");
        homeFolder.setApplicationId("HOME");

        final TreeItem<Application> game1 = new TreeItem<>(new Application("CLV-P-NAAAE", "Super Mario Bros.",
                "/bin/clover-kachikachi-wr /var/squashfs/usr/share/games/nes/kachikachi/CLV-P-NAAAE/CLV-P-NAAAE.nes  --guest-overscan-dimensions 0,0,9,3 --initial-fadein-durations 3,2 --volume 75 --enable-armet",
                "/var/squashfs/usr/share/games/nes/kachikachi/CLV-P-NAAAE/CLV-P-NAAAE.png", "/var/lib/clover/profiles/0//CLV-P-NAAAE",
                452, 0, 2, false, LocalDate.of(1985, 10, 18), 0, "super mario bros.", "NINTENDO", "Super Mario Bros. ©1985 Nintendo", 1001, true));
        final TreeItem<Application>  game2 = new TreeItem<>(new Application("CLV-P-NAACE", "Super Mario Bros. 3",
                "/bin/clover-kachikachi-wr /var/squashfs/usr/share/games/nes/kachikachi/CLV-P-NAACE/CLV-P-NAACE.nes  --guest-overscan-dimensions 0,0,9,3 --initial-fadein-durations 3,2 --volume 75 --enable-armet",
                "/var/squashfs/usr/share/games/nes/kachikachi/CLV-P-NAACE/CLV-P-NAACE.png", "/var/lib/clover/profiles/0//CLV-P-NAACE",
                492, 0, 2, false, LocalDate.of(1990, 02, 12), 0, "super mario bros. 3", "NINTENDO", "Super Mario Bros. 3 ©1988 Nintendo", 1002, false));
        final TreeItem<Application>  game3 = new TreeItem<>(new Application("CLV-P-HABCJ", "魔界村®",
                "/bin/clover-kachikachi-wr /var/squashfs/usr/share/games/nes/kachikachi/CLV-P-HABCJ/CLV-P-HABCJ.nes  --guest-overscan-dimensions 0,0,9,3 --initial-fadein-durations 3,2 --volume 67 --enable-armet",
                "/var/squashfs/usr/share/games/nes/kachikachi/CLV-P-HABCJ/CLV-P-HABCJ.png", "/var/lib/clover/profiles/0//CLV-P-HABCJ",
                21, 0, 2, false, LocalDate.of(1986, 06, 13), 0, "まかいむら", "かぷこん", "魔界村® ©CAPCOM CO., LTD. 1986, 2016 ALL RIGHTS RESERVED.", 1003, true));
        final TreeItem<Application>  game4 = new TreeItem<>(new Application("CLV-P-SAAFE", "Super Mario Kart",
                "/bin/clover-canoe-shvc-wr -rom /var/squashfs/usr/share/games/CLV-P-SAAFE/CLV-P-SAAFE.sfrom --volume 70 -rollback-snapshot-period 600",
                "/var/squashfs/usr/share/games/CLV-P-SAAFE/CLV-P-SAAFE.png", "/var/lib/clover/profiles/0//CLV-P-SAAFE",
                3, 0, 2, true, LocalDate.of(1992, 9, 01), 0, "super mario kart", "NINTENDO", "Super Mario Kart™ ©1992 Nintendo", 1004, false));
        final TreeItem<Application>  game5 = new TreeItem<>(new Application("CLV-P-SABCE", "Mega Man X",
                "/bin/clover-canoe-shvc-wr -rom /var/squashfs/usr/share/games/CLV-P-SABCE/CLV-P-SABCE.sfrom --volume 85 -rollback-snapshot-period 600",
                "/var/squashfs/usr/share/games/CLV-P-SABCE/CLV-P-SABCE.png", "/var/lib/clover/profiles/0//CLV-P-SABCE",
                9, 0, 1, false, LocalDate.of(1993, 12, 20), 0, "mega man x", "CAPCOM", "Mega Man™ X ©CAPCOM CO., LTD. 1993, 2017 ALL RIGHTS RESERVED.", 1005, true));

        rootItem.setValue(homeFolder);
        rootItem.getChildren().addAll(game1, game2, game3, game4, game5);
        rootItem.setExpanded(true);

        return rootItem;
    }
}
