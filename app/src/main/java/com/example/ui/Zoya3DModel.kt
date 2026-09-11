package com.example.ui

import com.example.live.ZoyaState
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive 3D Holographic Character Model of Zoya.
 * Features:
 * - 3D Perspective rotation with drag & tilt gesture tracking
 * - Natural breathing & floating idle physics
 * - Lip-sync mouth animations during speech transmission
 * - Floating blossom petals & ethereal aura particles
 * - Futuristic holographic projection disc base
 * - Tap interaction & reaction feedback
 */
@Composable
fun Zoya3DCharacterModel(
    state: ZoyaState,
    modifier: Modifier = Modifier,
    onAvatarClick: () -> Unit = {}
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // 3D Rotation Angles with physics spring return
    var targetRotY by remember { mutableFloatStateOf(0f) }
    var targetRotX by remember { mutableFloatStateOf(0f) }
    
    val animatedRotY by animateFloatAsState(
        targetValue = targetRotY,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 280f),
        label = "3dRotY"
    )
    val animatedRotX by animateFloatAsState(
        targetValue = targetRotX,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 280f),
        label = "3dRotX"
    )

    // Idle floating & breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "idle_motion")
    val floatingY by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingY"
    )
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.99f,
        targetValue = 1.018f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    val holoRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "holoDiscRotation"
    )

    // Speaking Lip-sync mouth flap
    var isMouthOpen by remember { mutableStateOf(false) }
    LaunchedEffect(state) {
        if (state == ZoyaState.SPEAKING) {
            while (true) {
                isMouthOpen = !isMouthOpen
                delay(180L)
            }
        } else {
            isMouthOpen = false
        }
    }

    // Tap feedback reaction (hearts / sparkles burst)
    var showHeartReaction by remember { mutableStateOf(false) }
    val reactionScale = remember { Animatable(0f) }

    val stateColor = when (state) {
        ZoyaState.IDLE -> Color(0xFFB388FF)
        ZoyaState.LISTENING -> Color(0xFF00E5FF)
        ZoyaState.THINKING -> Color(0xFFFFAB40)
        ZoyaState.SPEAKING -> Color(0xFF69F0AE)
        else -> Color(0xFF90CAF9)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Hologram 3D Stage Box - Does not intercept vertical drag, allowing full screen scroll
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 280.dp, height = 310.dp)
        ) {
            // 1. Ambient Hologram Backglow
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2f, size.height * 0.48f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            stateColor.copy(alpha = if (state == ZoyaState.SPEAKING) 0.38f else 0.22f),
                            stateColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.7f,
                    center = centerOffset
                )
            }

            // 2. Floating Blossom Petals Canvas (matching video)
            BlossomParticlesCanvas(
                modifier = Modifier.fillMaxSize(),
                primaryColor = stateColor
            )

            // 3. The 3D Character Model Card with Real Perspective Transformation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(210.dp)
                    .height(260.dp)
                    .offset(y = floatingY.dp)
                    .graphicsLayer {
                        rotationY = animatedRotY
                        rotationX = animatedRotX
                        scaleX = breathScale
                        scaleY = breathScale
                        cameraDistance = 16f * density.density
                        shadowElevation = 20.dp.toPx()
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        BorderStroke(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.45f),
                                    stateColor.copy(alpha = 0.6f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .background(Color(0xFF141324))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onAvatarClick()
                                scope.launch {
                                    showHeartReaction = true
                                    reactionScale.snapTo(0f)
                                    reactionScale.animateTo(
                                        targetValue = 1.2f,
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    )
                                    reactionScale.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(250, easing = LinearEasing)
                                    )
                                    showHeartReaction = false
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                targetRotY = 0f
                                targetRotX = 0f
                            },
                            onDragCancel = {
                                targetRotY = 0f
                                targetRotX = 0f
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                targetRotY = (targetRotY + dragAmount * 0.45f).coerceIn(-32f, 32f)
                            }
                        )
                    }
            ) {
                // Character Texture Layer
                val imageRes = if (state == ZoyaState.SPEAKING && isMouthOpen) {
                    R.drawable.zoya_avatar_speak
                } else {
                    R.drawable.zoya_avatar_idle
                }

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Zoya 3D Anime Model",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Holographic scanlines & depth lighting overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Soft bottom gradient shadow for depth
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.45f)
                            ),
                            startY = size.height * 0.65f,
                            endY = size.height
                        )
                    )

                    // 3D Glass edge highlight
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent)
                        ),
                        start = Offset(0f, 4f),
                        end = Offset(size.width, 4f),
                        strokeWidth = 2f
                    )
                }

                // Heart / Star reaction popup when tapped
                if (showHeartReaction) {
                    Text(
                        text = "💜 ✨",
                        fontSize = 32.sp,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = reactionScale.value
                                scaleY = reactionScale.value
                                alpha = reactionScale.value.coerceIn(0f, 1f)
                            }
                            .align(Alignment.TopCenter)
                            .offset(y = 40.dp)
                    )
                }

                // Live Audio Waveform bar overlay when speaking
                if (state == ZoyaState.SPEAKING) {
                    MiniWaveformBadge(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    )
                }
            }

            // 4. Futuristic Holographic Pedestal Disc at the Base
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 12.dp)
            ) {
                val center = Offset(size.width / 2f, size.height * 0.55f)
                val discWidth = size.width * 0.72f
                val discHeight = size.height * 0.45f

                // Outer holographic ring
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(stateColor.copy(alpha = 0.7f), Color.Transparent),
                        center = center,
                        radius = discWidth / 2f
                    ),
                    topLeft = Offset(center.x - discWidth / 2f, center.y - discHeight / 2f),
                    size = Size(discWidth, discHeight),
                    style = Stroke(width = 2.5f)
                )

                // Inner luminous core oval
                val innerWidth = discWidth * 0.55f
                val innerHeight = discHeight * 0.55f
                drawOval(
                    color = stateColor.copy(alpha = 0.4f),
                    topLeft = Offset(center.x - innerWidth / 2f, center.y - innerHeight / 2f),
                    size = Size(innerWidth, innerHeight),
                    style = Stroke(width = 1.8f)
                )

                // Rotating holographic beam tick marks
                rotate(holoRotation, pivot = center) {
                    for (i in 0 until 12) {
                        val angle = Math.toRadians((i * 30).toDouble())
                        val rX = (discWidth / 2f * 0.88f) * cos(angle).toFloat()
                        val rY = (discHeight / 2f * 0.88f) * sin(angle).toFloat()
                        drawCircle(
                            color = Color.White.copy(alpha = 0.75f),
                            radius = 2f,
                            center = Offset(center.x + rX, center.y + rY)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State indicator pill badge & interaction hint
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                .border(1.dp, stateColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(stateColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (state) {
                    ZoyaState.IDLE -> "Zoya • 3D Avatar Ready"
                    ZoyaState.LISTENING -> "Zoya • Listening..."
                    ZoyaState.THINKING -> "Zoya • Analyzing..."
                    ZoyaState.SPEAKING -> "Zoya • Speaking..."
                    else -> "Zoya • Standby"
                },
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Drag to rotate 3D view • Tap avatar to interact",
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 11.sp,
            letterSpacing = 0.3.sp
        )
    }
}

/**
 * Animated floating blossom petals & fairy dust particles matching the video.
 */
@Composable
private fun BlossomParticlesCanvas(
    modifier: Modifier = Modifier,
    primaryColor: Color
) {
    val transition = rememberInfiniteTransition(label = "petals")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "petalTime"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val petalCount = 14

        for (i in 0 until petalCount) {
            val seed = i * 47.3f
            val speed = 0.6f + (i % 5) * 0.25f
            val yProgress = ((time * speed + seed * 10f) % (h + 80f))
            val currentY = (h + 40f) - yProgress
            val wave = sin((time * 0.05f + seed).toDouble()).toFloat() * 24f
            val currentX = ((seed * 37f + wave) % (w - 40f)) + 20f

            val petalSize = 8f + (i % 4) * 4f
            val rotAngle = (time * 1.5f + seed * 15f) % 360f
            val alpha = (sin((currentY / h * Math.PI).toDouble()).toFloat()).coerceIn(0.15f, 0.75f)

            // Lavender blossom petal shape
            val petalColor = if (i % 2 == 0) {
                Color(0xFFE1BEE7).copy(alpha = alpha) // Lavender
            } else {
                Color(0xFFF8BBD0).copy(alpha = alpha * 0.85f) // Sakura pink
            }

            rotate(rotAngle, pivot = Offset(currentX, currentY)) {
                // Draw delicate 4-petal blossom or single fluttering petal
                if (i % 3 == 0) {
                    // Small floating 4-petal flower
                    val rad = petalSize * 0.5f
                    drawCircle(petalColor, radius = rad, center = Offset(currentX - rad, currentY))
                    drawCircle(petalColor, radius = rad, center = Offset(currentX + rad, currentY))
                    drawCircle(petalColor, radius = rad, center = Offset(currentX, currentY - rad))
                    drawCircle(petalColor, radius = rad, center = Offset(currentX, currentY + rad))
                    drawCircle(Color.White.copy(alpha = alpha), radius = rad * 0.4f, center = Offset(currentX, currentY))
                } else {
                    // Fluttering curved oval petal
                    val path = Path().apply {
                        moveTo(currentX, currentY - petalSize)
                        quadraticTo(currentX + petalSize * 0.8f, currentY, currentX, currentY + petalSize)
                        quadraticTo(currentX - petalSize * 0.8f, currentY, currentX, currentY - petalSize)
                        close()
                    }
                    drawPath(path, petalColor)
                }
            }
        }
    }
}

/**
 * Animated live audio waveform badge displayed during speaking.
 */
@Composable
private fun MiniWaveformBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(220, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(180, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(260, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b4"
    )

    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF69F0AE).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val bars = listOf(bar1, bar2, bar3, bar4, bar2)
        bars.forEach { heightFactor ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((16 * heightFactor).dp.coerceAtLeast(4.dp))
                    .background(Color(0xFF69F0AE), RoundedCornerShape(2.dp))
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "LIVE AUDIO",
            color = Color(0xFF69F0AE),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
