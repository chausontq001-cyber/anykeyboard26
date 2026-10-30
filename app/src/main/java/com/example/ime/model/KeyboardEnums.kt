package com.example.ime.model

enum class ThemeMode(val displayName: String, val description: String) {
    FOLLOW_SYSTEM("Theo hệ thống", "Tự động đổi sáng hoặc tối theo cài đặt giao diện của Android"),
    MANUAL("Tùy chọn thủ công", "Sử dụng bảng màu chủ đề do bạn chọn")
}

enum class KeyboardDisplayStyle(val displayName: String, val description: String) {
    DOCKED("Gắn đáy chuẩn", "Bàn phím hiển thị ở mép dưới toàn màn hình chuẩn"),
    FLOATING("Bàn phím nổi", "Bàn phím thu nhỏ thành cửa sổ nổi, có thanh kéo để di chuyển bất cứ đâu"),
    SPLIT("Bàn phím tách rời", "Tách làm 2 nửa trái/phải, tối ưu cầm gõ 2 tay trên màn hình ngang hoặc tablet")
}

enum class TypingMethod(val displayName: String, val description: String) {
    TELEX("Telex", "Gõ dấu bằng phím chữ: s (sắc), f (huyền), r (hỏi), x (ngã), j (nặng), aa, aw, ee, oo, ow, uw, dd"),
    VNI("VNI", "Gõ dấu bằng số: 1 (sắc), 2 (huyền), 3 (hỏi), 4 (ngã), 5 (nặng), 6 (mũ), 7 (móc), 8 (ă), 9 (đ)"),
    OFF("Tắt gõ tiếng Việt", "Chỉ gõ tiếng Anh thuần túy không dấu")
}

enum class TonePlacement(val displayName: String, val example: String) {
    MODERN("Kiểu mới (Khuyên dùng)", "hóa, hòa, thúy, khỏe, toán"),
    CLASSIC("Kiểu cũ", "hoá, hoà, thuý, khoẻ, toán")
}

enum class KeyboardAlignment(val displayName: String) {
    CENTER("Căn giữa (Mặc định)"),
    LEFT("Một tay trái"),
    RIGHT("Một tay phải")
}

enum class KeyboardThemeId(val displayName: String) {
    DYNAMIC_M3("Material You (Hệ thống)"),
    DARK_MIDNIGHT("OLED Midnight (Tối)"),
    LIGHT_PURE("Pure White (Sáng)"),
    OCEAN_BLUE("Ocean Deep Blue"),
    CYBER_EMERALD("Cyber Neon Emerald"),
    SUNSET_AMBER("Sunset Bronze"),
    PASTEL_LAVENDER("Pastel Lavender"),
    HIGH_CONTRAST("Tương phản cao (Vàng - Đen)")
}

data class LanguagePack(
    val id: String,
    val name: String,
    val nativeName: String,
    val flag: String,
    val inputMethod: String,
    val sizeMb: String,
    val isDownloaded: Boolean = false,
    val isEnabled: Boolean = false,
    val isDefault: Boolean = false
)

data class Shortcut(
    val trigger: String,
    val replacement: String
)
