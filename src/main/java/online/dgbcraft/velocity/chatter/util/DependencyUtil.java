package online.dgbcraft.velocity.chatter.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sanluli36li
 */
public class DependencyUtil {
    private final static Map<String, Boolean> LOADED = new HashMap<>();
    public static String PACKET_EVENTS = "PacketEvents";
    public static String VELOCITY_MANAGER = "VelocityManager";
    public static String VIA_VERSION = "ViaVersion";
    private final static Map<String, String> CLASS_NAME = new HashMap<>() {{
        put(VELOCITY_MANAGER, "online.dgbcraft.velocity.manager.api.VelocityManagerAPI");
        put(PACKET_EVENTS, "com.github.retrooper.packetevents.PacketEvents");
        put(VIA_VERSION, "com.viaversion.viaversion.api.Via");
    }};

    public static boolean isDependencyLoaded(String name) {
        if (!LOADED.containsKey(name)) {
            if (!CLASS_NAME.containsKey(name)) {
                return false;
            }
            try {
                Class.forName(CLASS_NAME.get(name));
                LOADED.put(name, true);
            } catch (ClassNotFoundException e) {
                LOADED.put(name, false);
            }
        }

        return LOADED.get(name);
    }
}
