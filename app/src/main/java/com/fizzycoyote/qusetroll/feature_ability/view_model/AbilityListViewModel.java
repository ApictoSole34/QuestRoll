package com.fizzycoyote.qusetroll.feature_ability.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomAbilityDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomAbilityEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomSkillDao;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityDao;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillDao;
import com.fizzycoyote.qusetroll.feature_ability.model.CombinedAbility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class AbilityListViewModel extends ViewModel {

    private final AbilityDao abilityDao;
    private final CustomAbilityDao customAbilityDao;
    private final SkillDao skillDao;
    private final CustomSkillDao customSkillDao;
    private final Executor executor;

    private final MutableLiveData<String> query = new MutableLiveData<>("");

    // Merged list
    public final LiveData<List<CombinedAbility>> abilities;

    public AbilityListViewModel(AbilityDao abilityDao,
                                CustomAbilityDao customAbilityDao,
                                SkillDao skillDao,
                                CustomSkillDao customSkillDao,
                                Executor executor) {
        this.abilityDao       = abilityDao;
        this.customAbilityDao = customAbilityDao;
        this.skillDao         = skillDao;
        this.customSkillDao   = customSkillDao;
        this.executor         = executor;

        LiveData<List<AbilityEntity>>       open5e = abilityDao.getAll();
        LiveData<List<CustomAbilityEntity>> custom = customAbilityDao.getAll();

        abilities = new MediatorLiveData<List<CombinedAbility>>() {{
            List<AbilityEntity>[]       o5 = new List[]{null};
            List<CustomAbilityEntity>[] cu = new List[]{null};

            Observer<Object> merge = ignored -> {
                List<CombinedAbility> merged = new ArrayList<>();
                if (o5[0] != null) for (AbilityEntity e : o5[0]) merged.add(new CombinedAbility(e));
                if (cu[0] != null) for (CustomAbilityEntity e : cu[0]) merged.add(new CombinedAbility(e));

                String q = query.getValue();
                if (q != null && !q.isEmpty()) {
                    String lower = q.toLowerCase();
                    merged = merged.stream()
                            .filter(a -> a.name.toLowerCase().contains(lower))
                            .collect(Collectors.toList());
                }
                setValue(merged);
            };

            addSource(open5e, v -> { o5[0] = v; merge.onChanged(null); });
            addSource(custom, v -> { cu[0] = v; merge.onChanged(null); });
            addSource(query,  v -> merge.onChanged(null));
        }};
    }

    public void setQuery(String q) { query.setValue(q == null ? "" : q); }

    public void deleteCustomAbility(CustomAbilityEntity entity) {
        executor.execute(() -> customAbilityDao.delete(entity));
    }

    // --- Factory ---
    public static class Factory implements ViewModelProvider.Factory {
        private final AbilityDao a; private final CustomAbilityDao ca;
        private final SkillDao s;   private final CustomSkillDao cs;
        private final Executor ex;

        public Factory(AbilityDao a, CustomAbilityDao ca,
                       SkillDao s, CustomSkillDao cs, Executor ex) {
            this.a = a; this.ca = ca; this.s = s; this.cs = cs; this.ex = ex;
        }

        @NonNull
        @Override @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> c) { return (T) new AbilityListViewModel(a,ca,s,cs,ex); }
    }
}