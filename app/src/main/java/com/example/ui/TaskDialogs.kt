package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, startDirectly: Boolean) -> Unit
) {
    var titleState by remember { mutableStateOf("") }
    var noteState by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("STUDY") }
    var startDirectly by remember { mutableStateOf(true) }

    val categoriesList = listOf(
        Pair("STUDY", "📖 学习"),
        Pair("CODE", "💻 写代码"),
        Pair("EXERCISE", "👟 运动"),
        Pair("READING", "🔖 阅读"),
        Pair("REST", "🐱 休息"),
        Pair("CUSTOM", "📌 其他")
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFFFDF8))
                .border(2.dp, PencilGraphite, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title Sticker Ribbon
                Box(
                    modifier = Modifier
                        .background(StickerYellow, RoundedCornerShape(8.dp))
                        .border(1.dp, PencilGraphite, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🐾 贴上一个新时间标签",
                        color = PencilGraphite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name field
                OutlinedTextField(
                    value = titleState,
                    onValueChange = { titleState = it },
                    label = { Text("事情名称 (例如: 敲Kotlin代码)", fontSize = 11.sp, color = PencilGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PencilGraphite,
                        unfocusedBorderColor = PencilGray,
                        focusedTextColor = PencilGraphite,
                        unfocusedTextColor = PencilGraphite,
                        focusedLabelColor = PencilGraphite
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes field
                OutlinedTextField(
                    value = noteState,
                    onValueChange = { noteState = it },
                    label = { Text("备注或标注 (今日心情细节)", fontSize = 11.sp, color = PencilGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PencilGraphite,
                        unfocusedBorderColor = PencilGray,
                        focusedTextColor = PencilGraphite,
                        unfocusedTextColor = PencilGraphite,
                        focusedLabelColor = PencilGraphite
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🏷️ 选择手账贴纸风格:",
                    color = PencilGraphite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Grid of pastel categories
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesList.take(3).forEach { cat ->
                            CategoryBadgeButton(
                                text = cat.second,
                                isSelected = selectedCategory == cat.first,
                                categoryType = cat.first,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedCategory = cat.first }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesList.takeLast(3).forEach { cat ->
                            CategoryBadgeButton(
                                text = cat.second,
                                isSelected = selectedCategory == cat.first,
                                categoryType = cat.first,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedCategory = cat.first }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle directly start vs pause
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFCF7ED))
                        .border(1.dp, PencilGraphite.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .clickable { startDirectly = !startDirectly }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = startDirectly,
                        onCheckedChange = { startDirectly = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryBrown)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "立即开启专注计时 ⏱️",
                            fontSize = 11.sp,
                            color = PencilGraphite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "打勾立即运行计时器，未打勾会先贴在页面上待命哦喵",
                            fontSize = 9.sp,
                            color = PencilGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm / Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, PencilGraphite),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PencilGraphite),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取 消", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val title = titleState.trim().ifEmpty { "未命名小日子" }
                            // Pass back
                            onConfirm(title, selectedCategory, startDirectly)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                        border = BorderStroke(1.dp, PencilGraphite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("贴上贴纸 ✨", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadgeButton(
    text: String,
    isSelected: Boolean,
    categoryType: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val baseColor = when (categoryType) {
        "STUDY" -> StickerYellow
        "CODE" -> StickerLavender
        "EXERCISE" -> StickerGreen
        "READING" -> StickerRose
        "REST" -> StickerBlue
        else -> StickerPeach
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) baseColor else baseColor.copy(alpha = 0.35f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PencilGraphite else PencilGraphite.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) PencilGraphite else PencilGraphite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}
