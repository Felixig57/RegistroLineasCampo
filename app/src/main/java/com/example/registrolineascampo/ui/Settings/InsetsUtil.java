package com.example.registrolineascampo.ui;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InsetsUtil {

    /**
     * header: la vista verde de arriba (crece para cubrir la barra de estado).
     * bottom: la barra de navegación inferior (puede ser null).
     */
    public static void aplicar(View root, View header, View bottom) {
        final int padTop = header.getPaddingTop();
        final ViewGroup.LayoutParams lp = bottom != null ? bottom.getLayoutParams() : null;
        final int altoBase = lp != null ? lp.height : 0;

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets b = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());

            header.setPadding(header.getPaddingLeft(), padTop + b.top,
                    header.getPaddingRight(), header.getPaddingBottom());

            if (bottom != null) {
                bottom.setPadding(bottom.getPaddingLeft(), bottom.getPaddingTop(),
                        bottom.getPaddingRight(), b.bottom);
                lp.height = altoBase + b.bottom;
                bottom.setLayoutParams(lp);
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }
}