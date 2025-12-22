package com.iamfiro.clari.feature.user.Component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R

@Composable
fun GoogleLoginButton() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 15.dp)
            .fillMaxWidth()
    ) {
        Image(
            painter = painterResource(R.drawable.google_logo),
            contentDescription = "Google logo",
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Google로 계속하기", style = MaterialTheme.typography.bodyLarge)
    }
}