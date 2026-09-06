package com.fizzycoyote.qusetroll.feature_character.view_model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterClassAssignmentEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesEntity;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for the Character List screen.
 * <p>
 * This class fetches all user-created characters from the database and maps them
 * into {@link CharacterDisplay} objects, which contain summarized information
 * (name, race, primary class, total level) suitable for display in a list.
 * </p>
 */
public class CharacterListViewModel extends AndroidViewModel {

    private final PlayerCharacterDatabase pcDb;
    private final Open5eDatabase open5eDb;
    private final MutableLiveData<List<CharacterDisplay>> displays = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CharacterListViewModel(Application app) {
        super(app);
        pcDb = PlayerCharacterDatabase.getInstance(app);
        open5eDb = Open5eDatabase.getInstance(app);
        loadCharacters();
    }

    /**
     * Observes the list of characters from the database and processes them on a background
     * thread to include information from the Open5e compendium (like species and class names).
     */
    private void loadCharacters() {
        pcDb.characterDao().getAllCharacters().observeForever(characters -> {
            executor.execute(() -> {
                List<CharacterDisplay> list = new ArrayList<>();
                for (CharacterEntity c : characters) {
                    CharacterDisplay d = new CharacterDisplay();
                    d.id = c.id;
                    d.name = c.name;
                    d.gameSystem = c.gameSystem;
                    d.thumbnailPath = c.thumbnailPath; // may be null – adapter will show default icon

                    // Race
                    if (c.speciesKey != null) {
                        SpeciesEntity species = open5eDb.speciesDao().getByKeySync(c.speciesKey);
                        d.raceName = species != null ? species.name : "?";
                    } else {
                        d.raceName = "?";
                    }

                    // Classes and levels
                    List<CharacterClassAssignmentEntity> assignments = pcDb.classAssignmentDao().getByCharacterId(c.id);
                    if (!assignments.isEmpty()) {
                        int totalLevel = assignments.stream().mapToInt(a -> a.level).sum();
                        CharacterClassAssignmentEntity first = assignments.get(0);
                        CharacterClassEntity cls = open5eDb.characterClassDao().getClassByKeySync(first.classKey);
                        String className = cls != null ? cls.name : "?";
                        d.className = className + " " + totalLevel;
                    } else {
                        d.className = "?";
                    }

                    list.add(d);
                }
                displays.postValue(list);
            });
        });
    }

    /**
     * Returns the LiveData containing the processed list of characters for display.
     *
     * @return LiveData list of {@link CharacterDisplay}.
     */
    public LiveData<List<CharacterDisplay>> getDisplays() {
        return displays;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
