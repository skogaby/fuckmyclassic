package com.fuckmyclassic;

import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.ui.MainWindow;

import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * Main driver class for the program.
 */
public class Main {

    /**
     * Program entrypoint.
     * @param args
     */
    public static void main(String[] args) {
        try {
            // set "native" look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // we tried :shrug:
            System.out.println("[WARN] Can't set the application theme -- falling back to cross-platform default");
        }

        // create the window
        JFrame frame = new JFrame(String.format("%s v%s", SharedConstants.APP_NAME, SharedConstants.APP_VERSION));
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
