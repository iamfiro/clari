package com.iamfiro.clari.feature.project.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.theme.Dimens

@Composable
fun ProjectDetailBanner(
    thumbnail: String?,
    onBannerClick: (() -> Unit)?,
    onShareClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val backStack = LocalNavBackStack.current
    
    Box(modifier = modifier) {
        AsyncImage(
            model = thumbnail,
            contentDescription = "Banner",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .then(
                    if (onBannerClick != null) {
                        Modifier.clickable(onClick = onBannerClick)
                    } else {
                        Modifier
                    }
                ),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.sample_banner),
            error = painterResource(R.drawable.sample_banner)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.ScreenPadding, 40.dp)
        ) {
            IconButton(
                onClick = { backStack.removeLastOrNull() },
                modifier = Modifier.offset(x = (-14).dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_left),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (onShareClick != null) {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            painter = painterResource(R.drawable.share),
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (onDeleteClick != null) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            painter = painterResource(R.drawable.trash),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
