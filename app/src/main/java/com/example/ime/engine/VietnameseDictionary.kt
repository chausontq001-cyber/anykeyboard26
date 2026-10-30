package com.example.ime.engine

import com.example.ime.model.Shortcut

object VietnameseDictionary {

    // Default fast shortcuts
    val defaultShortcuts = listOf(
        Shortcut("dc", "được"),
        Shortcut("ko", "không"),
        Shortcut("k", "không"),
        Shortcut("ng", "người"),
        Shortcut("vs", "với"),
        Shortcut("vn", "Việt Nam"),
        Shortcut("hn", "Hà Nội"),
        Shortcut("hcm", "Hồ Chí Minh"),
        Shortcut("bt", "bình thường"),
        Shortcut("oki", "OK"),
        Shortcut("ms", "mình"),
        Shortcut("cg", "cũng"),
        Shortcut("nt", "nhắn tin"),
        Shortcut("tn", "thế nào"),
        Shortcut("đc", "được")
    )

    // Contextual next-word bigram predictions
    private val NEXT_WORD_MAP: Map<String, List<String>> = mapOf(
        "xin" to listOf("chào", "lỗi", "phép", "cảm ơn", "hỏi"),
        "chào" to listOf("bạn", "anh", "chị", "em", "mọi người", "cả nhà"),
        "cảm" to listOf("ơn", "thấy", "xúc", "nhận", "giác"),
        "chúc" to listOf("mừng", "bạn", "ngủ ngon", "may mắn", "năm mới", "thành công"),
        "hôm" to listOf("nay", "qua", "kia"),
        "ngày" to listOf("mai", "nay", "hôm nay", "mới", "nghỉ"),
        "việt" to listOf("nam", "tiếng", "người", "kiều"),
        "tiếng" to listOf("việt", "anh", "nói", "cười", "hát"),
        "bạn" to listOf("có", "khỏe", "đang", "làm gì", "ơi"),
        "tôi" to listOf("là", "nghĩ", "muốn", "thấy", "đồng ý"),
        "em" to listOf("chào", "muốn", "biết", "yêu", "ở đâu"),
        "anh" to listOf("chào", "ơi", "có", "biết", "yêu"),
        "đi" to listOf("đâu", "chơi", "về", "ngủ", "ăn"),
        "ăn" to listOf("cơm", "gì", "uống", "sáng", "trưa", "tối"),
        "làm" to listOf("việc", "sao", "gì", "ơn", "quen"),
        "thời" to listOf("gian", "tiết", "sự", "khóa biểu"),
        "công" to listOf("việc", "ty", "nghệ", "nhân", "cụ"),
        "học" to listOf("tập", "sinh", "hành", "tiếng", "bài"),
        "gặp" to listOf("nhau", "lại", "mặt", "gỡ"),
        "gửi" to listOf("tin nhắn", "cho", "lời chào", "email")
    )

    // Common words list sorted roughly by frequency
    private val COMMON_WORDS = listOf(
        "và", "là", "của", "có", "trong", "được", "cho", "người", "với", "không",
        "một", "các", "đã", "nhiều", "những", "này", "để", "khi", "tại", "về",
        "thì", "ra", "như", "đến", "sẽ", "ở", "từ", "lại", "theo", "sau",
        "bằng", "lên", "vào", "làm", "phải", "đang", "cũng", "đó", "nói", "hơn",
        "việc", "rất", "biết", "trên", "qua", "năm", "ngày", "nhưng", "nếu", "hay",
        "chỉ", "thấy", "vì", "thế", "còn", "cùng", "họ", "tôi", "anh", "chị",
        "em", "bạn", "mình", "ông", "bà", "con", "cháu", "gia đình", "bố", "mẹ",
        "xin", "chào", "cảm ơn", "chúc", "tốt", "đẹp", "vui", "buồn", "yêu", "thương",
        "hôm nay", "ngày mai", "hôm qua", "tuần", "tháng", "năm", "giờ", "phút", "giây",
        "Việt Nam", "tiếng Việt", "Hà Nội", "Sài Gòn", "Đà Nẵng", "Huế", "Cần Thơ",
        "thành phố", "đất nước", "quê hương", "xã hội", "kinh tế", "văn hóa", "giáo dục",
        "công nghệ", "thông tin", "điện thoại", "máy tính", "bàn phím", "tin nhắn", "cuộc gọi",
        "lập trình", "ứng dụng", "phần mềm", "thiết kế", "giao diện", "màu sắc", "kích thước",
        "cài đặt", "tùy chọn", "hoàn thành", "thành công", "tiếp tục", "quay lại", "đồng ý",
        "hủy", "xóa", "sửa", "lưu", "tìm kiếm", "chia sẻ", "sao chép", "dán",
        "trường học", "bệnh viện", "công ty", "văn phòng", "nhà ở", "phòng khách", "phòng ngủ",
        "mua sắm", "thanh toán", "tiền mặt", "chuyển khoản", "giá cả", "sản phẩm", "chất lượng",
        "nhanh", "chậm", "dễ", "khó", "sáng", "tối", "cao", "thấp", "dài", "ngắn",
        "to", "nhỏ", "mới", "cũ", "nóng", "lạnh", "ấm áp", "mát mẻ", "mưa", "nắng",
        "thời tiết", "du lịch", "khách sạn", "nhà hàng", "quán cà phê", "trà sữa", "nước mía",
        "bánh mì", "phở", "bún bò", "cơm tấm", "gỏi cuốn", "chả giò", "nem rán",
        "thông minh", "chính xác", "thuận tiện", "tiện lợi", "linh hoạt", "hiện đại",
        "tự động", "khoảng cách", "thanh điều hướng", "con trỏ", "gợi ý", "chữ cái",
        "nguyên âm", "phụ âm", "dấu thanh", "dấu mũ", "dấu móc", "dấu sắc", "dấu huyền",
        "dấu hỏi", "dấu ngã", "dấu nặng", "quy tắc", "kiểu gõ", "chủ đề", "giao diện"
    )

    fun getSuggestions(
        currentWord: String,
        previousWord: String?,
        shortcuts: List<Shortcut> = defaultShortcuts,
        limit: Int = 4
    ): List<String> {
        val results = mutableListOf<String>()

        val trimmed = currentWord.trim()
        val lower = trimmed.lowercase()

        // 1. Check user shortcuts first
        if (lower.isNotEmpty()) {
            val shortcutMatch = shortcuts.find { it.trigger.equals(lower, ignoreCase = true) }
            if (shortcutMatch != null) {
                val replacement = if (trimmed.firstOrNull()?.isUpperCase() == true) {
                    shortcutMatch.replacement.replaceFirstChar { it.uppercase() }
                } else {
                    shortcutMatch.replacement
                }
                results.add(replacement)
            }
        }

        // 2. If user is currently typing a word (prefix matching)
        if (lower.isNotEmpty()) {
            val matchedWords = COMMON_WORDS.filter {
                it.lowercase().startsWith(lower) && !it.equals(lower, ignoreCase = true)
            }.take(limit)

            matchedWords.forEach { word ->
                if (!results.contains(word)) {
                    val formatted = if (trimmed.firstOrNull()?.isUpperCase() == true) {
                        word.replaceFirstChar { it.uppercase() }
                    } else {
                        word
                    }
                    results.add(formatted)
                }
            }

            // Also check if current word is a complete word with next-word followers
            val nextList = NEXT_WORD_MAP[lower]
            if (nextList != null) {
                for (next in nextList) {
                    if (!results.contains(next)) {
                        results.add(next)
                        if (results.size >= limit) break
                    }
                }
            }
        }

        // 3. If previousWord is available -> Predict next words
        if (results.size < limit && !previousWord.isNullOrBlank()) {
            val prevLower = previousWord.trim().lowercase()
            val nextList = NEXT_WORD_MAP[prevLower]
            if (nextList != null) {
                for (next in nextList) {
                    if (!results.contains(next)) {
                        results.add(next)
                        if (results.size >= limit) break
                    }
                }
            }
        }

        // 4. Fill with popular suggestions if still not enough
        if (results.size < limit && lower.isEmpty()) {
            val defaults = listOf("xin chào", "cảm ơn", "chúc bạn", "Việt Nam")
            for (d in defaults) {
                if (!results.contains(d)) {
                    results.add(d)
                    if (results.size >= limit) break
                }
            }
        }

        return results.take(limit)
    }
}
