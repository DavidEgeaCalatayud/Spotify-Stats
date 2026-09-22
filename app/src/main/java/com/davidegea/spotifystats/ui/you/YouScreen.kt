package com.davidegea.spotifystats.ui.you

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidegea.spotifystats.BuildConfig
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.ui.components.*
import com.davidegea.spotifystats.ui.settings.AppPreferences

@Composable
fun YouRoute(
    onWrapped: () -> Unit,
    onCalendar: () -> Unit,
    onImport: () -> Unit,
    viewModel: YouViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingRestore by rememberSaveable { mutableStateOf<String?>(null) }
    var theme by remember { mutableStateOf(AppPreferences.theme(context)) }
    var language by remember { mutableStateOf(AppPreferences.language(context)) }

    val export = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        uri?.let { viewModel.export(it.toString()) }
    }
    val restore = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        pendingRestore = uri?.toString()
    }

    YouScreen(
        state = state,
        theme = theme,
        language = language,
        onWrapped = onWrapped,
        onCalendar = onCalendar,
        onImport = onImport,
        onExport = {
            export.launch(
                "MusicStatsBackup_" + isoDate(System.currentTimeMillis()) + ".zip",
            )
        },
        onRestore = {
            restore.launch(
                arrayOf("application/zip", "application/octet-stream"),
            )
        },
        onDelete = viewModel::delete,
        onThemeSelected = { value ->
            theme = value
            AppPreferences.setTheme(context, value)
        },
        onLanguageSelected = { value ->
            language = value
            AppPreferences.setLanguage(context, value)
        },
    )

    pendingRestore?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text(stringResource(R.string.you_restore_title)) },
            text = { Text(stringResource(R.string.you_restore_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingRestore = null
                        viewModel.restore(uri)
                    },
                ) {
                    Text(stringResource(R.string.you_restore_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun YouScreen(
    state: DataControlState,
    theme: String,
    language: String,
    onWrapped: () -> Unit,
    onCalendar: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onThemeSelected: (String) -> Unit,
    onLanguageSelected: (String) -> Unit,
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var privacy by rememberSaveable { mutableStateOf(false) }
    var preference by rememberSaveable { mutableStateOf<String?>(null) }

    StatsPage {
        Text(
            stringResource(R.string.you_title),
            style = MaterialTheme.typography.headlineLarge,
        )

        SectionHeading(stringResource(R.string.you_music_group))
        Card {
            SettingsRow(
                stringResource(R.string.you_generate_wrapped),
                Icons.Default.Star,
                onClick = onWrapped,
            )
            HorizontalDivider()
            SettingsRow(
                stringResource(R.string.you_calendar),
                Icons.Default.DateRange,
                onClick = onCalendar,
            )
        }

        SectionHeading(stringResource(R.string.you_data_group))
        Card {
            SettingsRow(
                stringResource(R.string.import_title),
                Icons.Default.Add,
                onClick = onImport,
            )
            HorizontalDivider()
            SettingsRow(
                stringResource(R.string.you_export_backup),
                Icons.Default.AccountBox,
                enabled = !state.busy,
                onClick = onExport,
            )
            HorizontalDivider()
            SettingsRow(
                stringResource(R.string.you_restore_backup),
                Icons.Default.Refresh,
                enabled = !state.busy,
                onClick = onRestore,
            )
        }
        Text(
            stringResource(R.string.you_backup_restore_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (state.busy) {
            LoadingStateCard(stringResource(R.string.state_loading))
        }
        state.messageRes?.let {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    stringResource(it),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        SectionHeading(stringResource(R.string.you_privacy_group))
        Card {
            SettingsRow(
                stringResource(R.string.you_privacy_title),
                Icons.Default.Lock,
                onClick = { privacy = !privacy },
            )
            AnimatedVisibility(privacy) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(R.string.you_privacy_body_1))
                    Text(stringResource(R.string.you_privacy_body_2))
                    Text(stringResource(R.string.you_privacy_body_3))
                }
            }
        }

        SectionHeading(stringResource(R.string.you_connections_group))
        Card {
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.you_spotify_live_sync))
                },
                supportingContent = {
                    Text(stringResource(R.string.you_spotify_live_sync_body))
                },
                leadingContent = {
                    Icon(Icons.Default.Info, contentDescription = null)
                },
                trailingContent = {
                    SuggestionChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(stringResource(R.string.you_coming_soon)) },
                    )
                },
            )
        }

        SectionHeading(stringResource(R.string.you_app_group))
        Card {
            SettingsRow(
                stringResource(R.string.settings_theme),
                Icons.Default.Settings,
                subtitle = preferenceLabel(theme),
                onClick = { preference = "theme" },
            )
            HorizontalDivider()
            SettingsRow(
                stringResource(R.string.settings_language),
                Icons.Default.Edit,
                subtitle = preferenceLabel(language),
                onClick = { preference = "language" },
            )
            HorizontalDivider()
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(
                            R.string.you_app_version,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE,
                        ),
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Info, contentDescription = null)
                },
            )
        }

        SectionHeading(stringResource(R.string.you_delete_group))
        OutlinedButton(
            enabled = !state.busy,
            onClick = { confirmDelete = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.you_delete_history))
        }
    }

    preference?.let { selected ->
        ModalBottomSheet(
            onDismissRequest = { preference = null },
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(
                        if (selected == "theme") {
                            R.string.settings_theme
                        } else {
                            R.string.settings_language
                        },
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                val values = if (selected == "theme") {
                    listOf("system", "light", "dark")
                } else {
                    listOf("system", "en", "es")
                }
                values.forEach { value ->
                    val isSelected = value == if (selected == "theme") theme else language
                    ListItem(
                        headlineContent = { Text(preferenceLabel(value)) },
                        leadingContent = {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                            )
                        },
                        modifier = Modifier.clickable {
                            if (selected == "theme") {
                                onThemeSelected(value)
                            } else {
                                onLanguageSelected(value)
                            }
                            preference = null
                        },
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.you_delete_title)) },
            text = { Text(stringResource(R.string.you_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                ) {
                    Text(stringResource(R.string.you_delete_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
            )
        },
        modifier = Modifier.clickable(
            enabled = enabled,
            onClick = onClick,
        ),
    )
}

@Composable
private fun preferenceLabel(value: String): String = when (value) {
    "en" -> "English"
    "es" -> "Español"
    "light" -> stringResource(R.string.settings_light)
    "dark" -> stringResource(R.string.settings_dark)
    else -> stringResource(R.string.settings_system)
}
