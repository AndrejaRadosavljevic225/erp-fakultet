package com.aradosavljevic.schedule_service.domain.json;

public class ConfigMaper {
    private Integer index;
    private String custom;
    private String original;

    public ConfigMaper(Integer index, String custom, String original) {
        this.index = index;
        this.custom = custom;
        this.original = original;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }


}
