package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PencilGraphite
import com.example.ui.theme.PencilGray
import kotlin.math.cos
import kotlin.math.sin

enum class CatState {
    IDLE,
    RUNNING,
    PAUSED,
    CELEBRATING
}

@Composable
fun CuteCatCompanion(
    state: CatState,
    modifier: Modifier = Modifier,
    customMessage: String? = null,
    onClick: () -> Unit = {}
) {
    // Infinite transition for ambient animations (breathing, tail wag, Zzz)
    val infiniteTransition = rememberInfiniteTransition(label = "KittenAmbientMap")
    
    // Breathing scale: slightly change height/width ratio
    val breathingValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Breathing"
    )
    val breathingScaling = 1f + (sin(breathingValue) * 0.025f)

    // Tail wag angle
    val tailWagValue by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TailWag"
    )

    // Zzz float up
    val zzzOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "ZzzFloat"
    )

    // Interactive speech bubble dialogues
    val idleDialogues = listOf(
        "今天也要好好生活，本喵会在旁边一直陪着你哦，喵～",
        "时间手账像魔法书一样，记录的都是你努力的瞬间，喵！",
        "戳我干嘛？手账本还没写满呢，快去给今天加点彩色贴纸吧！",
        "喵～认真做完一件事，奖励自己一个盖章吧！",
        "时间会走，每一张小狗小猫标签都是发光的记忆，喵～",
        "呼噜呼噜……觉得充实的时候，小猫的毛都会变得松软，咪～"
    )

    val pauseDialogues = listOf(
        "喵～你暂停啦，我在这里等你回来。",
        "休息一下也没关系，斜躺着看你写字，喵。",
        "准备好了就继续吧，本喵已经伸好懒腰啦！"
    )

    val runningDialogues = listOf(
        "啪嗒啪嗒在超级专注吗？不要分心哦，本喵真挚地盯着你呢！",
        "加油冲刺！正在全力专注，努力的你超闪亮，喵！",
        "我们在跟时间赛跑，喵！我可以用小爪帮你在书页按个红印章！"
    )

    val celebrateDialogues = listOf(
        "呜哇！完成了！太赞啦喵，给你盖个章！",
        "小猫认证：努力值 +100！给你一张超棒的金枪鱼勋章，喵呜！",
        "哇！今天的生活好充实呀，本喵举双手双脚赞赏，喵呜！"
    )

    var clickTriggerCount by remember { mutableStateOf(0) }
    val displayedMessage = remember(state, clickTriggerCount, customMessage) {
        if (customMessage != null) return@remember customMessage
        when (state) {
            CatState.IDLE -> idleDialogues[(clickTriggerCount) % idleDialogues.size]
            CatState.PAUSED -> pauseDialogues[(clickTriggerCount) % pauseDialogues.size]
            CatState.RUNNING -> runningDialogues[(clickTriggerCount) % runningDialogues.size]
            CatState.CELEBRATING -> celebrateDialogues[(clickTriggerCount) % celebrateDialogues.size]
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                clickTriggerCount++
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speech Bubble inside open book
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .background(
                    color = Color(0xFFFFFDF5),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 220.dp)
        ) {
            Text(
                text = displayedMessage,
                color = PencilGraphite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Small bubble stem
        Canvas(
            modifier = Modifier
                .size(12.dp, 6.dp)
                .offset(y = (-3).dp)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f - 4f, 0f)
                lineTo(size.width / 2f + 4f, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(path, color = Color(0xFFFFFDF5))
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Cute Canvas Cat Renderer with low-saturation cream palette
        Box(
            modifier = Modifier
                .size(110.dp, 95.dp)
                .graphicsLayer(
                    scaleY = if (state == CatState.PAUSED) 1.02f else breathingScaling,
                    scaleX = if (state == CatState.PAUSED) 0.98f else (2f - breathingScaling)
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h * 0.58f // Center of body

                // Low-saturation Matte Colors
                val catCream = Color(0xFFFAF6EE)       // Soft Cream color from image.png
                val catFawn = Color(0xFFE5D2BA)        // Low-saturation latte beige
                val catDullRed = Color(0xFFE57373)     // Tomato dull red
                val catPeachPink = Color(0xFFFCD7D9)   // Pale soft pink
                val outlineGraphite = PencilGraphite   // Solid clean outline

                // 1. DRAW TAIL
                val tailPath = Path()
                if (state == CatState.PAUSED) {
                    // Lazy curl tail
                    tailPath.moveTo(cx + 20f, cy + 15f)
                    tailPath.quadraticTo(cx + 35f, cy + 20f, cx + 45f, cy + 2f)
                    tailPath.quadraticTo(cx + 50f, cy - 8f, cx + 42f, cy - 12f)
                    tailPath.quadraticTo(cx + 35f, cy - 10f, cx + 38f, cy)
                    tailPath.quadraticTo(cx + 35f, cy + 10f, cx + 20f, cy + 12f)
                    drawPath(tailPath, color = catFawn)
                    drawPath(tailPath, color = outlineGraphite, style = Stroke(width = 2f))
                } else {
                    // Wagging tail
                    val wagRad = Math.toRadians(tailWagValue.toDouble()).toFloat()
                    val tailEnd = Offset(
                        cx + 40f + sin(wagRad) * 32f,
                        cy + 15f - cos(wagRad) * 35f
                    )
                    tailPath.moveTo(cx + 15f, cy + 18f)
                    tailPath.quadraticTo(cx + 28f, cy + 8f, tailEnd.x, tailEnd.y)
                    drawPath(
                        path = tailPath,
                        color = catFawn,
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = tailPath,
                        color = outlineGraphite,
                        style = Stroke(width = 2f, cap = StrokeCap.Round)
                    )
                }

                // 2. DRAW CHUBBY BODY
                drawCircle(
                    color = catCream,
                    radius = 38f,
                    center = Offset(cx, cy + 8f)
                )
                drawCircle(
                    color = outlineGraphite,
                    radius = 38f,
                    center = Offset(cx, cy + 8f),
                    style = Stroke(width = 2.5f)
                )

                // White patch on chest
                drawArc(
                    color = Color.White,
                    startAngle = 30f,
                    sweepAngle = 120f,
                    useCenter = true,
                    topLeft = Offset(cx - 22f, cy - 10f),
                    size = Size(44f, 44f)
                )

                // 3. DRAW ROUND HEAD
                val headX = cx
                val headY = cy - 25f
                val headR = 35f

                // Ears
                val leftEar = Path().apply {
                    moveTo(headX - headR + 4f, headY)
                    lineTo(headX - headR - 6f, headY - 25f)
                    lineTo(headX - 8f, headY - 18f)
                    close()
                }
                val leftEarPink = Path().apply {
                    moveTo(headX - headR + 8f, headY - 2f)
                    lineTo(headX - headR, headY - 18f)
                    lineTo(headX - 11f, headY - 14f)
                    close()
                }
                val rightEar = Path().apply {
                    moveTo(headX + headR - 4f, headY)
                    lineTo(headX + headR + 6f, headY - 25f)
                    lineTo(headX + 8f, headY - 18f)
                    close()
                }
                val rightEarPink = Path().apply {
                    moveTo(headX + headR - 8f, headY - 2f)
                    lineTo(headX + headR, headY - 18f)
                    lineTo(headX + 11f, headY - 14f)
                    close()
                }

                drawPath(leftEar, color = catFawn)
                drawPath(leftEar, color = outlineGraphite, style = Stroke(width = 2.5f))
                drawPath(leftEarPink, color = catPeachPink)

                drawPath(rightEar, color = catFawn)
                drawPath(rightEar, color = outlineGraphite, style = Stroke(width = 2.5f))
                drawPath(rightEarPink, color = catPeachPink)

                // Head ellipse
                drawCircle(
                    color = catCream,
                    radius = headR,
                    center = Offset(headX, headY)
                )
                drawCircle(
                    color = outlineGraphite,
                    radius = headR,
                    center = Offset(headX, headY),
                    style = Stroke(width = 2.5f)
                )

                // Fluffy side curves details represent the first photo style
                drawArc(
                    color = catFawn,
                    startAngle = 150f,
                    sweepAngle = 60f,
                    useCenter = true,
                    topLeft = Offset(headX - headR, headY - headR),
                    size = Size(headR * 2, headR * 2)
                )

                // 4. FACE DETAILS
                if (state == CatState.PAUSED) {
                    // Sleepy eyes
                    drawArc(
                        color = outlineGraphite,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(headX - 16f, headY - 2f),
                        size = Size(8f, 5f),
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = outlineGraphite,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(headX + 8f, headY - 2f),
                        size = Size(8f, 5f),
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                    )
                    // Nose
                    drawCircle(color = catPeachPink, radius = 2.5f, center = Offset(headX, headY + 5f))

                    // Sleepy Zzz bubble
                    val zX = headX + 35f
                    val zY = headY - 10f + zzzOffset
                    drawZzz(this, zX, zY, size = 8f)
                    drawZzz(this, zX + 12f, zY - 12f, size = 12f)
                } else if (state == CatState.CELEBRATING) {
                    // Starry eyes
                    drawStarEye(this, headX - 10f, headY - 2f, size = 5f)
                    drawStarEye(this, headX + 10f, headY - 2f, size = 5f)

                    // Large pink plush cheeks blush
                    drawCircle(color = catPeachPink, radius = 5.5f, center = Offset(headX - 18f, headY + 6f))
                    drawCircle(color = catPeachPink, radius = 5.5f, center = Offset(headX + 18f, headY + 6f))

                    // Laughing open mouth
                    val mouthP = Path().apply {
                        moveTo(headX - 3.5f, headY + 4f)
                        quadraticTo(headX, headY + 3f, headX + 3.5f, headY + 4f)
                        quadraticTo(headX, headY + 11f, headX - 3.5f, headY + 4f)
                    }
                    drawPath(mouthP, color = outlineGraphite)
                } else {
                    // Default warm round eyes
                    drawCircle(color = outlineGraphite, radius = 3.2f, center = Offset(headX - 11f, headY - 2f))
                    drawCircle(color = outlineGraphite, radius = 3.2f, center = Offset(headX + 11f, headY - 2f))

                    // Delicate low-saturation pink blush
                    drawCircle(color = catPeachPink.copy(alpha = 0.8f), radius = 5f, center = Offset(headX - 16f, headY + 5f))
                    drawCircle(color = catPeachPink.copy(alpha = 0.8f), radius = 5f, center = Offset(headX + 16f, headY + 5f))

                    // 'w' mouth
                    drawCuteMouth(this, headX, headY + 3f)
                }

                // Whiskers
                drawLine(color = outlineGraphite.copy(alpha = 0.7f), start = Offset(headX - headR + 2f, headY + 4f), end = Offset(headX - headR - 8f, headY + 3f), strokeWidth = 2f)
                drawLine(color = outlineGraphite.copy(alpha = 0.7f), start = Offset(headX - headR + 2f, headY + 9f), end = Offset(headX - headR - 6f, headY + 10f), strokeWidth = 2f)
                drawLine(color = outlineGraphite.copy(alpha = 0.7f), start = Offset(headX + headR - 2f, headY + 4f), end = Offset(headX + headR + 8f, headY + 3f), strokeWidth = 2f)
                drawLine(color = outlineGraphite.copy(alpha = 0.7f), start = Offset(headX + headR - 2f, headY + 9f), end = Offset(headX + headR + 6f, headY + 10f), strokeWidth = 2f)

                // Hands waving in front
                if (state == CatState.RUNNING) {
                    // cute typing hands
                    drawCircle(color = catCream, radius = 5f, center = Offset(cx - 10f, cy + 26f))
                    drawCircle(color = catCream, radius = 5f, center = Offset(cx + 10f, cy + 26f))
                } else {
                    drawCircle(color = catCream, radius = 5.5f, center = Offset(cx - 24f, cy + 20f))
                    drawCircle(color = catCream, radius = 5.5f, center = Offset(cx + 24f, cy + 20f))
                }
            }
        }
    }
}

@Composable
fun MiniCatCategoryIcon(
    category: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.36f // Chubby head radius

        // Matches the low-saturation, chalk-crayon textures of image.png
        val creamWhite = Color(0xFFFFFFEE)     // Low-saturation butter white
        val paleGinger = Color(0xFFF0D6B1)     // Soft tan/bear brown
        val softGrey = Color(0xFFCFD8DC)       // Cozy slate grey
        val tomatoRed = Color(0xFFE56E6E)      // Warm faded tomato
        val brushPink = Color(0xFFFFCBD2)      // Light blush pink
        val charcoalOutline = PencilGraphite  // Pencil grey stroke

        // Draw chubby kitty head shape
        drawCircle(
            color = creamWhite,
            radius = r,
            center = Offset(cx, cy + h * 0.05f)
        )
        drawCircle(
            color = charcoalOutline,
            radius = r,
            center = Offset(cx, cy + h * 0.05f),
            style = Stroke(width = 1.8f)
        )

        // Pointy ears
        val earL = Path().apply {
            moveTo(cx - r * 0.7f, cy - r * 0.5f)
            lineTo(cx - r * 1.15f, cy - r * 1.1f)
            lineTo(cx - r * 0.15f, cy - r * 0.82f)
            close()
        }
        val earR = Path().apply {
            moveTo(cx + r * 0.7f, cy - r * 0.5f)
            lineTo(cx + r * 1.15f, cy - r * 1.1f)
            lineTo(cx + r * 0.15f, cy - r * 0.82f)
            close()
        }

        drawPath(earL, color = paleGinger)
        drawPath(earL, color = charcoalOutline, style = Stroke(width = 1.8f))
        drawPath(earR, color = paleGinger)
        drawPath(earR, color = charcoalOutline, style = Stroke(width = 1.8f))

        // Inner Pink Ears
        val innerEarL = Path().apply {
            moveTo(cx - r * 0.6f, cy - r * 0.55f)
            lineTo(cx - r * 0.95f, cy - r * 0.95f)
            lineTo(cx - r * 0.25f, cy - r * 0.75f)
            close()
        }
        val innerEarR = Path().apply {
            moveTo(cx + r * 0.6f, cy - r * 0.55f)
            lineTo(cx + r * 0.95f, cy - r * 0.95f)
            lineTo(cx + r * 0.25f, cy - r * 0.75f)
            close()
        }
        drawPath(innerEarL, color = brushPink)
        drawPath(innerEarR, color = brushPink)

        // Left/Right spots like student or waiter cat
        if (category == "SPORT" || category == "REST") {
            // Draw gray cow spots
            drawArc(
                color = softGrey,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(cx - r, cy - r * 0.8f),
                size = Size(r * 0.9f, r * 0.9f)
            )
        } else {
            // Draw ginger brow stripes
            drawRoundRect(
                color = paleGinger,
                topLeft = Offset(cx - 3.dp.toPx(), cy - r + 1.dp.toPx()),
                size = Size(6.dp.toPx(), 4.dp.toPx()),
                cornerRadius = CornerRadius(1.dp.toPx())
            )
        }

        // Tiny widely-spaced black eyes (very minimalist, like image.png)
        drawCircle(color = charcoalOutline, radius = 1.8f.dp.toPx(), center = Offset(cx - r * 0.44f, cy + h * 0.08f))
        drawCircle(color = charcoalOutline, radius = 1.8f.dp.toPx(), center = Offset(cx + r * 0.44f, cy + h * 0.08f))

        // Small red nose
        drawCircle(color = tomatoRed, radius = 1.5f.dp.toPx(), center = Offset(cx, cy + h * 0.15f))

        // Gentle Pink Blush
        drawCircle(color = brushPink.copy(alpha = 0.85f), radius = 3.dp.toPx(), center = Offset(cx - r * 0.6f, cy + h * 0.2f))
        drawCircle(color = brushPink.copy(alpha = 0.85f), radius = 3.dp.toPx(), center = Offset(cx + r * 0.6f, cy + h * 0.2f))

        // Whiskers
        drawLine(color = charcoalOutline.copy(alpha = 0.5f), start = Offset(cx - r * 0.8f, cy + h * 0.14f), end = Offset(cx - r * 1.15f, cy + h * 0.11f), strokeWidth = 1.5f)
        drawLine(color = charcoalOutline.copy(alpha = 0.5f), start = Offset(cx - r * 0.8f, cy + h * 0.21f), end = Offset(cx - r * 1.05f, cy + h * 0.23f), strokeWidth = 1.5f)
        drawLine(color = charcoalOutline.copy(alpha = 0.5f), start = Offset(cx + r * 0.8f, cy + h * 0.14f), end = Offset(cx + r * 1.15f, cy + h * 0.11f), strokeWidth = 1.5f)
        drawLine(color = charcoalOutline.copy(alpha = 0.5f), start = Offset(cx + r * 0.8f, cy + h * 0.21f), end = Offset(cx + r * 1.05f, cy + h * 0.23f), strokeWidth = 1.5f)

        // Small 'w' mouse
        val wY = cy + h * 0.18f
        drawArc(
            color = charcoalOutline,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - 3.dp.toPx(), wY),
            size = Size(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = 1.5f, cap = StrokeCap.Round)
        )
        drawArc(
            color = charcoalOutline,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx, wY),
            size = Size(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = 1.5f, cap = StrokeCap.Round)
        )

        // Custom hats attached according to the category
        when (category) {
            "STUDY" -> {
                // Student: Glasses + Tiny blue frame
                val glassBlue = Color(0xFF64B5F6)
                drawCircle(
                    color = glassBlue,
                    radius = 4.dp.toPx(),
                    center = Offset(cx - r * 0.44f, cy + h * 0.08f),
                    style = Stroke(width = 1.5f.dp.toPx())
                )
                drawCircle(
                    color = glassBlue,
                    radius = 4.dp.toPx(),
                    center = Offset(cx + r * 0.44f, cy + h * 0.08f),
                    style = Stroke(width = 1.5f.dp.toPx())
                )
                drawLine(
                    color = glassBlue,
                    start = Offset(cx - r * 0.22f, cy + h * 0.08f),
                    end = Offset(cx + r * 0.22f, cy + h * 0.08f),
                    strokeWidth = 1.5f.dp.toPx()
                )
            }
            "CODE" -> {
                // DJ: Cyan earcups headphones + red headband
                val headCyan = Color(0xFF26C6DA)
                // Draw ear cups on sides
                drawRoundRect(
                    color = headCyan,
                    topLeft = Offset(cx - r - 3.dp.toPx(), cy + h * 0.02f),
                    size = Size(5.dp.toPx(), 8.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                drawRoundRect(
                    color = headCyan,
                    topLeft = Offset(cx + r - 2.dp.toPx(), cy + h * 0.02f),
                    size = Size(5.dp.toPx(), 8.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                // Band
                drawArc(
                    color = tomatoRed,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r * 0.85f),
                    size = Size(r * 2f, r * 1.5f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            "READING" -> {
                // Painter: Tomato red painter beret
                val berColor = tomatoRed
                drawArc(
                    color = berColor,
                    startAngle = 170f,
                    sweepAngle = 200f,
                    useCenter = true,
                    topLeft = Offset(cx - r * 1.05f, cy - r * 1.1f),
                    size = Size(r * 2.1f, r * 0.75f)
                )
                drawArc(
                    color = charcoalOutline,
                    startAngle = 170f,
                    sweepAngle = 200f,
                    useCenter = true,
                    topLeft = Offset(cx - r * 1.05f, cy - r * 1.1f),
                    size = Size(r * 2.1f, r * 0.75f),
                    style = Stroke(width = 1.5f)
                )
                // Little beret cherry ribbon knot on top
                drawCircle(color = charcoalOutline, radius = 2.dp.toPx(), center = Offset(cx - 1.dp.toPx(), cy - r * 1.08f))
            }
            "REST" -> {
                // Chef: Tall puffed white chef hat
                val cPath = Path().apply {
                    moveTo(cx - r * 0.45f, cy - r * 0.55f)
                    quadraticTo(cx - r * 0.7f, cy - r * 1.1f, cx - r * 0.35f, cy - r * 1.1f)
                    quadraticTo(cx, cy - r * 1.25f, cx + r * 0.35f, cy - r * 1.1f)
                    quadraticTo(cx + r * 0.7f, cy - r * 1.1f, cx + r * 0.45f, cy - r * 0.55f)
                    close()
                }
                drawPath(cPath, color = Color.White)
                drawPath(cPath, color = charcoalOutline, style = Stroke(width = 1.5f))
                // white band
                drawRoundRect(
                    color = Color(0xFFEEEEEE),
                    topLeft = Offset(cx - r * 0.45f, cy - r * 0.65f),
                    size = Size(r * 0.9f, 4.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx())
                )
                drawRoundRect(
                    color = charcoalOutline,
                    topLeft = Offset(cx - r * 0.45f, cy - r * 0.65f),
                    size = Size(r * 0.9f, 4.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx()),
                    style = Stroke(width = 1f)
                )
            }
            "EXERCISE" -> {
                // Rider/Meituan: Yellow visor helmet
                val helColor = Color(0xFFFFCA28)
                drawArc(
                    color = helColor,
                    startAngle = 185f,
                    sweepAngle = 170f,
                    useCenter = true,
                    topLeft = Offset(cx - r * 0.8f, cy - r * 0.9f),
                    size = Size(r * 1.6f, r * 1.0f)
                )
                drawArc(
                    color = charcoalOutline,
                    startAngle = 185f,
                    sweepAngle = 170f,
                    useCenter = true,
                    topLeft = Offset(cx - r * 0.8f, cy - r * 0.9f),
                    size = Size(r * 1.6f, r * 1.0f),
                    style = Stroke(width = 1.5f)
                )
                // Black screen details
                drawRoundRect(
                    color = charcoalOutline,
                    topLeft = Offset(cx - r * 0.6f, cy - r * 0.5f),
                    size = Size(r * 1.2f, 3.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx())
                )
            }
            else -> {
                // Soft pink bow tie / head patch
                val bowColor = Color(0xFFF48FB1)
                val bLeft = Path().apply {
                    moveTo(cx, cy - r * 0.6f)
                    lineTo(cx - 7.dp.toPx(), cy - r * 0.75f)
                    lineTo(cx - 7.dp.toPx(), cy - r * 0.45f)
                }
                val bRight = Path().apply {
                    moveTo(cx, cy - r * 0.6f)
                    lineTo(cx + 7.dp.toPx(), cy - r * 0.75f)
                    lineTo(cx + 7.dp.toPx(), cy - r * 0.45f)
                }
                drawPath(bLeft, color = bowColor)
                drawPath(bLeft, color = charcoalOutline, style = Stroke(width = 1.2f))
                drawPath(bRight, color = bowColor)
                drawPath(bRight, color = charcoalOutline, style = Stroke(width = 1.2f))
                drawCircle(color = tomatoRed, radius = 2.dp.toPx(), center = Offset(cx, cy - r * 0.6f))
            }
        }
    }
}

private fun drawCuteMouth(scope: androidx.compose.ui.graphics.drawscope.DrawScope, x: Float, y: Float) {
    scope.drawArc(
        color = PencilGraphite,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(x - 5f, y),
        size = Size(5f, 5f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )
    scope.drawArc(
        color = PencilGraphite,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(x, y),
        size = Size(5f, 5f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )
}

private fun drawStarEye(scope: androidx.compose.ui.graphics.drawscope.DrawScope, x: Float, y: Float, size: Float) {
    scope.drawLine(color = PencilGraphite, start = Offset(x - size, y), end = Offset(x + size, y), strokeWidth = 2.5f, cap = StrokeCap.Round)
    scope.drawLine(color = PencilGraphite, start = Offset(x, y - size), end = Offset(x, y + size), strokeWidth = 2.5f, cap = StrokeCap.Round)
    scope.drawLine(color = PencilGraphite, start = Offset(x - size * 0.7f, y - size * 0.7f), end = Offset(x + size * 0.7f, y + size * 0.7f), strokeWidth = 1.8f, cap = StrokeCap.Round)
    scope.drawLine(color = PencilGraphite, start = Offset(x - size * 0.7f, y + size * 0.7f), end = Offset(x + size * 0.7f, y - size * 0.7f), strokeWidth = 1.8f, cap = StrokeCap.Round)
}

private fun drawZzz(scope: androidx.compose.ui.graphics.drawscope.DrawScope, x: Float, y: Float, size: Float) {
    scope.drawLine(color = PencilGray.copy(alpha = 0.8f), start = Offset(x, y), end = Offset(x + size, y), strokeWidth = 1.8f, cap = StrokeCap.Round)
    scope.drawLine(color = PencilGray.copy(alpha = 0.8f), start = Offset(x + size, y), end = Offset(x, y + size), strokeWidth = 1.8f, cap = StrokeCap.Round)
    scope.drawLine(color = PencilGray.copy(alpha = 0.8f), start = Offset(x, y + size), end = Offset(x + size, y + size), strokeWidth = 1.8f, cap = StrokeCap.Round)
}
