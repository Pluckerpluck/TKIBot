package com.pluckerpluck.tkibot.dpsreport;

import java.util.HashMap;
import java.util.Map;

public class DPSReport {
    private String permalink;
    private String error;

    private EVTC evtc;

    private static Map<Integer, String> bossIDs;

    static {
        bossIDs = new HashMap<>();
        bossIDs.put(0x3C4E, "Vale Guardian");
        bossIDs.put(0x3C45, "Gorseval");
        bossIDs.put(0x3C0F, "Sabetha");
        bossIDs.put(0x3EFB, "Slothasor");
        bossIDs.put(0x3EF3, "Mathias");
        bossIDs.put(0x3F6B, "Keep Construct");
        bossIDs.put(0x3F76, "Xera");
        bossIDs.put(0x432A, "Cairn");
        bossIDs.put(0x4314, "Mursaat Overseer");
        bossIDs.put(0x4324, "Samarog");
        bossIDs.put(0x4302, "Deimos");
        bossIDs.put(0x4d37, "Soulless Horror");
        bossIDs.put(0x4bfa, "Dhuum");

        bossIDs.put(0x427d, "MAMA");
        bossIDs.put(0x4284, "Siax");
        bossIDs.put(0x4234, "Ensolyss");

        bossIDs.put(0x44e0, "Skorvald");
        bossIDs.put(0x461d, "Artsariiv");
        bossIDs.put(0x455f, "Arkk");
    }


    private static class EVTC {
        private int bossId;
    }

    public String getPermalink() {
        return permalink;
    }

    public boolean hasError() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    public int getBossId() {
        return evtc.bossId;
    }

    public String getBossName() {
        String name = bossIDs.get(evtc.bossId);
        if (name == null){
            name = String.valueOf(evtc.bossId); 
        }
        return name;
    }
}