package com.fizzycoyote.qusetroll.feature_spell.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;

import java.util.List;
import java.util.concurrent.Executor;

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

    public LiveData<List<CustomSpellSchoolEntity>> getAllSchools() {
        return schoolDao.getAll();
    }

    public void addSchool(String name, String description) {
        executor.execute(() -> {
            if (schoolDao.countByName(name) > 0) return;
            CustomSpellSchoolEntity school = new CustomSpellSchoolEntity();
            school.name = name;
            school.description = description;
            schoolDao.insert(school);
        });
    }

    public void updateSchool(CustomSpellSchoolEntity school) {
        executor.execute(() -> schoolDao.update(school));
    }

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
