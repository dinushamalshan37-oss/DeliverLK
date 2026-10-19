package com.example.deliverlk.core.designsystem

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.deliverlk.R
import com.example.deliverlk.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class LanguageOption(
    val code: String,
    val nativeName: String
)

val supportedLanguages = listOf(
    LanguageOption("en", "English"),
    LanguageOption("si", "සිංහල"),
    LanguageOption("ta", "தமிழ்")
)

fun getCurrentLanguageTag(): String {
    val locales = AppCompatDelegate.getApplicationLocales()
    return if (!locales.isEmpty) {
        locales.get(0)?.language ?: "en"
    } else {
        "en"
    }
}

fun changeAppLanguage(languageTag: String, settingsRepository: SettingsRepository? = null) {
    val appLocales = LocaleListCompat.forLanguageTags(languageTag)
    AppCompatDelegate.setApplicationLocales(appLocales)
    if (settingsRepository != null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                settingsRepository.setLanguageTag(languageTag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun LanguagePickerDialog(
    settingsRepository: SettingsRepository? = null,
    onDismiss: () -> Unit
) {
    val currentTag = remember { getCurrentLanguageTag() }
    var selectedTag by remember { mutableStateOf(currentTag) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.language),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                supportedLanguages.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable { selectedTag = lang.code }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedTag == lang.code),
                            onClick = { selectedTag = lang.code }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = lang.nativeName,
                            fontSize = 16.sp,
                            fontWeight = if (selectedTag == lang.code) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    changeAppLanguage(selectedTag, settingsRepository)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}