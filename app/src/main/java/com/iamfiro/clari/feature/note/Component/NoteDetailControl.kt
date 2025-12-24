package com.iamfiro.clari.feature.note.Component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamfiro.clari.R

@Composable
fun NoteDetailControl() {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.back_skip_5sec),
            "back skip 5sec",
            modifier = Modifier.size(30.dp).alpha(.5f)
        )
        Icon(
            painter = painterResource(R.drawable.pause),
            "play stop",
            modifier = Modifier.size(40.dp)
        )
        Icon(
            painter = painterResource(R.drawable.skip_5sec),
            "skip 5sec",
            modifier = Modifier.size(30.dp).alpha(.5f)
        )
    }
}