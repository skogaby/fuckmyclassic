package com.fuckmyclassic.userconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class to hold configuration data about the currently
 * connected console, such as console SID, sync path, etc.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class ConsoleConfiguration {

    /** The SID of the currently connected console. */
    private String connectedConsoleSid;
    /** The system type of the currently connected console. */
    private String systemType;
    /** The sync path for the currently connected console. */
    private String systemSyncPath;

    @Autowired
    public ConsoleConfiguration() {

    }

    public String getConnectedConsoleSid() {
        return connectedConsoleSid;
    }

    public ConsoleConfiguration setConnectedConsoleSid(String connectedConsoleSid) {
        this.connectedConsoleSid = connectedConsoleSid;
        return this;
    }

    public String getSystemType() {
        return systemType;
    }

    public ConsoleConfiguration setSystemType(String systemType) {
        this.systemType = systemType;
        return this;
    }

    public String getSystemSyncPath() {
        return systemSyncPath;
    }

    public ConsoleConfiguration setSystemSyncPath(String systemSyncPath) {
        this.systemSyncPath = systemSyncPath;
        return this;
    }
}
