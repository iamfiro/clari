package com.iamfiro.clari.screen.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.component.SectionTitle
import com.iamfiro.clari.core.ui.theme.Dimens

@Composable
fun ProjectDetail() {
    Scaffold() { innerPadding ->
        Column(Modifier
            .padding()
            .padding(top = 0.dp)) {
            Box() {
                Image(
                    painter = painterResource(R.drawable.sample_banner),
                    "banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.ScreenPadding, 40.dp)
                ) {
                    Icon(painter = painterResource(R.drawable.arrow_left), "back")
                    Icon(painter = painterResource(R.drawable.trash), "trash", Modifier.size(22.dp))
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(Dimens.ScreenPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Android 사내 프로젝트",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "2025년 12월 25일 생성",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                SectionTitle("외부 연결")
                SectionTitle("단어")
            }
            Button(
                {}, modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("단어 추가")
            }
        }
    }
}
