package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.DailyMoodEntity
import com.example.db.TaskEntity
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun BookLayout(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val pageIndex by viewModel.currentPageIndex.collectAsState()
    val activeTask by viewModel.activeTask.collectAsState()
    val catCurrentState by viewModel.catState.collectAsState()
    val allTasks by viewModel.allTasksByDate.collectAsState()
    val allMoods by viewModel.allMoods.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    val isOpened = pageIndex > 0

    // Horizontal Swipe Page Turn Track
    var dragAccumulator by remember { mutableStateOf(0f) }
    var pageTransitionDirection by remember { mutableStateOf(1) }

    fun triggerPageChange(from: Int, to: Int, forward: Boolean = to > from) {
        if (to == from) return
        pageTransitionDirection = if (forward) 1 else -1
        viewModel.setPageIndex(to.coerceIn(0, 7))
    }

    // Cozy Handmade Desk Background Panel
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEADCC9)) // Warm off-book desk color
            .pointerInput(pageIndex) {
                // Hand horizontal swipe gestures
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onDragEnd = {
                        if (dragAccumulator > 120f) {
                            // Turn page back
                            if (pageIndex > 0) {
                                val target = pageIndex - 1
                                triggerPageChange(pageIndex, target, false)
                                viewModel.triggerCatMessage("哗啦……翻回前一页，本喵帮你扶着纸呢，喵～")
                            } else {
                                viewModel.triggerCatMessage("已经是封面啦，你可以戳戳我的肚子聊天，喵呜！")
                            }
                        } else if (dragAccumulator < -120f) {
                            // Turn page forward
                            if (pageIndex < 7) {
                                val target = pageIndex + 1
                                triggerPageChange(pageIndex, target, true)
                                viewModel.triggerCatMessage("哗啦……翻到下一页，看看写下的新故事，咪！")
                            } else {
                                viewModel.triggerCatMessage("到本周最后一页啦，喵～写得手账太棒了！")
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount
                    }
                )
            }
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Draw Cute stationary objects directly on desktop background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val graphiteColor = PencilGraphite.copy(alpha = 0.5f)
            
            // 1. Draw cozy pencils at top-left
            drawLine(graphiteColor, Offset(15.dp.toPx(), 45.dp.toPx()), Offset(65.dp.toPx(), 95.dp.toPx()), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(Color(0xFFE57373).copy(alpha = 0.5f), Offset(25.dp.toPx(), 35.dp.toPx()), Offset(75.dp.toPx(), 85.dp.toPx()), strokeWidth = 4f, cap = StrokeCap.Round)
            
            // 2. Draw stars outlines at bottom-left background
            drawStarBackground(this, 35.dp.toPx(), size.height - 110.dp.toPx(), radius = 12f)
            drawStarBackground(this, 75.dp.toPx(), size.height - 70.dp.toPx(), radius = 8f)
            
            // 3. Draw a tea saucer clip circle at top-right
            drawCircle(color = PencilGraphite.copy(alpha = 0.08f), radius = 32.dp.toPx(), center = Offset(size.width - 65.dp.toPx(), 75.dp.toPx()))
            drawArc(
                color = PencilGraphite.copy(alpha = 0.12f),
                startAngle = 0f, 
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(size.width - 60.dp.toPx(), 40.dp.toPx()),
                size = Size(40.dp.toPx(), 40.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
            
            // 4. Little hand drawn label
            drawStringOutline(this, size.width - 130.dp.toPx(), size.height - 45.dp.toPx())
        }

        // Bookmark Ribbon / Corner Bookmark Tags
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 20.dp)
                .shadow(2.dp, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BookmarkTag(title = "封面", isActive = pageIndex == 0, color = Color(0xFFFFCBD2)) { triggerPageChange(pageIndex, 0) }
            BookmarkTag(title = "今天", isActive = pageIndex == 6, color = Color(0xFFFFF1AC)) { triggerPageChange(pageIndex, 6) }
            BookmarkTag(title = "周报", isActive = pageIndex == 7, color = Color(0xFFB1E1FE)) { triggerPageChange(pageIndex, 7) }
        }

        // MAIN BOOK HOLDER
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Soft resting shadow behind the book.
            Box(
                modifier = Modifier
                    .width(if (isOpened) 350.dp else 230.dp)
                    .height(if (isOpened) 540.dp else 360.dp)
                    .offset(y = 20.dp)
                    .shadow(18.dp, RoundedCornerShape(18.dp))
                    .background(Color.Transparent)
            )

            // STATE A: CLOSED HARDCOVER BOOK STATE
            if (!isOpened) {
                Box(
                    modifier = Modifier
                        .width(235.dp)
                        .height(370.dp)
                        .clickable {
                            viewModel.triggerCatMessage("哗啦……手账展开啦！敲可爱的说，喵呜！")
                            triggerPageChange(0, 1, true)
                        }
                ) {
                    // Page edge stacked lines thickness (Left & bottom edge highlights)
                    repeat(4) { i ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(x = (i * 2.5).dp, y = (i * 1.5).dp)
                                .background(
                                    color = Color(0xFFFFFEEB), // light cream stacked pages shade
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .border(1.dp, Color(0x2A5D4037), RoundedCornerShape(14.dp))
                        )
                    }

                    // Rich Hardcover Core Card (Soft Pastel Teddy Bear brown or light Pink)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = (-2).dp)
                            .background(
                                color = Color(0xFF8D6E63), // Cozy book leather brown
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(2.dp, PencilGraphite, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        // Inner pattern cover border
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.2.dp, Color(0xFFFFE0B2).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Star icons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("✦", color = Color(0xFFFFF59D), fontSize = 14.sp)
                                    Text("✦", color = Color(0xFFFFF59D), fontSize = 14.sp)
                                }

                                // Book Cover Title Stamp
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFCF8F2), RoundedCornerShape(12.dp))
                                            .border(1.5.dp, PencilGraphite, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "喵咪时间手账",
                                                color = PencilGraphite,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Serif
                                            )
                                            Text(
                                                text = "MEOW DIARY STATIONARY",
                                                color = PencilGray,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }

                                // Cover Illustration Sticker (Drawn cozy painter cat)
                                Box(
                                    modifier = Modifier.size(110.dp, 110.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CuteCatCompanion(
                                        state = CatState.IDLE,
                                        customMessage = "喵～翻开有超可爱的3D书页效果和心情盖章，咪！"
                                    )
                                }

                                // Bottom gold status label
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFFB5C5), RoundedCornerShape(8.dp))
                                            .border(1.dp, PencilGraphite, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "点击翻开 • ENTRANCE 📖",
                                            color = PencilGraphite,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("🐾", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                        Text("🐾", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Thick binding spine highlight on the left
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(10.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF3E2723), Color.Transparent)
                                )
                            )
                    )
                }
            } else {
                // STATE B: OPENED DOUBLE-PAGE SPREAD SKETCHBOOK
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .height(555.dp)
                ) {
                    // Double-page hardcover background plate
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(3.dp, RoundedCornerShape(12.dp))
                            .background(
                                color = Color(0xFFFAF6EE), // Cream open page backing
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(2.dp, PencilGraphite, RoundedCornerShape(12.dp))
                    ) {
                        // Spread page divided into Left column and Right column
                        Row(modifier = Modifier.fillMaxSize()) {
                            // --- LEFT PAGE SPREAD: COMPANION & STAMPS (LEFT COLUMN, ALWAYS COZY & FIXED) ---
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(Color(0xFFFFFDF8))
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Fluffy Cat Companion Active Zone
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1.3f),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    val activeState = when {
                                        activeTask == null -> CatState.IDLE
                                        activeTask?.status == "RUNNING" -> CatState.RUNNING
                                        activeTask?.status == "PAUSED" -> CatState.PAUSED
                                        else -> CatState.IDLE
                                    }
                                    CuteCatCompanion(
                                        state = activeState,
                                        modifier = Modifier.fillMaxSize(),
                                        customMessage = viewModel.catCustomMessage.collectAsState().value
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Divider(color = BookLineColor, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(4.dp))

                                // Interactive Hand-drawn Stamps Rubber Board (手账盖盖章交互)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1.1f)
                                        .background(Color(0xFFFFF9EE), RoundedCornerShape(10.dp))
                                        .border(1.dp, BookLineColor, RoundedCornerShape(10.dp))
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🐾 喵喵手账盖章社 (Tap)",
                                        fontSize = 9.sp,
                                        color = PencilGraphite,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    // Custom stamp buttons with interactive counts
                                    var countBest by remember { mutableStateOf(3) }
                                    var countStar by remember { mutableStateOf(5) }
                                    var countCute by remember { mutableStateOf(2) }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        // 1. Red "BEST" stamp
                                        StampItem(
                                            label = "👍 赞",
                                            count = countBest,
                                            color = Color(0xFFE57373),
                                            onStamp = { 
                                                countBest++ 
                                                viewModel.triggerCatMessage("呜哇！你给自己盖章了‘赞’！努力值 +1，喵！")
                                            }
                                        )
                                        // 2. Cyan "LOVE" stamp
                                        StampItem(
                                            label = "✦ 炫",
                                            count = countStar,
                                            color = Color(0xFF64B5F6),
                                            onStamp = { 
                                                countStar++ 
                                                viewModel.triggerCatMessage("这个炫彩星星印章是认真生活的奖赏哦，喵呜！")
                                            }
                                        )
                                        // 3. Yellow "SMILE" stamp
                                        StampItem(
                                            label = "😸 萌",
                                            count = countCute,
                                            color = Color(0xFFFFB74D),
                                            onStamp = { 
                                                countCute++ 
                                                viewModel.triggerCatMessage("嘻嘻，盖章了一个猫咪笑脸！今天的心情超级萌，咪～")
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "本页手账已获珍贵印章 ${countBest+countStar+countCute} 枚",
                                        fontSize = 8.sp,
                                        color = PencilGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // --- MIDDLE BOUND SPINE SEPARATOR SHEET (中央中轴线及线圈) ---
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight()
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            colors = listOf(Color.Black.copy(alpha = 0.08f), Color.Transparent, Color.Black.copy(alpha = 0.08f))
                                        )
                                    )
                            )

                            // --- RIGHT PAGE SPREAD: ACTIVE JOURNAL CARD (RIGHT COLUMN, SLIDE PAGE CONTENT) ---
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(Color(0xFFFCF8F2))
                            ) {
                                AnimatedContent(
                                    targetState = pageIndex,
                                    transitionSpec = {
                                        val direction = pageTransitionDirection
                                        (slideInHorizontally(
                                            animationSpec = tween(260, easing = EaseOutCubic),
                                            initialOffsetX = { fullWidth -> fullWidth * direction }
                                        ) + fadeIn(tween(160))).togetherWith(
                                            slideOutHorizontally(
                                                animationSpec = tween(260, easing = EaseOutCubic),
                                                targetOffsetX = { fullWidth -> -fullWidth * direction }
                                            ) + fadeOut(tween(120))
                                        )
                                    },
                                    label = "PageSlide"
                                ) { targetPage ->
                                    RenderPage(
                                        index = targetPage,
                                        allTasks = allTasks,
                                        allMoods = allMoods,
                                        activeTask = activeTask,
                                        viewModel = viewModel,
                                        onAddTaskClick = { showAddTaskDialog = true }
                                    )
                                }
                            }
                        }

                        // Spiral ring binder elements running down the center split spine
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(12.dp)
                                .align(Alignment.Center)
                                .padding(vertical = 18.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            repeat(9) {
                                Canvas(modifier = Modifier.size(8.dp, 8.dp)) {
                                    drawCircle(color = PencilGraphite, radius = 2.5f.dp.toPx())
                                    drawArc(
                                        color = Color(0xFFB0BEC5), // Silver spiral loops
                                        startAngle = -90f,
                                        sweepAngle = 180f,
                                        useCenter = false,
                                        style = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                            }
                        }
                    }

                    // Bottom-Left Tappable Dog-ear Corner
                    if (pageIndex > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(bottom = 6.dp, start = 6.dp)
                                .size(34.dp, 34.dp)
                                .clickable {
                                    val target = pageIndex - 1
                                    triggerPageChange(pageIndex, target, false)
                                    viewModel.triggerCatMessage("哗啦……翻回前一页，本喵扶着纸呢，喵～")
                                },
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val p = Path().apply {
                                    moveTo(0f, size.height)
                                    lineTo(size.width * 0.4f, size.height)
                                    lineTo(0f, size.height * 0.6f)
                                    close()
                                }
                                drawPath(p, color = PencilGray.copy(alpha = 0.3f))
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "上一页",
                                tint = PencilGray,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(1.dp)
                                    .rotate(-45f)
                            )
                        }
                    }

                    // Bottom-Right Tappable Dog-ear Corner
                    if (pageIndex < 7) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 6.dp, end = 6.dp)
                                .size(34.dp, 34.dp)
                                .clickable {
                                    val target = pageIndex + 1
                                    triggerPageChange(pageIndex, target, true)
                                    viewModel.triggerCatMessage("哗啦……向前翻了一页，咪！")
                                },
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val p = Path().apply {
                                    moveTo(size.width, size.height)
                                    lineTo(size.width * 0.6f, size.height)
                                    lineTo(size.width, size.height * 0.6f)
                                    close()
                                }
                                drawPath(p, color = PencilGray.copy(alpha = 0.3f))
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "下一页",
                                tint = PencilGray,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(1.dp)
                                    .rotate(45f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Task Creation Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, category, startDirectly ->
                viewModel.createAndStartTimer(title, category, startDirectly)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun StampItem(
    label: String,
    count: Int,
    color: Color,
    onStamp: () -> Unit
) {
    var isPressing by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressing) 0.82f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh, dampingRatio = 0.45f)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scaleAnim
                scaleY = scaleAnim
            }
            .clickable {
                isPressing = true
                onStamp()
            }
    ) {
        // Core Circular Stamp Outline representing image 2 hand stitched rubber look
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color.White, CircleShape)
                .border(1.5.dp, color, CircleShape)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.take(2),
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "x$count",
            color = PencilGraphite,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )

        LaunchedEffect(isPressing) {
            if (isPressing) {
                delay(120)
                isPressing = false
            }
        }
    }
}

@Composable
fun RenderPage(
    index: Int,
    allTasks: Map<String, List<TaskEntity>>,
    allMoods: Map<String, DailyMoodEntity>,
    activeTask: TaskEntity?,
    viewModel: MainViewModel,
    onAddTaskClick: () -> Unit
) {
    when (index) {
        0 -> BookCoverPage(onStart = { viewModel.setPageIndex(1) })
        in 1..6 -> {
            val dStr = viewModel.pageDateStrings[index - 1]
            val tasksForDay = allTasks[dStr] ?: emptyList()
            val moodForDay = allMoods[dStr]
            DailyJournalPage(
                dateString = dStr,
                pageNumber = index,
                tasks = tasksForDay,
                moodEntity = moodForDay,
                isToday = index == 6,
                activeTask = activeTask,
                viewModel = viewModel,
                onAddTaskClick = onAddTaskClick
            )
        }
        7 -> {
            WeeklyReportPage(
                pageDateStrings = viewModel.pageDateStrings,
                allTasks = allTasks,
                allMoods = allMoods,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun BookmarkTag(
    title: String,
    isActive: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(36.dp)
            .height(if (isActive) 35.dp else 26.dp)
            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(color)
            .border(1.dp, PencilGraphite, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .clickable(onClick = onClick)
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = PencilGraphite,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 10.sp
        )
    }
}

// ---------------- 1. BOOK COVER PAGE ----------------
@Composable
fun BookCoverPage(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Large title card inside Cover
        Box(
            modifier = Modifier
                .background(Color(0xFFFFFDF5), RoundedCornerShape(12.dp))
                .border(2.dp, PencilGraphite, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "喵咪手账",
                    color = PencilGraphite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "COZY WORKSPACE LOG",
                    color = PencilGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cute kitty illustration sticker
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            CuteCatCompanion(
                state = CatState.IDLE,
                customMessage = "喵～你好，本手账支持横向拖拽翻页以及3D空间拖转查看噢！快翻页吧！"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, PencilGraphite),
            modifier = Modifier
                .height(40.dp)
                .widthIn(min = 120.dp)
        ) {
            Text(
                text = "翻开手账本 📖",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
        
        Spacer(modifier = Modifier.height(5.dp))
    }
}

// ---------------- 2. DAILY JOURNAL PAGE ----------------
@Composable
fun DailyJournalPage(
    dateString: String,
    pageNumber: Int,
    tasks: List<TaskEntity>,
    moodEntity: DailyMoodEntity?,
    isToday: Boolean,
    activeTask: TaskEntity?,
    viewModel: MainViewModel,
    onAddTaskClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val formattedDate = remember(dateString) { getPrettyDateHeader(dateString) }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
        ) {
            // Header panel containing Date and status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formattedDate,
                        color = PencilGraphite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "今日手账 • Page $pageNumber",
                        color = PencilGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Weather and Mood sticky stickers
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeatherSticker(
                        weather = moodEntity?.weather ?: "SUNNY",
                        onSelect = { viewModel.selectWeatherForDate(dateString, it) }
                    )
                    MoodSticker(
                        mood = moodEntity?.mood ?: "PEACEFUL",
                        onSelect = { viewModel.selectMoodForDate(dateString, it) }
                    )
                }
            }

            Divider(color = BookLineColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            // Timer Panel (shown on Today active page, or when task is running)
            if (isToday) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                        .shadow(2.dp, RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFFDF8), RoundedCornerShape(10.dp))
                        .border(1.dp, PencilGraphite, RoundedCornerShape(10.dp))
                        .padding(6.dp)
                ) {
                    // Cute simulated Washi tape overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-14).dp)
                            .width(65.dp)
                            .height(12.dp)
                            .rotate(-1.2f)
                            .background(Color(0xFFB2EBF2).copy(alpha = 0.5f))
                            .border(0.5.dp, Color(0x6680DEEA))
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (activeTask != null) {
                            Text(
                                text = "正在专注：“${activeTask.title}”",
                                fontSize = 10.sp,
                                color = PencilGraphite,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            // Timer tick
                            val elapsedSecondsState = remember { mutableLongStateOf(0L) }
                            LaunchedEffect(activeTask, activeTask.status) {
                                if (activeTask.status == "RUNNING") {
                                    while (true) {
                                        val start = activeTask.startTimeMs ?: System.currentTimeMillis()
                                        elapsedSecondsState.longValue = activeTask.durationSeconds + ((System.currentTimeMillis() - start) / 1000L)
                                        delay(1000)
                                    }
                                } else {
                                    elapsedSecondsState.longValue = activeTask.durationSeconds
                                }
                            }

                            Text(
                                text = formatSeconds(elapsedSecondsState.longValue),
                                fontSize = 16.sp,
                                color = AccentCoral,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )

                            // Timer controllers row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Close/cancel
                                IconButton(
                                    onClick = { viewModel.cancelActiveTimer() },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, PencilGraphite, CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "取消", tint = PencilGraphite, modifier = Modifier.size(14.dp))
                                }

                                if (activeTask.status == "RUNNING") {
                                    IconButton(
                                        onClick = { viewModel.pauseTimer() },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(StickerYellow, CircleShape)
                                            .border(1.dp, PencilGraphite, CircleShape)
                                    ) {
                                        Canvas(modifier = Modifier.size(8.dp)) {
                                            drawRect(color = PencilGraphite, topLeft = Offset(1.5.dp.toPx(), 2.dp.toPx()), size = Size(1.5.dp.toPx(), 4.dp.toPx()))
                                            drawRect(color = PencilGraphite, topLeft = Offset(5.dp.toPx(), 2.dp.toPx()), size = Size(1.5.dp.toPx(), 4.dp.toPx()))
                                        }
                                    }
                                } else {
                                    IconButton(
                                        onClick = { viewModel.resumeTimer() },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(Color(0xFFC8E6C9), CircleShape)
                                            .border(1.dp, PencilGraphite, CircleShape)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "继续", tint = PencilGraphite, modifier = Modifier.size(16.dp))
                                    }
                                }

                                // Complete FINISH button
                                Button(
                                    onClick = { viewModel.completeTimer() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, PencilGraphite),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("完成 FINISH", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        } else {
                            Text(
                                text = "🐾 戳右下角，给今天盖个新专注标签吧！",
                                fontSize = 9.sp,
                                color = PencilGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Scrollable stickers listing
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🐾 空白的手账本页面\n戳 ＋ 粘贴可爱标签喵～",
                            color = PencilGray.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                } else {
                    tasks.forEach { task ->
                        TaskStickerItem(task = task, onDelete = { viewModel.deleteCompletedTask(task.id) })
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                DailyNotesArea(
                    notes = moodEntity?.dailySummaryText ?: "",
                    onNotesChanged = { viewModel.updateDailyNotes(dateString, it) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            DailySummaryPanel(tasks = tasks)
        }

        // Add sticker float action button
        FloatingActionButton(
            onClick = onAddTaskClick,
            containerColor = PencilGraphite,
            contentColor = PaperPageColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 60.dp, end = 6.dp)
                .size(38.dp)
                .border(1.dp, PencilGraphite, RoundedCornerShape(12.dp))
        ) {
            Text(
                text = "＋",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PaperPageColor
            )
        }
    }
}

// ---------------- 3. TASK STICKER ITEM (便签贴纸) ----------------
@Composable
fun TaskStickerItem(
    task: TaskEntity,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rotationAngle = remember(task.id) {
        val angles = listOf(-1.2f, 0.8f, -0.7f, 1.2f, -1.0f, 1.0f)
        angles[(task.id % angles.size).toInt().coerceAtLeast(0)]
    }

    val bgColor = when (task.category) {
        "STUDY" -> StickerYellow
        "CODE" -> StickerLavender
        "EXERCISE" -> StickerGreen
        "READING" -> StickerRose
        "REST" -> StickerBlue
        else -> StickerPeach
    }

    // Keep label terms simple and minimal as requested
    val categoryLabel = when (task.category) {
        "STUDY" -> "学习"
        "CODE" -> "写代码"
        "EXERCISE" -> "运动"
        "READING" -> "阅读"
        "REST" -> "休息"
        else -> "事项"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .rotate(rotationAngle)
            .shadow(1.5.dp, RoundedCornerShape(8.dp))
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, PencilGraphite, RoundedCornerShape(8.dp))
            .clickable { isExpanded = !isExpanded }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Renders custom styled cat-head roles next to label stickers!
                MiniCatCategoryIcon(
                    category = task.category,
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Text(
                        text = task.title,
                        fontSize = 11.sp,
                        color = PencilGraphite,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = categoryLabel,
                        fontSize = 8.sp,
                        color = PencilGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatPrettySeconds(task.durationSeconds),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = PencilGraphite
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "详情",
                    tint = PencilGraphite,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Divider(color = PencilGraphite.copy(alpha = 0.12f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "状态: ${getChStatus(task.status)}",
                            fontSize = 9.sp,
                            color = PencilGraphite,
                            fontWeight = FontWeight.Bold
                        )
                        if (task.endTimeMs != null) {
                            val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val endStr = timeSdf.format(Date(task.endTimeMs))
                            Text(
                                text = "完成时间: $endStr",
                                fontSize = 8.sp,
                                color = PencilGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Delete sticky
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "撕下便签",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                if (task.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "📒 记录笔记: " + task.notes,
                            fontSize = 9.sp,
                            color = PencilGray,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ---------------- 4. NOTES AREA (手账便签) ----------------
@Composable
fun DailyNotesArea(
    notes: String,
    onNotesChanged: (String) -> Unit
) {
    var textState by remember(notes) { mutableStateOf(notes) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(1.dp, RoundedCornerShape(10.dp))
            .background(Color(0xFFFFF9EE), RoundedCornerShape(10.dp))
            .border(1.dp, PencilGraphite, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = "✍️", fontSize = 11.sp)
                Text(text = "本页随笔:", color = PencilGraphite, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }

            OutlinedTextField(
                value = textState,
                onValueChange = {
                    textState = it
                    onNotesChanged(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 45.dp, max = 85.dp),
                placeholder = { Text("写今天的心情/好吃的东西吧～", fontSize = 9.sp, color = PencilGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = PencilGraphite,
                    unfocusedTextColor = PencilGraphite
                ),
                textStyle = TextStyle(fontSize = 10.sp, lineHeight = 12.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        }
    }
}

// ---------------- 5. DAILY SUMMARY PANEL (今日底栏总结) ----------------
@Composable
fun DailySummaryPanel(tasks: List<TaskEntity>) {
    val totalFocusedSecs = remember(tasks) { tasks.sumOf { it.durationSeconds } }
    val completedCount = remember(tasks) { tasks.size }

    val encouragementQuote = remember(tasks, totalFocusedSecs) {
        when {
            completedCount == 0 -> "“今天还没有开始做事情噢，我们都在等你，喵～”"
            totalFocusedSecs > 10800L -> "“哇！今天专注生活了 ${formatPrettySeconds(totalFocusedSecs)}！太厉害啦！”"
            totalFocusedSecs > 3600L -> "“今天坚持完成了 ${completedCount} 个事情。小猫真心给你点赞，咪！”"
            else -> "“迈出的每一步都是闪光的手账魔法，足够好啦！”"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .background(Color(0xFFFCF7ED), RoundedCornerShape(8.dp))
            .border(1.dp, PencilGraphite, RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "专注时隙 ⏱️", fontSize = 8.sp, color = PencilGray, fontWeight = FontWeight.Black)
                    Text(
                        text = formatPrettySeconds(totalFocusedSecs),
                        fontSize = 11.sp,
                        color = PencilGraphite,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "盖章事项 💮", fontSize = 8.sp, color = PencilGray, fontWeight = FontWeight.Black)
                    Text(
                        text = "${completedCount}个标签",
                        fontSize = 11.sp,
                        color = PencilGraphite,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = encouragementQuote,
                color = PencilGray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------------- 6. WEEKLY REPORT SUMMARY PAGE (周总结手账) ----------------
@Composable
fun WeeklyReportPage(
    pageDateStrings: List<String>,
    allTasks: Map<String, List<TaskEntity>>,
    allMoods: Map<String, DailyMoodEntity>,
    viewModel: MainViewModel
) {
    val weekTasks = remember(allTasks, pageDateStrings) {
        pageDateStrings.flatMap { allTasks[it] ?: emptyList() }
    }

    val totalWeekSecs = remember(weekTasks) { weekTasks.sumOf { it.durationSeconds } }
    val totalTasksCount = remember(weekTasks) { weekTasks.size }

    val categoryTally = remember(weekTasks) {
        val groups = weekTasks.groupBy { it.category }
        groups.mapValues { entry -> entry.value.sumOf { it.durationSeconds } }
    }

    val mostCommonCategory = remember(categoryTally) {
        val best = categoryTally.maxByOrNull { it.value }?.key
        when (best) {
            "STUDY" -> "📚 学习"
            "CODE" -> "💻 敲代码"
            "EXERCISE" -> "👟 运动"
            "READING" -> "阅读"
            "REST" -> "休息"
            else -> "没有任何"
        }
    }

    val dayDurations = remember(allTasks, pageDateStrings) {
        pageDateStrings.associateWith { date ->
            (allTasks[date] ?: emptyList()).sumOf { it.durationSeconds }
        }
    }
    val bestDayDate = remember(dayDurations) {
        dayDurations.maxByOrNull { it.value }?.key ?: "今天"
    }
    val bestDayPretty = remember(bestDayDate) {
        if (bestDayDate == "今天") "无" else getPrettyDateHeader(bestDayDate).take(5)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // Core weekly title sticker
        Box(
            modifier = Modifier
                .background(Color(0xFFE8DDFC), RoundedCornerShape(8.dp))
                .border(1.dp, PencilGraphite, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "📊 本周时间周终审报告",
                color = PencilGraphite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Week stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .background(Color(0xFFFFF9EE), RoundedCornerShape(8.dp))
                    .border(1.dp, PencilGraphite, RoundedCornerShape(8.dp))
                    .padding(6.dp)
            ) {
                Column {
                    Text(text = "累计专注", fontSize = 7.sp, color = PencilGray, fontWeight = FontWeight.Bold)
                    Text(text = formatPrettySeconds(totalWeekSecs), fontSize = 10.sp, color = PencilGraphite, fontWeight = FontWeight.Black)
                }
            }

            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .background(Color(0xFFFFF9EE), RoundedCornerShape(8.dp))
                    .border(1.dp, PencilGraphite, RoundedCornerShape(8.dp))
                    .padding(6.dp)
            ) {
                Column {
                    Text(text = "高峰日", fontSize = 7.sp, color = PencilGray, fontWeight = FontWeight.Bold)
                    Text(text = bestDayPretty, fontSize = 10.sp, color = PencilGraphite, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dynamic weekly distributions (6 columns)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .background(Color(0xFFFDFCF9), RoundedCornerShape(10.dp))
                .border(1.dp, PencilGraphite, RoundedCornerShape(10.dp))
                .padding(6.dp)
        ) {
            Column {
                Text(
                    text = "📈 每日时间分配手账:",
                    fontSize = 8.sp,
                    color = PencilGraphite,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxVal = remember(dayDurations) { dayDurations.values.maxOrNull()?.toFloat() ?: 1f }.coerceAtLeast(1800f)

                    pageDateStrings.reversed().forEach { dateStr ->
                        val prettyDay = getShortWeekDay(dateStr)
                        val dur = dayDurations[dateStr] ?: 0L
                        val barHeightFraction = (dur.toFloat() / maxVal).coerceIn(0.08f, 1f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(
                                text = if (dur == 0L) "" else "${(dur / 60)}分",
                                fontSize = 7.sp,
                                color = PencilGray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .fillMaxHeight(barHeightFraction)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(if (dur > 3600) StickerGreen else StickerYellow)
                                    .border(1.dp, PencilGraphite, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = prettyDay,
                                fontSize = 8.sp,
                                color = PencilGraphite,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Week priority categories
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD4EFFC), RoundedCornerShape(10.dp))
                .border(1.dp, PencilGraphite, RoundedCornerShape(10.dp))
                .padding(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(text = "🏆 本周最高频事情标签:", fontSize = 7.sp, color = PencilGray, fontWeight = FontWeight.Bold)
                    Text(text = mostCommonCategory, fontSize = 11.sp, color = PencilGraphite, fontWeight = FontWeight.Black)
                }
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "完成 $totalTasksCount 个项目",
                        fontSize = 9.sp,
                        color = PencilGraphite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Weekly cat quote panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .background(Color(0xFFFFF9EE), RoundedCornerShape(10.dp))
                .border(1.dp, PencilGraphite, RoundedCornerShape(10.dp))
                .padding(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "🐾 喵咪周终审寄语:", fontSize = 8.sp, color = PencilGray, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            totalWeekSecs > 14400L -> "“喵！本周你累积专注时长好充足！小爪全力按印认证超闪亮！奖励你下周贴纸管够！”"
                            totalWeekSecs > 3600L -> "“敲代码跟休息时间最长哦，记录了 $totalTasksCount 个美好温润瞬间，已经很优秀啦，咪～”"
                            else -> "“咕噜咕噜……这周节奏像微风一样安闲，认真睡好与生活，足够完美啦，喵呜～”"
                        },
                        fontSize = 8.sp,
                        color = PencilGraphite,
                        lineHeight = 11.sp
                    )
                }
            }
        }
    }
}

// ---------------- 7. WEATHER & MOOD STICKERS (天气/心情手账贴纸) ----------------
@Composable
fun WeatherSticker(
    weather: String,
    onSelect: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val weatherIcon = when (weather) {
        "SUNNY" -> "☀️ 晴"
        "CLOUDY" -> "☁️ 阴"
        "RAINY" -> "🌧️ 雨"
        "WINDY" -> "💨 风"
        else -> "☀️"
    }

    Box {
        Text(
            text = weatherIcon,
            fontSize = 9.sp,
            color = PencilGraphite,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .background(Color(0xFFD4EFFC), RoundedCornerShape(6.dp))
                .border(1.5.dp, PencilGraphite, RoundedCornerShape(6.dp))
                .clickable { isExpanded = true }
                .padding(horizontal = 6.dp, vertical = 3.dp)
        )

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.background(Color(0xFFFFFDF5))
        ) {
            DropdownMenuItem(
                text = { Text("☀️ 暖阳", fontSize = 10.sp, color = PencilGraphite) },
                onClick = { onSelect("SUNNY"); isExpanded = false }
            )
            DropdownMenuItem(
                text = { Text("☁️ 阴天", fontSize = 10.sp, color = PencilGraphite) },
                onClick = { onSelect("CLOUDY"); isExpanded = false }
            )
            DropdownMenuItem(
                text = { Text("🌧️ 细雨", fontSize = 10.sp, color = PencilGraphite) },
                onClick = { onSelect("RAINY"); isExpanded = false }
            )
            DropdownMenuItem(
                text = { Text("💨 狂风", fontSize = 10.sp, color = PencilGraphite) },
                onClick = { onSelect("WINDY"); isExpanded = false }
            )
        }
    }
}

@Composable
fun MoodSticker(
    mood: String,
    onSelect: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val moodIcon = when (mood) {
        "HAPPY" -> "😸 开心"
        "TIRED" -> "😿 累累"
        "PEACEFUL" -> "😽 平静"
        "ANXIOUS" -> "🙀 焦虑"
        "FULFILLED" -> "😻 充实"
        else -> "😽"
    }

    Box {
        Text(
            text = moodIcon,
            fontSize = 9.sp,
            color = PencilGraphite,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .background(Color(0xFFFFD6DC), RoundedCornerShape(6.dp))
                .border(1.5.dp, PencilGraphite, RoundedCornerShape(6.dp))
                .clickable { isExpanded = true }
                .padding(horizontal = 6.dp, vertical = 3.dp)
        )

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.background(Color(0xFFFFFDF5))
        ) {
            DropdownMenuItem(
                text = { Text("😸 开心", fontSize = 10.sp, color = PencilGraphite) },
                onClick = { onSelect("HAPPY"); isExpanded = false }
            )
            DropdownMenuItem(
                text = { Text("😿 疲惫", fontSize = 10.sp, color = PencilGraphite) },
                onClick = { onSelect("TIRED"); isExpanded = false }
            )
            DropdownMenuItem(
                text = { Text("😽 平静", fontSize = 10.sp, color = PencilGraphite) },
                onClick = { onSelect("PEACEFUL"); isExpanded = false }
            )
            DropdownMenuItem(
                text = { Text("🙀 焦虑", fontSize = 10.sp, color = PencilGraphite) },
                onClick = { onSelect("ANXIOUS"); isExpanded = false }
            )
            DropdownMenuItem(
                text = { Text("😻 充实", fontSize = 10.sp, color = PencilGraphite) },
                onClick = { onSelect("FULFILLED"); isExpanded = false }
            )
        }
    }
}

// ---------------- BACKGROUND RENDERING HELPERS ----------------
private fun drawStarBackground(scope: androidx.compose.ui.graphics.drawscope.DrawScope, x: Float, y: Float, radius: Float) {
    val starColor = PencilGraphite.copy(alpha = 0.12f)
    scope.drawLine(color = starColor, start = Offset(x - radius, y), end = Offset(x + radius, y), strokeWidth = 3f)
    scope.drawLine(color = starColor, start = Offset(x, y - radius), end = Offset(x, y + radius), strokeWidth = 3f)
    scope.drawLine(color = starColor, start = Offset(x - radius * 0.7f, y - radius * 0.7f), end = Offset(x + radius * 0.7f, y + radius * 0.7f), strokeWidth = 2f)
    scope.drawLine(color = starColor, start = Offset(x - radius * 0.7f, y + radius * 0.7f), end = Offset(x + radius * 0.7f, y - radius * 0.7f), strokeWidth = 2f)
}

private fun drawStringOutline(scope: androidx.compose.ui.graphics.drawscope.DrawScope, x: Float, y: Float) {
    with(scope) {
        drawRoundRect(
            color = PencilGraphite.copy(alpha = 0.1f),
            topLeft = Offset(x, y),
            size = Size(85.dp.toPx(), 22.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            style = Stroke(width = 1.5f.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f))
        )
    }
}

// ---------------- HELPERS & FORMATTER ----------------
private fun formatSeconds(totalSecs: Long): String {
    val hrs = totalSecs / 3600
    val mins = (totalSecs % 3600) / 60
    val secs = totalSecs % 60
    return String.format("%02d:%02d:%02d", hrs, mins, secs)
}

private fun formatPrettySeconds(totalSecs: Long): String {
    val hrs = totalSecs / 3600
    val mins = (totalSecs % 3600) / 60
    return when {
        hrs > 0 -> "${hrs}h${mins}m"
        mins > 0 -> "${mins}m"
        else -> "${totalSecs}s"
    }
}

private fun getChStatus(status: String): String = when (status) {
    "RUNNING" -> "🐾 正在专注"
    "PAUSED" -> "💤 暂停中"
    "COMPLETED" -> "💮 已圆满完成"
    else -> "记录中"
}

private fun getPrettyDateHeader(dateString: String): String {
    return try {
        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdfIn.parse(dateString) ?: Date()
        val sdfOut = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)
        sdfOut.format(date)
    } catch (_: Exception) {
        dateString
    }
}

private fun getShortWeekDay(dateString: String): String {
    return try {
        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdfIn.parse(dateString) ?: Date()
        val sdfOut = SimpleDateFormat("E", Locale.CHINESE)
        sdfOut.format(date).replace("星期", "周")
    } catch (_: Exception) {
        "天"
    }
}
