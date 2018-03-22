package com.fuckmyclassic;

import com.fuckmyclassic.shared.SharedConstants;
import com.fuckmyclassic.ui.MainWindow;

import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) {
        JFrame frame = new JFrame(String.format("%s v%s", SharedConstants.APP_NAME, SharedConstants.APP_VERSION));
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
    }
}
