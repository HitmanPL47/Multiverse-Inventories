package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.profile.ProfileTypeManager;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.util.CommentedYamlConfiguration;
import com.onarandombox.multiverseinventories.util.Logging;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DefaultProfileTypeManager extends ProfileTypeManager {

    private CommentedYamlConfiguration profileConfig;

    DefaultProfileTypeManager(File profileFile) {
        this.profileConfig = new CommentedYamlConfiguration(profileFile, false);
        this.profileConfig.load();
        setDefaults();
        loadProfileTypes();
    }

    private void setDefaults() {
        if (!this.profileConfig.getConfig().isSet("profile_types")) {
            ConfigurationSection section = this.profileConfig.getConfig().createSection("profile_types");
            section.createSection(ProfileTypes.DEFAULT.getName())
                    .set("shares", ProfileTypes.DEFAULT.getShares().toStringList());
            section.createSection(ProfileTypes.GAME_MODE.getName())
                    .set("shares", ProfileTypes.GAME_MODE.getShares().toStringList());
        }
        String nl = System.getProperty("line.separator");
        this.profileConfig.getConfig().options()
                .header("Here you may set the shares that are used for different profile types."
                + nl + "Profile types are used for things like separate inventories/stats for creative mode."
                + nl + "The shares set for a profile indicate the data that will be saved for the profile type."
                + nl + ProfileTypes.DEFAULT.getName() + " is the default data, it is recommended to leave this sharing 'all'."
                + nl + ProfileTypes.GAME_MODE.getName() + " indicates what will be used when switching data based on game mode.");
        if (!this.profileConfig.save()) {
            Logging.severe("Unable to save profile types!");
        }
    }

    private void loadProfileTypes() {
        ConfigurationSection section = this.profileConfig.getConfig().getConfigurationSection("profile_types");
        for (String key : section.getKeys(false)) {
            List sharesList = section.getList(key + ".shares");
            if (sharesList != null) {
                ProfileTypes.registerProfileType(key, Sharables.fromList(sharesList));
            }
        }
    }

    public void registerProfileType(String name, Shares shares) {
        ProfileTypes.registerProfileType(name, shares);
        saveProfileTypes();
    }

    public void saveProfileTypes() {
        Collection<ProfileType> profileTypes = ProfileTypes.getProfileTypes();
        Map<String, Object> toSave = new HashMap<String, Object>(profileTypes.size());
        for (ProfileType profileType : profileTypes) {
            Map<String, Object> data = new HashMap<String, Object>(1);
            data.put("shares", profileType.getShares().toStringList());
            toSave.put(profileType.getName(), data);
        }
        this.profileConfig.getConfig().set("profile_types", toSave);
        this.profileConfig.save();
    }
}
