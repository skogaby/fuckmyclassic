package com.fuckmyclassic.network;

import net.posick.mDNS.Browse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.ResolverListener;
import org.xbill.DNS.Section;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A listener that responds to MDNS advertisements, so we can find the IP addresses
 * of connected NES/SNES Minis on the network and connect to them appropriately.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class MdnsListener {

    /** The service type to query for MDNS packets with */
    private static final String SERVICE_TYPE = "_hakchi._tcp";

    static Logger LOG = LogManager.getLogger(MdnsListener.class.getName());

    /** The set of addresses that are currently being advertised for this service */
    private Set<String> advertisedAddresses;
    /** The Browse object that we use for background polling */
    private final Browse browse;

    @Autowired
    public MdnsListener() throws IOException {
        this.advertisedAddresses = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        this.browse = new Browse(new String[] { SERVICE_TYPE });
    }

    /**
     * Begin listenening for MDNS packets.
     */
    public void beginPolling() {
        this.browse.start(new ResolverListener() {

            @Override
            public void receiveMessage(Object id, Message m) {
                final Record[] records = m.getSectionArray(Section.ADDITIONAL);

                for (Record r : records) {
                    if (r instanceof ARecord) {
                        final String ipAddress = ((ARecord) r).getAddress().getHostAddress();
                        advertisedAddresses.add(ipAddress);

                        LOG.debug(String.format("Detected IP for %s: %s", SERVICE_TYPE, ipAddress));
                    }
                }
            }

            public void handleException(Object id, Exception e) {
                System.err.println("Exception: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        });
    }

    /**
     * Stop listenening for MDNS packets.
     */
    public void endPolling() throws IOException {
        this.browse.close();
    }

    public Set<String> getAdvertisedAddresses() {
        return advertisedAddresses;
    }
}
