package com.iamfiro.clari.feature.project.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.ui.component.SectionTitle
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.project.model.Word

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectDetailWords(
    words: List<Word>,
    onWordLongPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier.padding(
                start = Dimens.ScreenPadding,
                end = Dimens.ScreenPadding,
                top = 24.dp
            )
        ) {
            SectionTitle("단어")
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (words.isEmpty()) {
            Text(
                "단어가 없습니다",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(
                    start = Dimens.ScreenPadding,
                    end = Dimens.ScreenPadding,
                    bottom = 0.dp
                )
            ) {
                words.forEach { word ->
                    WordCard(
                        word = word,
                        modifier = Modifier
                            .width(160.dp)
                            .pointerInput(word.name) {
                                detectTapGestures(
                                    onLongPress = {
                                        onWordLongPress(word.name)
                                    }
                                )
                            }
                    )
                }
            }
        }
    }
}
