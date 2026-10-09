package com.skillswap.model;

import java.util.Objects;

/**
 * Represents a skill entity (e.g., Java Programming, Guitar, Photoshop).
 */
public class Skill {

    private int skillId;
    private String name;
    private String category;

    public Skill(int skillId, String name, String category) {
        this.skillId = skillId;
        this.name = name;
        this.category = category;
    }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill)) return false;
        Skill other = (Skill) o;
        return skillId == other.skillId || (name != null && name.equalsIgnoreCase(other.name));
    }

    @Override
    public int hashCode() {
        return name != null ? name.toLowerCase().hashCode() : Objects.hash(skillId);
    }
}
