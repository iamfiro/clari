package com.iamfiro.clari.feature.wordPack.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PackTabChips(
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf(0) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PrimaryFillChip(
            text = "단어 팩",
            selected = selectedIndex == 0,
            onClick = { selectedIndex = 0 },
        )
        PrimaryFillChip(
            text = "다운로드한 항목",
            selected = selectedIndex == 1,
            onClick = { selectedIndex = 1 },
        )
    }
}

@Composable
fun PrimaryFillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                color = fg,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = MaterialTheme.typography.labelLarge.fontSize * 1.12f
                )
            )
        },
        leadingIcon = null,
        shape = RoundedCornerShape(999.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = bg,
            selectedContainerColor = bg
        ),
        modifier = modifier.height(38.dp)
        // ✅ border 파라미터 자체를 안 넣음 (outline 없음)
    )
}