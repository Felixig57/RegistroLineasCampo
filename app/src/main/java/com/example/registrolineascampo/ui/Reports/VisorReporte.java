package com.example.registrolineascampo.ui.Reports;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.registrolineascampo.R;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class VisorReporte extends AppCompatActivity {

    public static final String EXTRA_RUTA = "ruta";
    private static final int MAX_COLS = 6;

    private File archivo;
    private ScrollView scrollPdf, scrollExcel;
    private LinearLayout containerPaginas;
    private TableLayout tablaExcel;

    private static class Celda {
        final String texto;
        final int estilo; // 0 normal, 1 negrita, 2 moneda, 3 negrita + moneda
        Celda(String texto, int estilo) {
            this.texto = texto;
            this.estilo = estilo;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(Color.parseColor("#4CAF50"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visor_reporte);

        String ruta = getIntent().getStringExtra(EXTRA_RUTA);
        if (ruta == null || !new File(ruta).exists()) {
            Toast.makeText(this, "El reporte ya no existe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        archivo = new File(ruta);

        scrollPdf = findViewById(R.id.scrollPdf);
        scrollExcel = findViewById(R.id.scrollExcel);
        containerPaginas = findViewById(R.id.containerPaginas);
        tablaExcel = findViewById(R.id.tablaExcel);

        ((TextView) findViewById(R.id.txtTituloVisor)).setText(archivo.getName());
        ((Button) findViewById(R.id.btnVolverVisor)).setOnClickListener(v -> finish());
        ((Button) findViewById(R.id.btnCompartirVisor))
                .setOnClickListener(v -> ReportesArchivos.compartirWhatsApp(this, archivo));

        if (ReportesArchivos.esPdf(archivo)) mostrarPdf();
        else mostrarExcel();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    // ================= PDF =================

    private void mostrarPdf() {
        scrollPdf.setVisibility(View.VISIBLE);
        final int ancho = getResources().getDisplayMetrics().widthPixels - dp(24);

        new Thread(() -> {
            try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY);
                 PdfRenderer renderer = new PdfRenderer(pfd)) {

                for (int i = 0; i < renderer.getPageCount(); i++) {
                    PdfRenderer.Page page = renderer.openPage(i);
                    int alto = (int) ((float) ancho * page.getHeight() / page.getWidth());
                    Bitmap bmp = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
                    bmp.eraseColor(Color.WHITE);
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    page.close();

                    runOnUiThread(() -> agregarPagina(bmp));
                }
            } catch (IOException | SecurityException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "No se pudo abrir el PDF", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void agregarPagina(Bitmap bmp) {
        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        img.setLayoutParams(lp);
        img.setAdjustViewBounds(true);
        img.setImageBitmap(bmp);
        containerPaginas.addView(img);
    }

    // ================= Excel =================

    private void mostrarExcel() {
        scrollExcel.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                List<Celda[]> filas = leerXlsx();
                runOnUiThread(() -> pintarTabla(filas));
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "No se pudo abrir el Excel", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private List<Celda[]> leerXlsx() throws Exception {
        List<Celda[]> filas = new ArrayList<>();

        try (ZipFile zip = new ZipFile(archivo)) {
            ZipEntry entrada = zip.getEntry("xl/worksheets/sheet1.xml");
            if (entrada == null) throw new IOException("Hoja no encontrada");

            try (InputStream in = zip.getInputStream(entrada)) {
                XmlPullParser p = Xml.newPullParser();
                p.setInput(in, "UTF-8");

                Celda[] fila = null;
                int ultimaFila = 0, col = 0, estilo = 0;
                boolean inline = false;
                StringBuilder txt = null;
                String tag = "";

                int ev = p.getEventType();
                while (ev != XmlPullParser.END_DOCUMENT) {
                    if (ev == XmlPullParser.START_TAG) {
                        tag = p.getName();
                        if (tag.equals("row")) {
                            int r = Integer.parseInt(p.getAttributeValue(null, "r"));
                            while (ultimaFila + 1 < r) { // filas vacías intermedias
                                filas.add(new Celda[MAX_COLS]);
                                ultimaFila++;
                            }
                            fila = new Celda[MAX_COLS];
                            ultimaFila = r;
                        } else if (tag.equals("c")) {
                            String ref = p.getAttributeValue(null, "r");
                            col = ref.charAt(0) - 'A';
                            String s = p.getAttributeValue(null, "s");
                            estilo = s != null ? Integer.parseInt(s) : 0;
                            inline = "inlineStr".equals(p.getAttributeValue(null, "t"));
                            txt = new StringBuilder();
                        }
                    } else if (ev == XmlPullParser.TEXT) {
                        if (txt != null && (tag.equals("t") || tag.equals("v"))) {
                            txt.append(p.getText());
                        }
                    } else if (ev == XmlPullParser.END_TAG) {
                        String n = p.getName();
                        if (n.equals("c") && fila != null && txt != null && col >= 0 && col < MAX_COLS) {
                            fila[col] = new Celda(formatear(txt.toString(), inline, estilo), estilo);
                            txt = null;
                        } else if (n.equals("row") && fila != null) {
                            filas.add(fila);
                            fila = null;
                        }
                        tag = "";
                    }
                    ev = p.next();
                }
            }
        }
        return filas;
    }

    private String formatear(String texto, boolean inline, int estilo) {
        if (inline) return texto;
        try {
            double d = Double.parseDouble(texto);
            if (estilo == 2 || estilo == 3) return String.format(Locale.US, "$%,.2f", d);
            if (d == Math.rint(d)) return String.valueOf((long) d);
        } catch (NumberFormatException ignorada) {
        }
        return texto;
    }

    private void pintarTabla(List<Celda[]> filas) {
        tablaExcel.removeAllViews();

        for (Celda[] fila : filas) {
            TableRow tr = new TableRow(this);
            for (int c = 0; c < MAX_COLS; c++) {
                Celda celda = fila[c];

                TextView tv = new TextView(this);
                tv.setPadding(dp(10), dp(7), dp(10), dp(7));
                tv.setTextSize(13);
                tv.setTextColor(Color.parseColor("#333333"));
                tv.setText(celda != null ? celda.texto : "");

                if (celda != null) {
                    if (celda.estilo == 1 || celda.estilo == 3) tv.setTypeface(null, Typeface.BOLD);
                    if (celda.estilo == 2 || celda.estilo == 3) tv.setGravity(Gravity.END);
                }
                tr.addView(tv);
            }
            tablaExcel.addView(tr);
        }
    }
}