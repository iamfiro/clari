import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamfiro.clari.R

val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_extrabold, FontWeight.ExtraBold),
)

private fun TextStyle.withPretendard() = copy(fontFamily = Pretendard)

val Typographys = Typography().run {
    Typography(
        displayLarge = displayLarge.withPretendard(),
        displayMedium = displayMedium.withPretendard(),
        displaySmall = displaySmall.withPretendard(),

        headlineLarge = headlineLarge.withPretendard().copy(fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.withPretendard().copy(fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.withPretendard(),

        titleLarge = titleLarge.withPretendard(),
        titleMedium = titleMedium.withPretendard(),
        titleSmall = titleSmall.withPretendard(),

        bodyLarge = bodyLarge.withPretendard().copy(fontSize = 17.sp),
        bodyMedium = bodyMedium.withPretendard(),
        bodySmall = bodySmall.withPretendard(),

        labelLarge = labelLarge.withPretendard(),
        labelMedium = labelMedium.withPretendard(),
        labelSmall = labelSmall.withPretendard(),
    )
}