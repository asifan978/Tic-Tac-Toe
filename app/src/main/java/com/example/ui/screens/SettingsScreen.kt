package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.UserPreferences
import com.example.model.AIDifficulty
import com.example.model.AVATAR_OPTIONS
import com.example.ui.components.DreamyAvatarView
import com.example.ui.components.DreamyBackground
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    prefs: UserPreferences,
    soundManager: SoundManager,
    onBack: () -> Unit
) {
    var musicEnabled by remember { mutableStateOf(prefs.isMusicEnabled) }
    var sfxEnabled by remember { mutableStateOf(prefs.isSfxEnabled) }
    var vibrateEnabled by remember { mutableStateOf(prefs.isVibrateEnabled) }
    var playerName by remember { mutableStateOf(prefs.playerName) }
    var selectedAvatar by remember { mutableStateOf(prefs.avatarId) }
    var selectedDifficulty by remember { mutableStateOf(prefs.difficulty) }
    var selectedLanguage by remember { mutableStateOf("English") }

    var showResetDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    DreamyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundManager.playButtonTap()
                        // Save name if changed
                        if (playerName.isNotBlank()) {
                            prefs.playerName = playerName.trim()
                        }
                        onBack()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape, ambientColor = PastelPinkLight)
                        .clip(CircleShape)
                        .background(PastelWhite.copy(alpha = 0.9f))
                        .border(1.5.dp, PastelPinkLight, CircleShape)
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PastelPinkDark
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Settings",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = DreamyTextPrimary
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Sound & Feedback
            SettingsCard(title = "Sound & Vibration") {
                // Music Toggle
                SettingToggleRow(
                    title = "Background Music",
                    subtitle = "Soft dreamy music box lullaby",
                    iconEmoji = "🎵",
                    checked = musicEnabled,
                    onCheckedChange = {
                        musicEnabled = it
                        prefs.isMusicEnabled = it
                        soundManager.isMusicEnabled = it
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // SFX Toggle
                SettingToggleRow(
                    title = "Sound Effects",
                    subtitle = "Kawaii pops, chimes & whooshes",
                    iconEmoji = "✨",
                    checked = sfxEnabled,
                    onCheckedChange = {
                        sfxEnabled = it
                        prefs.isSfxEnabled = it
                        soundManager.isSfxEnabled = it
                        if (it) soundManager.playPop()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Vibration Toggle
                SettingToggleRow(
                    title = "Vibration Feedback",
                    subtitle = "Gentle haptic pulses on tap",
                    iconEmoji = "📳",
                    checked = vibrateEnabled,
                    onCheckedChange = {
                        vibrateEnabled = it
                        prefs.isVibrateEnabled = it
                        soundManager.isVibrateEnabled = it
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 2: Player Profile & Avatar Picker
            SettingsCard(title = "Player Profile") {
                // Name Field
                Text(
                    text = "Player Name",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = DreamyTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = playerName,
                    onValueChange = {
                        if (it.length <= 14) {
                            playerName = it
                            prefs.playerName = it
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Options (6 cute kawaii options)
                Text(
                    text = "Choose Your Avatar",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = DreamyTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AVATAR_OPTIONS.take(3).forEach { avatar ->
                        AvatarSelectionItem(
                            avatar = avatar,
                            isSelected = selectedAvatar == avatar.id,
                            onSelect = {
                                soundManager.playPop()
                                selectedAvatar = avatar.id
                                prefs.avatarId = avatar.id
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AVATAR_OPTIONS.drop(3).take(3).forEach { avatar ->
                        AvatarSelectionItem(
                            avatar = avatar,
                            isSelected = selectedAvatar == avatar.id,
                            onSelect = {
                                soundManager.playPop()
                                selectedAvatar = avatar.id
                                prefs.avatarId = avatar.id
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 3: AI Difficulty
            SettingsCard(title = "AI Difficulty") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AIDifficulty.entries.forEach { diff ->
                        val isSelected = selectedDifficulty == diff
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(
                                    if (isSelected) 4.dp else 1.dp,
                                    RoundedCornerShape(16.dp),
                                    ambientColor = PastelLavenderLight
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) PastelPinkLight else PastelWhite)
                                .border(
                                    1.5.dp,
                                    if (isSelected) PastelPinkDark else PastelLavenderLight,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    soundManager.playPop()
                                    selectedDifficulty = diff
                                    prefs.difficulty = diff
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = diff.label,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) PastelPinkDark else DreamyTextPrimary
                                )
                                Text(
                                    text = "+${diff.xpReward} XP",
                                    fontFamily = NunitoFontFamily,
                                    fontSize = 11.sp,
                                    color = DreamyTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 4: Language Selector (UI-ready)
            SettingsCard(title = "Language") {
                val languages = listOf("English", "Kawaii Mode 🌸", "Español", "Français")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    languages.forEach { lang ->
                        val isSelected = selectedLanguage == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PastelLavenderLight else PastelWhite)
                                .border(
                                    1.dp,
                                    if (isSelected) PastelLavenderDark else PastelLavenderLight,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    soundManager.playPop()
                                    selectedLanguage = lang
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lang,
                                fontFamily = NunitoFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                color = if (isSelected) PastelLavenderDark else DreamyTextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 5: Stats Overview
            SettingsCard(title = "Dream Journey Stats") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatPill("Level", "Lv. ${prefs.level}")
                    StatPill("XP", "${prefs.xp}")
                    StatPill("Wins", "${prefs.totalWins}")
                    StatPill("Played", "${prefs.totalGames}")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reset Data Button
            TextButton(
                onClick = { showResetDialog = true }
            ) {
                Text(
                    text = "Reset Game Progress",
                    fontFamily = FredokaFontFamily,
                    fontSize = 13.sp,
                    color = Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = {
                    Text(
                        text = "Reset Progress?",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DreamyTextPrimary
                    )
                },
                text = {
                    Text(
                        text = "This will reset your Level, XP, and stats back to beginning. Are you sure?",
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        color = DreamyTextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            prefs.level = 1
                            prefs.xp = 0
                            prefs.totalGames = 0
                            prefs.totalWins = 0
                            prefs.dailyWinsCount = 0
                            prefs.hintsRemaining = 3
                            prefs.undosRemaining = 3
                            showResetDialog = false
                            soundManager.playPop()
                        }
                    ) {
                        Text(
                            text = "Reset",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel", fontFamily = NunitoFontFamily)
                    }
                },
                shape = RoundedCornerShape(22.dp),
                containerColor = PastelWhite
            )
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = CardBorderAccent)
            .clip(RoundedCornerShape(22.dp))
            .background(PastelWhite.copy(alpha = 0.95f))
            .border(2.5.dp, CardBorderAccent, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = title,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = DreamyTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    iconEmoji: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = iconEmoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = DreamyTextPrimary
            )
            Text(
                text = subtitle,
                fontFamily = NunitoFontFamily,
                fontSize = 11.sp,
                color = DreamyTextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PastelWhite,
                checkedTrackColor = PastelPinkPrimary,
                uncheckedThumbColor = PastelWhite,
                uncheckedTrackColor = PastelLavenderLight
            )
        )
    }
}

@Composable
fun AvatarSelectionItem(
    avatar: com.example.model.AvatarOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) PastelPinkDark else PastelLavenderLight,
                    shape = CircleShape
                )
                .padding(3.dp)
        ) {
            DreamyAvatarView(
                avatarId = avatar.id,
                size = 50.dp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = avatar.name,
            fontFamily = NunitoFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = if (isSelected) PastelPinkDark else DreamyTextSecondary
        )
    }
}

@Composable
fun StatPill(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PastelPinkBg)
            .border(1.dp, PastelPinkLight, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontFamily = NunitoFontFamily,
                fontSize = 10.sp,
                color = DreamyTextMuted
            )
            Text(
                text = value,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = DreamyTextPrimary
            )
        }
    }
}
