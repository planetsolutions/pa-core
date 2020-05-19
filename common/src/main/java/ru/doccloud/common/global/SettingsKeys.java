package ru.doccloud.common.global;

public enum SettingsKeys {
     STORAGE_AREA_KEY("storage_area"),
     CMIS_SETTINGS_KEY("cmis_settings");

     private String settingsKey;

    SettingsKeys(String settingsKey) {
        this.settingsKey = settingsKey;
    }

    public String getSettingsKey() {
        return settingsKey;
    }
}
