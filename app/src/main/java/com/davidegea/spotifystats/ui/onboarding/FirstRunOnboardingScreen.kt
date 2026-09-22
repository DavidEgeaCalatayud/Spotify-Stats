package com.davidegea.spotifystats.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.ui.components.*
import com.davidegea.spotifystats.ui.importhistory.ImportHistorySection

@Composable
fun FirstRunOnboardingScreen(onImportStarted: () -> Unit, onImportFinished: () -> Unit, onContinueWithoutImport: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(step > 0) { step-- }
    StatsPage(resetScrollKey = step) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.labelLarge)
        LinearProgressIndicator(progress = { (step + 1) / 3f }, modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.onboarding_step, step + 1), style = MaterialTheme.typography.bodySmall)
        AnimatedContent(step, transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(180)) }, label = "onboarding") { current ->
            HeroSurface {
                LocalArtwork(listOf("Your music", "Private listening", "Your history")[current], size = 128.dp)
                Text(stringResource(when (current) { 0 -> R.string.onboarding_title; 1 -> R.string.you_privacy_title; else -> R.string.onboarding_ready }), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(when (current) { 0 -> R.string.onboarding_welcome_copy; 1 -> R.string.onboarding_privacy_copy; else -> R.string.onboarding_import_body }), style = MaterialTheme.typography.bodyLarge)
            }
        }
        if (step == 2) {
            ImportHistorySection(onImportStarted, onImportFinished)
            Text(stringResource(R.string.onboarding_reimport_note), style = MaterialTheme.typography.bodySmall)
        } else Button(onClick = { step++ }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.action_next)) }
        if (step > 0) TextButton(onClick = { step-- }) { Text(stringResource(R.string.action_back)) }
        TextButton(onClick = onContinueWithoutImport, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.onboarding_explore_without_import)) }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}
