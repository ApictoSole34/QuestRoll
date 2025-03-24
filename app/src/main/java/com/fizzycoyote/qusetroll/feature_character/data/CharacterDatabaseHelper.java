package com.fizzycoyote.qusetroll.feature_character.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fizzycoyote.qusetroll.feature_character.model.CharacterRPG;

import java.util.ArrayList;
import java.util.List;

public class CharacterDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "characters.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "characters";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_RACE = "race";
    private static final String COLUMN_CLASS = "class";
    private static final String COLUMN_LEVEL = "level";
    private static final String COLUMN_STRENGTH = "strength";
    private static final String COLUMN_DEXTERITY = "dexterity";
    private static final String COLUMN_CONSTITUTION = "constitution";
    private static final String COLUMN_INTELLIGENCE = "intelligence";
    private static final String COLUMN_WISDOM = "wisdom";
    private static final String COLUMN_CHARISMA = "charisma";
    private static final String COLUMN_GAME_VERSION = "game_version";
    private static final String COLUMN_CHARACTER_MAIN_IMAGE_PATH = "character_main_image_path";

    public CharacterDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_RACE + " TEXT, " +
                COLUMN_CLASS + " TEXT, " +
                COLUMN_LEVEL + " INTEGER, " +
                COLUMN_STRENGTH + " INTEGER, " +
                COLUMN_DEXTERITY + " INTEGER, " +
                COLUMN_CONSTITUTION + " INTEGER, " +
                COLUMN_INTELLIGENCE + " INTEGER, " +
                COLUMN_WISDOM + " INTEGER, " +
                COLUMN_CHARISMA + " INTEGER, " +
                COLUMN_GAME_VERSION + " TEXT," +
                COLUMN_CHARACTER_MAIN_IMAGE_PATH + " TEXT" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addCharacter(CharacterRPG characterRPG) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, characterRPG.getName());
        values.put(COLUMN_RACE, characterRPG.getRace());
        values.put(COLUMN_CLASS, characterRPG.getCharacterClass());
        values.put(COLUMN_LEVEL, characterRPG.getLevel());
        values.put(COLUMN_STRENGTH, characterRPG.getStrength());
        values.put(COLUMN_DEXTERITY, characterRPG.getDexterity());
        values.put(COLUMN_INTELLIGENCE, characterRPG.getIntelligence());
        values.put(COLUMN_CONSTITUTION, characterRPG.getConstitution());
        values.put(COLUMN_WISDOM, characterRPG.getWisdom());
        values.put(COLUMN_CHARISMA, characterRPG.getCharisma());
        values.put(COLUMN_GAME_VERSION, characterRPG.getGameVersion());
        values.put(COLUMN_CHARACTER_MAIN_IMAGE_PATH, characterRPG.getCharacterMainImagePath());

        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    @SuppressLint("Range")
    public List<CharacterRPG> getAllCharacters() {
        List<CharacterRPG> characters = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor != null) {
            String[] columnNames = cursor.getColumnNames();
            while (cursor.moveToNext()) {
                        CharacterRPG characterRPG = new CharacterRPG(
                        cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_RACE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_CLASS)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_LEVEL)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_STRENGTH)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_DEXTERITY)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_CONSTITUTION)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_INTELLIGENCE)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_WISDOM)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_CHARISMA)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_GAME_VERSION)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_CHARACTER_MAIN_IMAGE_PATH))
                );
                characterRPG.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                characters.add(characterRPG);
            }
            cursor.close();
        }
        db.close();
        return characters;
    }

    @SuppressLint("Range")
    public CharacterRPG getCharacterById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            CharacterRPG characterRPG = new CharacterRPG(
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_RACE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CLASS)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_LEVEL)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_STRENGTH)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_DEXTERITY)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_CONSTITUTION)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_INTELLIGENCE)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_WISDOM)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_CHARISMA)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_GAME_VERSION)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CHARACTER_MAIN_IMAGE_PATH))
            );
            characterRPG.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            cursor.close();
            db.close();
            return characterRPG;
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return null;
    }

    public void updateCharacter(CharacterRPG characterRPG) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, characterRPG.getName());
        values.put(COLUMN_RACE, characterRPG.getRace());
        values.put(COLUMN_CLASS, characterRPG.getCharacterClass());
        values.put(COLUMN_LEVEL, characterRPG.getLevel());
        values.put(COLUMN_STRENGTH, characterRPG.getStrength());
        values.put(COLUMN_DEXTERITY, characterRPG.getDexterity());
        values.put(COLUMN_CONSTITUTION, characterRPG.getConstitution());
        values.put(COLUMN_INTELLIGENCE, characterRPG.getIntelligence());
        values.put(COLUMN_WISDOM, characterRPG.getWisdom());
        values.put(COLUMN_CHARISMA, characterRPG.getCharisma());
        values.put(COLUMN_GAME_VERSION, characterRPG.getGameVersion());
        values.put(COLUMN_CHARACTER_MAIN_IMAGE_PATH, characterRPG.getCharacterMainImagePath());

        db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(characterRPG.getId())});
        db.close();
    }

    public void deleteCharacter (int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
