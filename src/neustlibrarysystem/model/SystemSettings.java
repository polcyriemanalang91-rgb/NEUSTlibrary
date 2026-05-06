package neustlibrarysystem.model;

import java.time.LocalDateTime;

public class SystemSettings {
    private int settingID;
    private String settingKey;
    private String settingValue;
    private String description;
    private Integer updatedBy; // AdminID
    private LocalDateTime updatedAt;

    // Display field
    private String updatedByName;

    public SystemSettings() {}

    public SystemSettings(String settingKey, String settingValue, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
    }

    public int getSettingID() { return settingID; }
    public void setSettingID(int settingID) { this.settingID = settingID; }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Integer updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedByName() { return updatedByName; }
    public void setUpdatedByName(String updatedByName) { this.updatedByName = updatedByName; }

    // Convenience converters
    public int getValueAsInt() { return Integer.parseInt(settingValue); }
    public double getValueAsDouble() { return Double.parseDouble(settingValue); }

    @Override
    public String toString() { return settingKey + " = " + settingValue; }
}