# QuestRoll — D&D 5e Character & Campaign Manager

QuestRoll is a native Android application for Dungeons & Dragons 5th Edition players and Dungeon Masters. It combines a full character creation wizard, campaign management tools, an offline-first rules/reference library, and everyday utilities like a dice roller — all backed by the [Open5e API](https://api.open5e.com/).

## 🎥 Highlights

[![Watch Short](https://img.youtube.com/vi/viJoKf4yjuk/hqdefault.jpg)](https://youtube.com/shorts/viJoKf4yjuk)

[![Watch Video](https://img.youtube.com/vi/6JkVR4css44/hqdefault.jpg)](https://youtu.be/6JkVR4css44)


## ✨ Features

### Character Management
- **Guided Character Wizard** — a multi-step flow (identity, race/species, class, background, alignment, attributes, skills, languages, spells, equipment, image, summary, subclass choice) that walks a player through building a legal 5e character.
- **Character Sheet** — live character sheet with tabs for stats, inventory, spells, and traits, backed by an in-app rules engine (`CharacterEngine`) that computes modifiers, saving throws, proficiency bonus, and HP.
- **Level-Up Wizard** — dedicated flow for leveling up, including ability score improvements, new features, and subclass/spell selection.
- **Multiclassing** — validated against attribute prerequisites (`MulticlassRequirement`).
- **Custom Content** — create your own classes, subclasses, species, backgrounds, spells, items, item sets/categories/rarities, creatures, creature types, conditions, damage types, environments, languages, abilities, alignments, and services, all stored locally and merged with official Open5e data via `Combined*` model classes.

### Campaign Tools
- **Campaign Dashboard** — organize characters into campaigns.
- **Per-Campaign Tabs** — character sheet, equipment, spells, dice, notes, and traits, each as its own fragment (`Campaign*Fragment`).
- **Notes** — freeform session/campaign notes tied to a campaign.

### Reference Library
Browsable, searchable, filterable lists and detail screens for the full Open5e catalogue: classes, species, backgrounds, spells & spell schools, items, item sets, item categories, item rarities, weapon properties, creatures & creature types, conditions, damage types, environments, languages, abilities/skills, alignments, rules & rulesets, and services — each with an "official + custom" combined view.

### Utilities
- **Dice Roller** — polyhedral dice (d4–d100) with a manageable dice pool.
- **Global D&D Calculator Drawer** — a slide-out calculator accessible from the main screen, resizable between 50–100% of screen width.
- **PDF Library** — import, list, and view reference PDFs in-app.
- **Offline Cache** — reference data pulled from Open5e is cached locally so the app is usable without a constant connection; a "Refresh from API" action re-syncs on demand.

## 🏗 Architecture

The app follows a **feature-based package structure** with a shared `core` module, roughly:

```
com.murkfeatherstudio.questroll
├── core
│   ├── api            Retrofit/OkHttp client for the Open5e API
│   ├── base            BaseActivity and shared UI plumbing
│   ├── config           App-wide configuration
│   ├── local_database    Room databases and DAOs
│   ├── models          DTOs, Entities, Mappers, Responses (per Open5e domain object)
│   └── repository      Repositories mediating between network, cache, and UI
└── feature_*            One package per feature (ability, alignment, background,
                            campaign, character, class, condition, creature,
                            damage_types, dice, environment, item, language,
                            loading, rule, service, species, spell, tools/pdf)
      ├── model / adapter / ui / view_model
```

- **Pattern:** MVVM — `ViewModel` classes expose `LiveData` to `Activity`/`Fragment` UI, backed by `Repository` classes.
- **Persistence:** four separate Room databases, each with a single responsibility:
    - `Open5eDatabase` — cached, read-only reference data from the Open5e API.
    - `PlayerCharacterDatabase` — player characters and everything attached to them (attributes, inventory, spells, traits, resources, class assignments…).
    - `CampaignDatabase` — campaigns and campaign notes.
    - `UserContentDatabase` — all user-authored custom content (custom classes, spells, items, creatures, etc.).
- **Networking:** Retrofit + OkHttp + Gson against `https://api.open5e.com/v2/` (`Open5eApiClient` / `Open5eApiService`).
- **Mapping:** dedicated `*Mapper` classes convert between API DTOs, Room Entities, and UI-facing domain models, and `Combined*` classes merge official + custom content for list/detail screens.
- **UI:** XML layouts, Material Design components, `RecyclerView` adapters per list screen, and dialogs/bottom sheets for filtering and pickers.

## 🛠 Tech Stack

- **Language:** Java
- **Database:** Room (SQLite)
- **Networking:** Retrofit2, OkHttp, Gson
- **Architecture:** MVVM
- **UI:** XML layouts + Material Components, ViewPager/fragments for tabbed screens
- **Data source:** [Open5e API](https://api.open5e.com/) v2

## 🚀 Getting Started

1. Clone the repository and open it in Android Studio.
2. Let Gradle sync and download dependencies.
3. Run the app on an emulator or device (min/target SDK as configured in `build.gradle`).
4. On first launch, the app fetches and caches reference data (classes, spells, items, etc.) from the Open5e API — an internet connection is required for this initial sync.

No API key is required; the Open5e API is public.

## 📄 License & Data Attribution

The QuestRoll source code in this repository is licensed under the PolyForm Noncommercial License 1.0.0 — you're free to view, use, modify, and share the code for any noncommercial purpose (personal projects, learning, portfolio review, etc.), but commercial use is not permitted without a separate agreement.

Game reference data is pulled from the Open5e API, made available under the Open Game License (OGL). That data has its own licensing terms, independent of the code license above — check Open5e's own licensing notes before redistributing the content itself.