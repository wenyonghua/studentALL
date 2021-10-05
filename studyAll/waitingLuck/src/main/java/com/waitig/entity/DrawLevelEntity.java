package com.waitig.entity;

public class DrawLevelEntity {
    String id;
    String level;
    String name;
    String description;
    String number;
    String drawNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDrawNumber() {
        return drawNumber;
    }

    public void setDrawNumber(String drawNumber) {
        this.drawNumber = drawNumber;
    }

    @Override
    public String toString() {
        return "DrawLevelEntity{" +
                "id='" + id + '\'' +
                ", level='" + level + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", number='" + number + '\'' +
                ", drawNumber='" + drawNumber + '\'' +
                '}';
    }
}
