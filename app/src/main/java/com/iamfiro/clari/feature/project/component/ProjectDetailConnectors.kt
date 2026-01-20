package com.iamfiro.clari.feature.project.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.ui.component.SectionTitle
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.project.model.ProjectConnector

@Composable
fun ProjectDetailConnectors(
    connectors: List<ProjectConnector>?,
    onAddClick: (() -> Unit)?,
    onEditClick: ((ProjectConnector) -> Unit)?,
    onDeleteClick: ((ProjectConnector) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.padding(horizontal = Dimens.ScreenPadding)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SectionTitle("External Links")
            if (onAddClick != null) {
                OutlinedButton(
                    onClick = onAddClick,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Add", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        connectors?.forEach { connector ->
            ConnectorCard(
                connector = connector,
                onEdit = if (onEditClick != null) {{ onEditClick(connector) }} else null,
                onDelete = if (onDeleteClick != null) {{ onDeleteClick(connector) }} else null
            )
        }
        if (connectors.isNullOrEmpty()) {
            Text(
                "No external links",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
