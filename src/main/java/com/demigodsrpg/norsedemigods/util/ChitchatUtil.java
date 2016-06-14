/*
 * Copyright 2014 Alex Bennett & Alexander Chauncey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.demigodsrpg.norsedemigods.util;

import com.demigodsrpg.chitchat.Chitchat;
import com.demigodsrpg.norsedemigods.Setting;
import com.demigodsrpg.norsedemigods.chitchat.AllianceTag;
import com.demigodsrpg.norsedemigods.chitchat.ServerIdTag;
import org.bukkit.Bukkit;


public class ChitchatUtil {
    private static boolean ENABLED;

    public ChitchatUtil() {
        try {
            ENABLED = Bukkit.getPluginManager().getPlugin("Chitchat") instanceof Chitchat;
        } catch (Exception error) {
            ENABLED = false;
        }
    }

    /**
     * @return WorldGuard is enabled.
     */
    public static boolean chitchatEnabled() {
        return ENABLED;
    }

    public static boolean hook() {
        if (ENABLED) {
            // Chitchat integration
            if (Bukkit.getServer().getPluginManager().isPluginEnabled("Chitchat")) {
                Chitchat.getChatFormat().add(new AllianceTag());
                if (Setting.SERVER_ID_TAG) {
                    Chitchat.getChatFormat().add(new ServerIdTag());
                }
            }
        }
        return ENABLED;
    }
}
