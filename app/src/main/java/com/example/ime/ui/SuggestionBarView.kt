package com.example.ime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.model.KeyboardColorScheme
import com.example.ime.model.KeyboardSettings

@Composable
fun SuggestionBarView(
    composingWord: String,
    suggestions: List<String>,
    colorScheme: KeyboardColorScheme,
    settings: KeyboardSettings,
    onSelectSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!settings.showSuggestions && composingWord.isEmpty()) return

    val fontScale = settings.fontScalePercent / 100f
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 42.dp)
            .background(colorScheme.suggestionBgColor)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // If actively composing, show primary transformed candidate first
            if (composingWord.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorScheme.accentActionColor.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = colorScheme.accentActionColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = colorScheme.accentActionColor)
                        ) {
                            onSelectSuggestion(composingWord)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("suggestion_composing_item"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "“$composingWord”",
                        color = colorScheme.suggestionTextColor,
                        fontSize = (15 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Word suggestions from dictionary
            suggestions.forEachIndexed { index, suggestion ->
                if (suggestion.equals(composingWord, ignoreCase = true)) return@forEachIndexed

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorScheme.keyBackgroundColor.copy(alpha = 0.7f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = colorScheme.accentActionColor)
                        ) {
                            onSelectSuggestion(suggestion)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("suggestion_item_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = suggestion,
                        color = if (index == 0 && composingWord.isEmpty()) {
                            colorScheme.suggestionTextColor
                        } else {
                            colorScheme.primaryTextColor
                        },
                        fontSize = (14 * fontScale).sp,
                        fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick punctuation if suggestions are empty
            if (suggestions.isEmpty() && composingWord.isEmpty()) {
                listOf(",", ".", "?", "!", ":", ";", "-", "@").forEach { punct ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colorScheme.keyBackgroundColor.copy(alpha = 0.5f))
                            .clickable { onSelectSuggestion(punct) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = punct,
                            color = colorScheme.secondaryTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
