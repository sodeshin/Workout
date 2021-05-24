package com.example.muscletraining;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class Arc extends View{

    private final Paint paint;
    private RectF rect;

    // Animation 開始地点をセット
    private static final int AngleTarget = 270;//0は右
    // 初期 Angle
    private float angle = 0;

    public Arc(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Arcの幅
        int strokeWidth = 100;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        // Arcの色
        paint.setColor(Color.argb(255, 0, 255, 255));//円
        // Arcの範囲
        rect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 背景、半透明
        canvas.drawColor(Color.argb(0, 255, 255, 255));//背景(0で透明)

        // Canvas 中心点
        float x = getWidth()/2;
        float y = getHeight()/2;
        // 半径
        float radius = getWidth()/4;

        // 円弧の領域設定
        rect.set(x - radius, y - radius, x + radius, y + radius);

        // 円弧の描画
        canvas.drawArc(rect, AngleTarget, angle, false, paint);
    }

    // AnimationAへ現在のangleを返す
    public float getAngle() {
        return angle;
    }

    // AnimationAから新しいangleが設定される
    public void setAngle(float angle) {
        this.angle = angle;
    }
}