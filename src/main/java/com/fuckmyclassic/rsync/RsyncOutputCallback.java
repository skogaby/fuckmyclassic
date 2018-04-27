package com.fuckmyclassic.rsync;

/**
 * Simple callback so the output processor's output can be passed back to the
 * Task that spawned the process.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@FunctionalInterface
public interface RsyncOutputCallback {

    void process(String output);
}
