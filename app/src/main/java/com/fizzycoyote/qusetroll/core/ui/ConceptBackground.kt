package com.fizzycoyote.qusetroll.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val BgDeep   = Color(0xFF030508)
private val BgMid    = Color(0xFF070B12)
private val BgBottom = Color(0xFF0A1220)

private val Gold       = Color(0xFFD4AF37)
private val GoldBright = Color(0xFFEDD56A)
private val GoldFaint  = Color(0xFFB8922A)
private val Silver     = Color(0xFFD8DDE8)
private val BlueGlow   = Color(0xFF3A5280)

private data class ThreadDef(
    val startFrac: Offset,
    val endFrac: Offset,
    val ctrl1Frac: Offset,
    val ctrl2Frac: Offset,
    val phase: Float,
    val speed: Float,
    val amplitude: Float,
    val isGold: Boolean,
    val strokeWidth: Float,
    val alpha: Float
)

private data class NodeDef(
    val posFrac: Offset,
    val phase: Float,
    val hasStar: Boolean,
    val isGold: Boolean
)

private val threadDefs: List<ThreadDef> = buildList {
    val rng = Random(137)
    // hero threads
    repeat(4) { i ->
        val startEdge = i % 4
        add(
            ThreadDef(
                startFrac   = edgeFrac(startEdge, rng.nextFloat()),
                endFrac     = edgeFrac((startEdge + 2) % 4, rng.nextFloat()),
                ctrl1Frac   = Offset(0.2f + rng.nextFloat() * 0.6f, rng.nextFloat()),
                ctrl2Frac   = Offset(0.2f + rng.nextFloat() * 0.6f, rng.nextFloat()),
                phase       = rng.nextFloat() * 2f * PI.toFloat(),
                speed       = 0.3f + rng.nextFloat() * 0.4f,
                amplitude   = 0.025f + rng.nextFloat() * 0.02f,
                isGold      = true,
                strokeWidth = 2.0f + rng.nextFloat() * 1.0f,
                alpha       = 0.18f + rng.nextFloat() * 0.1f
            )
        )
    }
    // medium threads
    repeat(8) {
        val startEdge = rng.nextInt(4)
        add(
            ThreadDef(
                startFrac   = edgeFrac(startEdge, rng.nextFloat()),
                endFrac     = edgeFrac((startEdge + 1 + rng.nextInt(2)) % 4, rng.nextFloat()),
                ctrl1Frac   = Offset(rng.nextFloat(), rng.nextFloat()),
                ctrl2Frac   = Offset(rng.nextFloat(), rng.nextFloat()),
                phase       = rng.nextFloat() * 2f * PI.toFloat(),
                speed       = 0.4f + rng.nextFloat() * 0.6f,
                amplitude   = 0.015f + rng.nextFloat() * 0.025f,
                isGold      = false,
                strokeWidth = 0.8f + rng.nextFloat() * 0.8f,
                alpha       = 0.07f + rng.nextFloat() * 0.06f
            )
        )
    }
    // 12 thin threads
    repeat(12) {
        val startEdge = rng.nextInt(4)
        add(
            ThreadDef(
                startFrac   = edgeFrac(startEdge, rng.nextFloat()),
                endFrac     = edgeFrac(rng.nextInt(4), rng.nextFloat()),
                ctrl1Frac   = Offset(rng.nextFloat(), rng.nextFloat()),
                ctrl2Frac   = Offset(rng.nextFloat(), rng.nextFloat()),
                phase       = rng.nextFloat() * 2f * PI.toFloat(),
                speed       = 0.5f + rng.nextFloat() * 1.0f,
                amplitude   = 0.008f + rng.nextFloat() * 0.015f,
                isGold      = rng.nextFloat() < 0.15f,
                strokeWidth = 0.4f + rng.nextFloat() * 0.5f,
                alpha       = 0.03f + rng.nextFloat() * 0.04f
            )
        )
    }
}

private fun edgeFrac(edge: Int, t: Float): Offset = when (edge) {
    0 -> Offset(t, 0f)   // top
    1 -> Offset(1f, t)   // right
    2 -> Offset(t, 1f)   // bottom
    else -> Offset(0f, t) // left
}

private val nodeDefs: List<NodeDef> = buildList {
    val rng = Random(999)
    repeat(12) { i ->
        add(
            NodeDef(
                posFrac  = Offset(0.05f + rng.nextFloat() * 0.9f, 0.05f + rng.nextFloat() * 0.9f),
                phase    = rng.nextFloat() * 2f * PI.toFloat(),
                hasStar  = i < 4,
                isGold   = i < 6
            )
        )
    }
}

// Noise
private val noisePoints: List<Offset> = buildList {
    val rng = Random(42)
    repeat(1800) {
        add(Offset(rng.nextFloat(), rng.nextFloat()))
    }
}

@Composable
fun ConceptBackground(modifier: Modifier = Modifier) {

    val transition = rememberInfiniteTransition(label = "threads_bg")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue  = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 80_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {

        drawRect(
            brush = Brush.verticalGradient(listOf(BgDeep, BgMid, BgBottom))
        )

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Gold.copy(alpha = 0.06f),
                    0.4f to Gold.copy(alpha = 0.02f),
                    1.0f to Color.Transparent
                )
            ),
            radius = size.minDimension * 0.9f,
            center = Offset(size.width * 0.5f, -size.height * 0.2f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to BlueGlow.copy(alpha = 0.04f),
                    1.0f to Color.Transparent
                )
            ),
            radius = size.minDimension * 1.1f,
            center = Offset(size.width * 0.5f, size.height * 1.1f)
        )

        threadDefs.forEach { t ->
            drawThread(t, phase, size.width, size.height)
        }

        nodeDefs.forEach { n ->
            drawNode(n, phase, size.width, size.height)
        }

        noisePoints.forEach { p ->
            val alpha = 0.006f + (p.x + p.y) % 0.008f
            drawCircle(
                color  = Color.White.copy(alpha = alpha),
                radius = 0.6f,
                center = Offset(size.width * p.x, size.height * p.y)
            )
        }

        drawRect(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color.Transparent,
                    0.7f to Color.Transparent,
                    1.0f to Color.Black.copy(alpha = 0.55f)
                )
            )
        )
    }
}

private fun DrawScope.drawThread(
    t: ThreadDef,
    time: Float,
    w: Float,
    h: Float
) {
    val wave = sin(time * t.speed + t.phase).toFloat()

    val dx = wave * t.amplitude * w
    val dy = cos(time * t.speed + t.phase + 1.0).toFloat() * t.amplitude * 0.6f * h

    val x0 = t.startFrac.x * w
    val y0 = t.startFrac.y * h
    val cx1 = (t.ctrl1Frac.x + t.amplitude * 0.5f * wave) * w + dx
    val cy1 = t.ctrl1Frac.y * h + dy
    val cx2 = (t.ctrl2Frac.x - t.amplitude * 0.5f * wave) * w - dx
    val cy2 = t.ctrl2Frac.y * h - dy
    val x1 = t.endFrac.x * w
    val y1 = t.endFrac.y * h

    val path = Path().apply {
        moveTo(x0, y0)
        cubicTo(cx1, cy1, cx2, cy2, x1, y1)
    }

    val color = if (t.isGold) Gold else Silver
    drawPath(
        path        = path,
        color       = color.copy(alpha = t.alpha),
        style       = Stroke(width = t.strokeWidth, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawNode(
    n: NodeDef,
    time: Float,
    w: Float,
    h: Float
) {
    val pulse = 0.5f + 0.5f * sin(time * 0.8f + n.phase).toFloat()
    val x = n.posFrac.x * w
    val y = n.posFrac.y * h
    val center = Offset(x, y)
    val color = if (n.isGold) Gold else Silver

    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to color.copy(alpha = 0.08f + pulse * 0.05f),
                1.0f to Color.Transparent
            ),
            center = center,
            radius = 80f
        ),
        radius = 80f,
        center = center
    )

    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to color.copy(alpha = 0.30f + pulse * 0.15f),
                1.0f to Color.Transparent
            ),
            center = center,
            radius = 20f
        ),
        radius = 20f,
        center = center
    )

    drawCircle(
        color  = if (n.isGold) GoldBright.copy(alpha = 0.85f) else Silver.copy(alpha = 0.7f),
        radius = 3.5f,
        center = center
    )

    if (n.hasStar) {
        drawSparkle(
            center    = center,
            size      = 14f + pulse * 6f,
            color     = if (n.isGold) GoldBright.copy(alpha = 0.6f + pulse * 0.2f)
            else Silver.copy(alpha = 0.5f + pulse * 0.2f)
        )
    }
}

private fun DrawScope.drawSparkle(
    center: Offset,
    size: Float,
    color: Color
) {
    val path = Path()
    val s = size
    val n = size * 0.18f

    val points = listOf(
        Offset(center.x,     center.y - s),
        Offset(center.x + n, center.y - n),
        Offset(center.x + s, center.y),
        Offset(center.x + n, center.y + n),
        Offset(center.x,     center.y + s),
        Offset(center.x - n, center.y + n),
        Offset(center.x - s, center.y),
        Offset(center.x - n, center.y - n)
    )

    path.moveTo(points[0].x, points[0].y)
    points.drop(1).forEach { path.lineTo(it.x, it.y) }
    path.close()

    drawPath(path = path, color = color, style = Fill)
}