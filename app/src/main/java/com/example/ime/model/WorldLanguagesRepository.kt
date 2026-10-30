package com.example.ime.model

object WorldLanguagesRepository {

    val allLanguages: List<LanguagePack> = listOf(
        LanguagePack(
            id = "vi_telex",
            name = "Tiếng Việt (Telex)",
            nativeName = "Tiếng Việt",
            flag = "🇻🇳",
            inputMethod = "Telex (s, f, r, x, j, aa, aw, ee, oo, dd)",
            sizeMb = "1.8 MB",
            isDownloaded = true,
            isEnabled = true,
            isDefault = true
        ),
        LanguagePack(
            id = "vi_vni",
            name = "Tiếng Việt (VNI)",
            nativeName = "Tiếng Việt",
            flag = "🇻🇳",
            inputMethod = "VNI (1-5 dấu, 6-9 mũ móc)",
            sizeMb = "1.8 MB",
            isDownloaded = true,
            isEnabled = true,
            isDefault = false
        ),
        LanguagePack(
            id = "en_us",
            name = "English (US)",
            nativeName = "English",
            flag = "🇺🇸",
            inputMethod = "QWERTY Standard",
            sizeMb = "3.2 MB",
            isDownloaded = true,
            isEnabled = true,
            isDefault = false
        ),
        LanguagePack(
            id = "en_uk",
            name = "English (UK)",
            nativeName = "English (UK)",
            flag = "🇬🇧",
            inputMethod = "QWERTY (UK layout)",
            sizeMb = "3.4 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        ),
        LanguagePack(
            id = "fr_fr",
            name = "Français (Pháp)",
            nativeName = "Français",
            flag = "🇫🇷",
            inputMethod = "AZERTY",
            sizeMb = "4.1 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        ),
        LanguagePack(
            id = "es_es",
            name = "Español (Tây Ban Nha)",
            nativeName = "Español",
            flag = "🇪🇸",
            inputMethod = "QWERTY (ñ, á, é, í, ó, ú)",
            sizeMb = "4.5 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        ),
        LanguagePack(
            id = "de_de",
            name = "Deutsch (Đức)",
            nativeName = "Deutsch",
            flag = "🇩🇪",
            inputMethod = "QWERTZ (ä, ö, ü, ß)",
            sizeMb = "4.2 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        ),
        LanguagePack(
            id = "ja_jp",
            name = "日本語 (Nhật Bản)",
            nativeName = "日本語",
            flag = "🇯🇵",
            inputMethod = "Romaji / Kana",
            sizeMb = "8.6 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        ),
        LanguagePack(
            id = "ko_kr",
            name = "한국어 (Hàn Quốc)",
            nativeName = "한국어",
            flag = "🇰🇷",
            inputMethod = "Hangul 2-Set (두벌식)",
            sizeMb = "6.2 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        ),
        LanguagePack(
            id = "zh_cn",
            name = "中文 (Trung Quốc)",
            nativeName = "简体中文",
            flag = "🇨🇳",
            inputMethod = "Pinyin (Bính âm)",
            sizeMb = "12.4 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        ),
        LanguagePack(
            id = "ru_ru",
            name = "Русский (Nga)",
            nativeName = "Русский",
            flag = "🇷🇺",
            inputMethod = "ЙЦУКЕН (Cyrillic)",
            sizeMb = "5.1 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        ),
        LanguagePack(
            id = "ar_sa",
            name = "العربية (Ả Rập)",
            nativeName = "العربية",
            flag = "🇸🇦",
            inputMethod = "Arabic Standard",
            sizeMb = "4.8 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        ),
        LanguagePack(
            id = "pt_br",
            name = "Português (Bồ Đào Nha)",
            nativeName = "Português",
            flag = "🇧🇷",
            inputMethod = "QWERTY (ã, ç, õ)",
            sizeMb = "4.0 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        ),
        LanguagePack(
            id = "it_it",
            name = "Italiano (Ý)",
            nativeName = "Italiano",
            flag = "🇮🇹",
            inputMethod = "QWERTY (è, é, à, ò, ù)",
            sizeMb = "3.9 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        ),
        LanguagePack(
            id = "th_th",
            name = "ไทย (Thái Lan)",
            nativeName = "ภาษาไทย",
            flag = "🇹🇭",
            inputMethod = "Kedmanee",
            sizeMb = "5.5 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        ),
        LanguagePack(
            id = "hi_in",
            name = "हिन्दी (Ấn Độ)",
            nativeName = "हिन्दी",
            flag = "🇮🇳",
            inputMethod = "Devanagari Inscript",
            sizeMb = "4.7 MB",
            isDownloaded = false,
            isEnabled = false,
            isDefault = false
        )
    )

    fun getLanguage(id: String): LanguagePack? {
        return allLanguages.find { it.id == id }
    }
}
