package com.fizzycoyote.qusetroll.core.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ThreadsBackgroundView extends View {

    // ========== KONFIGURACJA (dopracowana wizualnie) ==========

    // Nici
    private static final int   THREAD_COUNT       = 5;        // więcej nici, gęściej
    private static final float LINE_STROKE_BASE   = 0.5f;      // grubość białej (dp)
    private static final float GOLD_STROKE_BASE   = 0.7f;      // grubość złotej (dp)
    private static final float LINE_ALPHA         = 0.28f;     // przezroczystość białych
    private static final float GOLD_LINE_ALPHA    = 0.34f;     // złote nieco bardziej widoczne

    // Glow (poświata)
    private static final float GLOW_BLUR_RADIUS   = 24f;       // duży rozmycie (dp)
    private static final float GLOW_STROKE_FACTOR = 4.2f;      // grubość glow (mnożnik)
    private static final float GLOW_ALPHA         = 0.48f;     // intensywność blasku (0-1)

    // Węzły (miejsca świecenia na nitkach)
    private static final float NODE_RADIUS        = 9f;        // promień (dp)
    private static final float NODE_GLOW_RADIUS   = 32f;       // rozmycie blasku węzła (dp)
    private static final float NODE_ALPHA         = 0.92f;     // intensywność

    // Gwiazdy (tło)
    private static final int   STAR_COUNT         = 80;        // dużo gwiazd
    private static final float STAR_RADIUS_BASE   = 2.0f;      // minimalny promień (dp)
    private static final float STAR_RADIUS_VAR    = 2.5f;      // losowy dodatek (dp)
    private static final float STAR_ALPHA_BASE    = 0.5f;      // przezroczystość bazowa
    private static final float STAR_ALPHA_VAR     = 0.4f;      // zakres zmienności
    private static final float STAR_GLOW_RADIUS   = 16f;       // blask gwiazdy (dp)
    private static final float STAR_SPEED_MIN     = 0.4f;
    private static final float STAR_SPEED_MAX     = 1.8f;

    // Animacja
    private static final long  ANIM_DURATION_MS   = 20000;     // wolniejszy płynny ruch

    // ========== Pędzle ==========
    private Paint linePaint, lineGlowPaint;
    private Paint goldPaint, goldGlowPaint;
    private Paint nodePaint;
    private Paint starPaint;

    // ========== Struktury danych ==========
    private static class ThreadLine {
        float x0, y0, cx1, cy1, cx2, cy2, x1, y1;
        float amplitude, phase, speed;
        boolean isGold;
        float nodeT;
    }
    private static class Star {
        float x, y, radius, alpha, speed, phase;
    }

    private final List<ThreadLine> threads = new ArrayList<>();
    private final List<Star> stars = new ArrayList<>();
    private final Random random = new Random(42);
    private int width, height;
    private float animProgress;
    private ValueAnimator animator;

    // ========== Konstruktory ==========
    public ThreadsBackgroundView(Context context) {
        super(context);
        init();
    }
    public ThreadsBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ThreadsBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;

        // Główne linie (cienkie, ostre)
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(LINE_STROKE_BASE * density);
        linePaint.setColor(Color.WHITE);
        linePaint.setAlpha((int)(LINE_ALPHA * 255));
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        goldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goldPaint.setStyle(Paint.Style.STROKE);
        goldPaint.setStrokeWidth(GOLD_STROKE_BASE * density);
        goldPaint.setColor(Color.parseColor("#E5B83C"));
        goldPaint.setAlpha((int)(GOLD_LINE_ALPHA * 255));
        goldPaint.setStrokeCap(Paint.Cap.ROUND);

        // Glow (rozmyte, grube)
        lineGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lineGlowPaint.setStyle(Paint.Style.STROKE);
        lineGlowPaint.setStrokeWidth(LINE_STROKE_BASE * GLOW_STROKE_FACTOR * density);
        lineGlowPaint.setColor(Color.WHITE);
        lineGlowPaint.setAlpha((int)(LINE_ALPHA * GLOW_ALPHA * 255));
        lineGlowPaint.setStrokeCap(Paint.Cap.ROUND);
        lineGlowPaint.setMaskFilter(new BlurMaskFilter(GLOW_BLUR_RADIUS * density, BlurMaskFilter.Blur.NORMAL));

        goldGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goldGlowPaint.setStyle(Paint.Style.STROKE);
        goldGlowPaint.setStrokeWidth(GOLD_STROKE_BASE * GLOW_STROKE_FACTOR * density);
        goldGlowPaint.setColor(Color.parseColor("#E5B83C"));
        goldGlowPaint.setAlpha((int)(GOLD_LINE_ALPHA * GLOW_ALPHA * 255));
        goldGlowPaint.setStrokeCap(Paint.Cap.ROUND);
        goldGlowPaint.setMaskFilter(new BlurMaskFilter(GLOW_BLUR_RADIUS * density, BlurMaskFilter.Blur.NORMAL));

        // Węzły
        nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodePaint.setStyle(Paint.Style.FILL);
        nodePaint.setColor(Color.parseColor("#E5B83C"));
        nodePaint.setAlpha((int)(NODE_ALPHA * 255));
        nodePaint.setMaskFilter(new BlurMaskFilter(NODE_GLOW_RADIUS * density, BlurMaskFilter.Blur.NORMAL));

        // Gwiazdy
        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setColor(Color.parseColor("#E5B83C"));
        starPaint.setAlpha((int)(STAR_ALPHA_BASE * 255));
        starPaint.setMaskFilter(new BlurMaskFilter(STAR_GLOW_RADIUS * density, BlurMaskFilter.Blur.NORMAL));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        generateThreads();
        generateStars();
        startAnimation();
    }

    private void generateThreads() {
        threads.clear();
        random.setSeed(42);
        for (int i = 0; i < THREAD_COUNT; i++) {
            int startEdge = random.nextInt(4);
            int endEdge = (startEdge + 1 + random.nextInt(2)) % 4;
            float x0 = edgeX(startEdge, random.nextFloat());
            float y0 = edgeY(startEdge, random.nextFloat());
            float x1 = edgeX(endEdge, random.nextFloat());
            float y1 = edgeY(endEdge, random.nextFloat());

            float cx1 = 0.15f + random.nextFloat() * 0.7f;
            float cy1 = random.nextFloat();
            float cx2 = 0.15f + random.nextFloat() * 0.7f;
            float cy2 = random.nextFloat();

            float amplitude = 0.015f + random.nextFloat() * 0.035f;
            float phase = random.nextFloat() * (float)(Math.PI * 2);
            float speed = 0.3f + random.nextFloat() * 1.2f;
            boolean isGold = random.nextFloat() < 0.3f;   // 30% złotych
            float nodeT = 0.2f + random.nextFloat() * 0.6f;

            ThreadLine t = new ThreadLine();
            t.x0 = x0; t.y0 = y0;
            t.cx1 = cx1; t.cy1 = cy1;
            t.cx2 = cx2; t.cy2 = cy2;
            t.x1 = x1; t.y1 = y1;
            t.amplitude = amplitude;
            t.phase = phase;
            t.speed = speed;
            t.isGold = isGold;
            t.nodeT = nodeT;
            threads.add(t);
        }
    }

    private void generateStars() {
        stars.clear();
        random.setSeed(123);
        for (int i = 0; i < STAR_COUNT; i++) {
            Star s = new Star();
            s.x = random.nextFloat();
            s.y = random.nextFloat();
            s.radius = STAR_RADIUS_BASE + random.nextFloat() * STAR_RADIUS_VAR;
            s.alpha = STAR_ALPHA_BASE + random.nextFloat() * STAR_ALPHA_VAR;
            s.speed = STAR_SPEED_MIN + random.nextFloat() * (STAR_SPEED_MAX - STAR_SPEED_MIN);
            s.phase = random.nextFloat() * (float)(Math.PI * 2);
            stars.add(s);
        }
    }

    private float edgeX(int edge, float r) {
        switch (edge) {
            case 0: case 1: return r;
            case 2: return 0f;
            case 3: return 1f;
            default: return r;
        }
    }

    private float edgeY(int edge, float r) {
        switch (edge) {
            case 0: return 0f;
            case 1: return 1f;
            case 2: case 3: return r;
            default: return r;
        }
    }

    private void startAnimation() {
        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(ANIM_DURATION_MS);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            animProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) animator.cancel();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (width > 0 && !threads.isEmpty()) startAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (width == 0 || height == 0) return;

        float time = animProgress * (float)(Math.PI * 2);
        float density = getResources().getDisplayMetrics().density;

        // Rysowanie nici (najpierw pod spodem)
        for (ThreadLine t : threads) {
            float dx = (float)(Math.sin(time * t.speed + t.phase) * t.amplitude);
            float dy = (float)(Math.cos(time * t.speed + t.phase + 1.0) * t.amplitude * 0.6f);

            float px0 = t.x0 * width;
            float py0 = t.y0 * height;
            float pcx1 = (t.cx1 + dx) * width;
            float pcy1 = (t.cy1 + dy) * height;
            float pcx2 = (t.cx2 - dx) * width;
            float pcy2 = (t.cy2 - dy) * height;
            float px1 = t.x1 * width;
            float py1 = t.y1 * height;

            Path path = new Path();
            path.moveTo(px0, py0);
            path.cubicTo(pcx1, pcy1, pcx2, pcy2, px1, py1);

            Paint glow = t.isGold ? goldGlowPaint : lineGlowPaint;
            Paint main = t.isGold ? goldPaint : linePaint;

            // Najpierw warstwa glow (gruba, rozmyta)
            canvas.drawPath(path, glow);
            // Potem cienka, ostra linia (rdzeń)
            canvas.drawPath(path, main);

            // Węzeł (blask)
            float[] nodePos = evalBezier(px0, py0, pcx1, pcy1, pcx2, pcy2, px1, py1, t.nodeT);
            canvas.drawCircle(nodePos[0], nodePos[1], NODE_RADIUS * density, nodePaint);
        }

        // Rysowanie gwiazd (na wierzchu, ale mogą być pod spodem – dla efektu lepiej na wierzchu)
        for (Star s : stars) {
            float blink = 0.5f + 0.5f * (float)Math.sin(time * s.speed + s.phase);
            starPaint.setAlpha((int)(s.alpha * blink * 255));
            canvas.drawCircle(s.x * width, s.y * height, s.radius * density, starPaint);
        }
    }

    private float[] evalBezier(float x0, float y0, float cx1, float cy1, float cx2, float cy2, float x1, float y1, float t) {
        float mt = 1 - t;
        float mt2 = mt * mt;
        float mt3 = mt2 * mt;
        float t2 = t * t;
        float t3 = t2 * t;
        float x = mt3 * x0 + 3 * mt2 * t * cx1 + 3 * mt * t2 * cx2 + t3 * x1;
        float y = mt3 * y0 + 3 * mt2 * t * cy1 + 3 * mt * t2 * cy2 + t3 * y1;
        return new float[]{x, y};
    }
}