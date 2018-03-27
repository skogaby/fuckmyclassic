package com.fuckmyclassic;

import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.ui.MainWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * Real main class for the program. Shim for main()
 * so that we can wire up Spring Beans.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class MainApplication {

    /**
     * Reference to the main window of the application.
     */
    private final MainWindow mainWindow;

    /**
     * Constructor.
     * @param mainWindow
     */
    @Autowired
    public MainApplication(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * Program entrypoint.
     * @param args
     */
    public void start(String[] args) {
        try {
            // set "native" look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // we tried :shrug:
            System.out.println("[WARN] Can't set the application theme -- falling back to cross-platform default");
        }

        // create the window
        JFrame frame = new JFrame(String.format("%s v%s", SharedConstants.APP_NAME, SharedConstants.APP_VERSION));
        frame.setContentPane(mainWindow.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
