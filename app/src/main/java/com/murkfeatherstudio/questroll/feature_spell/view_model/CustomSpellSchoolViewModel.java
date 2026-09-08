package com.murkfeatherstudio.questroll.feature_spell.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * ViewModel for managing custom spell schools.
 * <p>
 * This class provides functionality for adding, updating, and deleting homebrew
 * spell schools. It also ensures data consistency by re-associating spells
 * when their school is deleted.
 * </p>
 */
public class CustomSpellSchoolViewModel extends ViewModel {

    private final CustomSpellSchoolDao schoolDao;
    private final CustomSpellDao spellDao;
    private final Executor executor;

    public CustomSpellSchoolViewModel(CustomSpellSchoolDao schoolDao,
                                      CustomSpellDao spellDao,
                                      Executor executor) {
        this.schoolDao = schoolDao;
        this.spellDao = spellDao;
        this.executor = executor;
    }

    /**
     * Retrieves all custom spell schools currently stored in the database.
     *
     * @return LiveData list of {@link CustomSpellSchoolEntity}.
     */
    public LiveData<List<CustomSpellSchoolEntity>> getAllSchools() {
        return schoolDao.getAll();
    }

    /**
     * Adds a new custom spell school to the database.
     *
     * @param name        The name of the school.
     * @param description A brief description of the school's theme.
     */
    public void addSchool(String name, String description) {
        executor.execute(() -> {
            if (schoolDao.countByName(name) > 0) return;
            CustomSpellSchoolEntity school = new CustomSpellSchoolEntity();
            school.name = name;
            school.description = description;
            schoolDao.insert(school);
        });
    }

    /**
     * Updates an existing custom spell school.
     *
     * @param school The {@link CustomSpellSchoolEntity} with updated values.
     */
    public void updateSchool(CustomSpellSchoolEntity school) {
        executor.execute(() -> schoolDao.update(school));
    }

    /**
     * Deletes a custom spell school.
     * <p>
     * Before deletion, any custom spells currently assigned to this school are
     * moved to "No School" to maintain database integrity.
     * </p>
     *
     * @param school The school entity to delete.
     */
    public void deleteSchool(CustomSpellSchoolEntity school) {
        executor.execute(() -> {
            List<CustomSpellEntity> spells = spellDao.getBySchoolNameSync(school.name);
            if (spells != null) {
                for (CustomSpellEntity spell : spells) {
                    spell.schoolName = "No School";
                    spellDao.update(spell);
                }
            }
            schoolDao.delete(school.id);
        });
    }

    /**
     * Factory for creating {@link CustomSpellSchoolViewModel}.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomSpellSchoolDao schoolDao;
        private final CustomSpellDao spellDao;
        private final Executor executor;

        public Factory(CustomSpellSchoolDao schoolDao, CustomSpellDao spellDao,
                       Executor executor) {
            this.schoolDao = schoolDao;
            this.spellDao = spellDao;
            this.executor = executor;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomSpellSchoolViewModel(schoolDao, spellDao, executor);
        }
    }
}
