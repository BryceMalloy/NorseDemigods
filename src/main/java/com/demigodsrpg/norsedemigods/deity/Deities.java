package com.demigodsrpg.norsedemigods.deity;

import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.deity.aesir.*;
import com.demigodsrpg.norsedemigods.deity.jotunn.*;

public class Deities {

    // -- TEMPLATE/EXAMPLE -- //

    private static final Template TEMPLATE = new Template(); // EXAMPLE

    // -- AESIR -- //

    public static final Baldr BALDR = new Baldr();
    public static final Bragi BRAGI = new Bragi();
    public static final Dwarf DWARF = new Dwarf();
    public static final Heimdallr HEIMDALLR = new Heimdallr();
    public static final Odin ODIN = new Odin();
    public static final Thor THOR = new Thor();
    public static final Vidar VIDAR = new Vidar();

    // -- JOTUNN -- //

    public static final Dis DIS = new Dis();
    public static final FireGiant FIRE_GIANT = new FireGiant();
    public static final FrostGiant FROST_GIANT = new FrostGiant();
    public static final Hel HEL = new Hel();
    public static final Jord JORD = new Jord();
    public static final Jormungand JORMUNGAND = new Jormungand();
    public static final Thrymr THRYMR = new Thrymr();

    // -- MOB LIST -- //

    private static final Deity[] deityList = new Deity[]{
            BALDR, BRAGI, DWARF, HEIMDALLR, ODIN, THOR, VIDAR, // AESIR
            DIS, FIRE_GIANT, FROST_GIANT, HEL, JORD, JORMUNGAND, THRYMR // JOTUNN
    };

    // -- PRIVATE CONSTRUCTOR -- //

    private Deities() {
    }

    // -- HELPER METHODS -- //

    public static Deity[] values() {
        return deityList;
    }

    public static Deity valueOf(final String name) {
        if (name != null) {
            for (Deity deity : deityList) {
                if (deity.getName().equalsIgnoreCase(name)) {
                    return deity;
                }
            }
        }
        return null;
    }
}
