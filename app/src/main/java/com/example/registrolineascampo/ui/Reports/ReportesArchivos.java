package com.example.registrolineascampo.ui.Reports;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportesArchivos {

    public static File carpeta(Context ctx) {
        File base = ctx.getExternalFilesDir("reportes");
        if (base == null) base = new File(ctx.getFilesDir(), "reportes");
        if (!base.exists()) base.mkdirs();
        return base;
    }

    /** Reportes guardados, del más reciente al más antiguo. */
    public static List<File> listar(Context ctx) {
        File[] arr = carpeta(ctx).listFiles((d, n) -> n.endsWith(".pdf") || n.endsWith(".xlsx"));
        List<File> lista = new ArrayList<>();
        if (arr != null) {
            Arrays.sort(arr, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            lista.addAll(Arrays.asList(arr));
        }
        return lista;
    }

    public static boolean esPdf(File f) {
        return f.getName().toLowerCase().endsWith(".pdf");
    }

    public static String mime(File f) {
        return esPdf(f) ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    /** Abre WhatsApp con el archivo adjunto. Si no está instalado, muestra el menú de compartir. */
    public static void compartirWhatsApp(Context ctx, File f) {
        Uri uri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", f);

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType(mime(f));
        i.putExtra(Intent.EXTRA_STREAM, uri);
        i.putExtra(Intent.EXTRA_TEXT, "Reporte del día - Sistema de Apuntado");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.setPackage("com.whatsapp");

        try {
            ctx.startActivity(i);
        } catch (ActivityNotFoundException e) {
            i.setPackage(null);
            ctx.startActivity(Intent.createChooser(i, "Compartir reporte"));
        }
    }
}