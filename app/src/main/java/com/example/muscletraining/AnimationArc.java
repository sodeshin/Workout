package com.example.muscletraining;


import android.view.animation.Animation;
import android.view.animation.Transformation;

public class AnimationArc extends Animation {

    private Arc arc;

    // 中心座標
    private float centerX;
    private float centerY;

    // アニメーション角度
    private float oldAngle;
    private float newAngle;

    AnimationArc(Arc arc, int newAngle) {
        this.oldAngle = arc.getAngle();
        this.newAngle = newAngle;
        this.arc = arc;
    }

    @Override
    protected void applyTransformation(
            float interpolatedTime, Transformation transformation) {
        float angle = oldAngle + ((newAngle - oldAngle) * interpolatedTime);

        arc.setAngle(angle);
        arc.requestLayout();
    }
}