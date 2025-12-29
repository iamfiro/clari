package com.iamfiro.clari.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.theme.Dimens

@Composable
fun HeaderWithBackButton(
    text: String,
    onBackClick: (() -> Unit)? = null
) {
    val backStack = com.iamfiro.clari.core.ui.LocalNavBackStack.current
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.ScreenPadding)) {
        IconButton(
            onClick = { 
                if (onBackClick != null) {
                    onBackClick()
                } else {
                    backStack.removeLastOrNull()
                }
            },
            modifier = Modifier.offset(x = (-14).dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_left),
                contentDescription = "Back",
            )
        }
        Text(
            text,
            style = MaterialTheme.typography.titleLarge,
            lineHeight = 32.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
