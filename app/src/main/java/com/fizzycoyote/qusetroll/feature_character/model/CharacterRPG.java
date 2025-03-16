package com.fizzycoyote.qusetroll.feature_character.model;

public class CharacterRPG {
    private int id;
    private String name;
    private String race;
    private String characterClass;
    private int level;
    private int strength;
    private int dexterity;
    private int constitution;
    private int intelligence;
    private int wisdom;
    private int charisma;
    private String gameVersion;

    public CharacterRPG(int id, String name, String race, String characterClass, int level,
                        int strength, int dexterity, int constitution, int intelligence,
                        int wisdom, int charisma, String gameVersion) {
        this.id = id;
        this.name = name;
        this.race = race;
        this.characterClass = characterClass;
        this.level = level;
        this.strength = strength;
        this.dexterity = dexterity;
        this.constitution = constitution;
        this.intelligence = intelligence;
        this.wisdom = wisdom;
        this.charisma = charisma;
        this.gameVersion = gameVersion;
    }

    public CharacterRPG(String name, String race, String characterClass, int level,
                        int strength, int dexterity, int constitution, int intelligence,
                        int wisdom, int charisma, String gameVersion) {
        this.name = name;
        this.race = race;
        this.characterClass = characterClass;
        this.level = level;
        this.strength = strength;
        this.dexterity = dexterity;
        this.constitution = constitution;
        this.intelligence = intelligence;
        this.wisdom = wisdom;
        this.charisma = charisma;
        this.gameVersion = gameVersion;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRace() { return race; }
    public String getCharacterClass() { return characterClass; }
    public int getLevel() { return level; }
    public int getStrength() { return strength; }
    public int getDexterity() { return dexterity; }
    public int getConstitution() { return constitution; }
    public int getIntelligence() { return intelligence; }
    public int getWisdom() { return wisdom; }
    public int getCharisma() { return charisma; }
    public String getGameVersion() { return gameVersion; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setRace(String race) { this.race = race; }
    public void setCharacterClass(String characterClass) { this.characterClass = characterClass; }
    public void setLevel(int level) { this.level = level; }
    public void setStrength(int strength) { this.strength = strength; }
    public void setDexterity(int dexterity) { this.dexterity = dexterity; }
    public void setConstitution(int constitution) { this.constitution = constitution; }
    public void setIntelligence(int intelligence) { this.intelligence = intelligence; }
    public void setWisdom(int wisdom) { this.wisdom = wisdom; }
    public void setCharisma(int charisma) { this.charisma = charisma; }
    public void setGameVersion(String gameVersion) { this.gameVersion = gameVersion; }
}
