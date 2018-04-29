package com.fuckmyclassic.rsync;

/**
 * Callback for when rsync succeeds.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@FunctionalInterface
public interface RsyncCompletionCallback {

    void call();
}
