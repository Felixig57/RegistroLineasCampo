package com.example.registrolineascampo.ui.Reports;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.text.SimpleDateFormat;
import java.util.Date;
import androidx.appcompat.app.AppCompatActivity;

import com.example.registrolineascampo.R;
import com.example.registrolineascampo.data.entity.ConfiguracionTrabajo;
import com.example.registrolineascampo.data.entity.LaborCampo;
import com.example.registrolineascampo.data.entity.Trabajador;
import com.example.registrolineascampo.data.local.AppDatabase;
import com.example.registrolineascampo.ui.Records.Registros;
import com.example.registrolineascampo.ui.Settings.Configuracion;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Reportes extends AppCompatActivity {

    private TextView txtTotalLineas, txtTotalTrabajadores, txtRankingVacio, txtReportesVacio;
    private LinearLayout containerRanking, containerReportes;
    private Button btnExportTXT, btnExportCSV; // ids viejos: CSV = Excel, TXT = PDF
    private AppDatabase db;

    private List<Trabajador> listaActual = new ArrayList<>();
    private ConfiguracionTrabajo configActual;
    private int totalLineasHoy = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#4CAF50"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes); // ajusta si tu layout se llama distinto
        com.example.registrolineascampo.ui.InsetsUtil.aplicar(
                findViewById(R.id.main),
                findViewById(R.id.linearLayoutHeader),
                findViewById(R.id.bottomNavLayout));
        db = AppDatabase.getAppDatabase(getApplicationContext());

        txtTotalLineas = findViewById(R.id.txtTotalLineas);
        txtTotalTrabajadores = findViewById(R.id.txtTotalTrabajadores);
        txtRankingVacio = findViewById(R.id.txtRankingVacio);

        containerRanking = findViewById(R.id.containerRanking);
        btnExportTXT = findViewById(R.id.btnExportTXT);
        btnExportCSV = findViewById(R.id.btnExportCSV);
        txtReportesVacio = findViewById(R.id.txtReportesVacio);
        containerReportes = findViewById(R.id.containerReportes);
        btnExportCSV.setOnClickListener(v -> exportarExcel());
        btnExportTXT.setOnClickListener(v -> exportarPDF());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarReporte();
        cargarListaReportes();
    }

    // ================= Carga y ranking =================

    private void cargarReporte() {
        new Thread(() -> {
            String hoy = LaborCampo.fechaHoy();
            List<Trabajador> lista = db.trabajadorDao().getRankingPorFecha(hoy);

            ConfiguracionTrabajo cfg = db.configuracionDao().getConfigPorFecha(hoy);
            if (cfg == null) cfg = db.configuracionDao().getUltimaConfig(); // respaldo
            final ConfiguracionTrabajo cfgFinal = cfg;

            int suma = 0;
            for (Trabajador t : lista) suma += t.totalLineas;
            final int total = suma;

            runOnUiThread(() -> {
                listaActual = lista;
                configActual = cfgFinal;
                totalLineasHoy = total;
                txtTotalLineas.setText(String.valueOf(total));
                txtTotalTrabajadores.setText(String.valueOf(lista.size()));
                pintarRanking();
            });
        }).start();
    }

    private void pintarRanking() {
        containerRanking.removeAllViews();

        if (listaActual.isEmpty()) {
            txtRankingVacio.setVisibility(View.VISIBLE);
            return;
        }
        txtRankingVacio.setVisibility(View.GONE);

        int max = Math.max(1, listaActual.get(0).totalLineas);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < listaActual.size(); i++) {
            Trabajador t = listaActual.get(i);
            View fila = inflater.inflate(R.layout.item_ranking, containerRanking, false);

            ((TextView) fila.findViewById(R.id.txtPosicion)).setText(String.valueOf(i + 1));
            ((TextView) fila.findViewById(R.id.txtNombreRanking)).setText(t.nombre);
            ((TextView) fila.findViewById(R.id.txtCantidad)).setText(String.valueOf(t.totalLineas));

            ProgressBar pb = fila.findViewById(R.id.progressLineas);
            pb.setMax(max);
            pb.setProgress(t.totalLineas);

            containerRanking.addView(fila);
        }
    }

    // ================= Helpers =================

    private double precio() {
        return configActual != null ? configActual.precioLinea : 0;
    }

    private String textoConfig(String valor) {
        return (valor == null || valor.isEmpty()) ? "---" : valor;
    }

    private String dinero(double v) {
        return String.format(Locale.US, "$%,.2f", v);
    }

    private String fechaArchivo() {
        String hora = new SimpleDateFormat("HH-mm-ss", Locale.getDefault()).format(new Date());
        return LaborCampo.fechaHoy().replaceAll("[^0-9A-Za-z-]", "-") + "_" + hora;
    }

    private String xml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    // ================= Excel (.xlsx) =================

    private void exportarExcel() {
        if (listaActual.isEmpty()) {
            Toast.makeText(this, "No hay registros para exportar", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            guardarArchivo("reporte_lineas_" + fechaArchivo() + ".xlsx", generarXlsx());
        } catch (IOException e) {
            Toast.makeText(this, "Error al generar Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private byte[] generarXlsx() throws IOException {
        double precio = precio();
        String tipo = configActual != null ? textoConfig(configActual.tipoLabor) : "---";
        String cuadro = configActual != null ? textoConfig(configActual.numeroCuadro) : "---";
        String variedad = configActual != null ? textoConfig(configActual.variedadCuadro) : "---";

        // Estilos: 0 normal, 1 negrita, 2 moneda, 3 negrita + moneda
        StringBuilder s = new StringBuilder();
        s.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
                .append("<cols>")
                .append("<col min=\"1\" max=\"1\" width=\"18\" customWidth=\"1\"/>")
                .append("<col min=\"2\" max=\"2\" width=\"16\" customWidth=\"1\"/>")
                .append("<col min=\"3\" max=\"3\" width=\"30\" customWidth=\"1\"/>")
                .append("<col min=\"4\" max=\"4\" width=\"14\" customWidth=\"1\"/>")
                .append("<col min=\"5\" max=\"5\" width=\"22\" customWidth=\"1\"/>")
                .append("<col min=\"6\" max=\"6\" width=\"16\" customWidth=\"1\"/>")
                .append("</cols><sheetData>");

        // Datos generales
        s.append("<row r=\"1\">").append(celdaTexto(0, 1, "REPORTE DEL DIA", 1)).append("</row>");
        s.append("<row r=\"2\">").append(celdaTexto(0, 2, "Fecha", 1)).append(celdaTexto(1, 2, LaborCampo.fechaHoy(), 0)).append("</row>");
        s.append("<row r=\"3\">").append(celdaTexto(0, 3, "Labor", 1)).append(celdaTexto(1, 3, tipo, 0)).append("</row>");
        s.append("<row r=\"4\">").append(celdaTexto(0, 4, "Cuadro", 1)).append(celdaTexto(1, 4, cuadro, 0)).append("</row>");
        s.append("<row r=\"5\">").append(celdaTexto(0, 5, "Variedad", 1)).append(celdaTexto(1, 5, variedad, 0)).append("</row>");
        s.append("<row r=\"6\">").append(celdaTexto(0, 6, "Precio por linea", 1)).append(celdaNumero(1, 6, precio, 2)).append("</row>");

        // Encabezados de la tabla (fila 8)
        String[] enc = {"Posicion", "No. Empleado", "Nombre", "Total Lineas", "Lineas", "Pago"};
        s.append("<row r=\"8\">");
        for (int c = 0; c < enc.length; c++) s.append(celdaTexto(c, 8, enc[c], 1));
        s.append("</row>");

        // Datos desde la fila 9
        int primera = 9;
        double sumaPago = 0;
        for (int i = 0; i < listaActual.size(); i++) {
            Trabajador t = listaActual.get(i);
            int f = primera + i;
            double pago = t.totalLineas * precio;
            sumaPago += pago;
            String lineas = (t.totalLineas > 0 && t.lineasTexto != null) ? t.lineasTexto : "";

            s.append("<row r=\"").append(f).append("\">")
                    .append(celdaNumero(0, f, i + 1, 0))
                    .append(celdaTexto(1, f, t.numeroTrabajador, 0))
                    .append(celdaTexto(2, f, t.nombre, 0))
                    .append(celdaNumero(3, f, t.totalLineas, 0))
                    .append(celdaTexto(4, f, lineas, 0))
                    .append(celdaFormula(5, f, "D" + f + "*$B$6", pago, 2))
                    .append("</row>");
        }

        // Fila de totales
        int ultima = primera + listaActual.size() - 1;
        int ft = ultima + 1;
        s.append("<row r=\"").append(ft).append("\">")
                .append(celdaTexto(2, ft, "TOTAL", 1))
                .append(celdaFormula(3, ft, "SUM(D" + primera + ":D" + ultima + ")", totalLineasHoy, 1))
                .append(celdaFormula(5, ft, "SUM(F" + primera + ":F" + ultima + ")", sumaPago, 3))
                .append("</row>");
        s.append("</sheetData></worksheet>");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bos)) {
            agregarEntrada(zip, "[Content_Types].xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                            + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                            + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                            + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                            + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                            + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                            + "</Types>");

            agregarEntrada(zip, "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                            + "</Relationships>");

            agregarEntrada(zip, "xl/workbook.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                            + "<sheets><sheet name=\"Reporte\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");

            agregarEntrada(zip, "xl/_rels/workbook.xml.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                            + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                            + "</Relationships>");

            agregarEntrada(zip, "xl/styles.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                            + "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"&quot;$&quot;#,##0.00\"/></numFmts>"
                            + "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                            + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                            + "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills>"
                            + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                            + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                            + "<cellXfs count=\"4\">"
                            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                            + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                            + "<xf numFmtId=\"164\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\"/>"
                            + "</cellXfs></styleSheet>");

            agregarEntrada(zip, "xl/worksheets/sheet1.xml", s.toString());
        }
        return bos.toByteArray();
    }

    private void agregarEntrada(ZipOutputStream zip, String ruta, String contenido) throws IOException {
        zip.putNextEntry(new ZipEntry(ruta));
        zip.write(contenido.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String refCelda(int col, int fila) {
        return String.valueOf((char) ('A' + col)) + fila; // solo columnas A-Z
    }

    private String celdaTexto(int col, int fila, String valor, int estilo) {
        return "<c r=\"" + refCelda(col, fila) + "\" s=\"" + estilo + "\" t=\"inlineStr\"><is><t>"
                + xml(valor) + "</t></is></c>";
    }

    private String celdaNumero(int col, int fila, double valor, int estilo) {
        return "<c r=\"" + refCelda(col, fila) + "\" s=\"" + estilo + "\"><v>" + valor + "</v></c>";
    }

    private String celdaFormula(int col, int fila, String formula, double valorCalculado, int estilo) {
        return "<c r=\"" + refCelda(col, fila) + "\" s=\"" + estilo + "\"><f>" + formula
                + "</f><v>" + valorCalculado + "</v></c>";
    }

    // ================= PDF =================

    private void exportarPDF() {
        if (listaActual.isEmpty()) {
            Toast.makeText(this, "No hay registros para exportar", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            guardarArchivo("reporte_lineas_" + fechaArchivo() + ".pdf", generarPdf());
        } catch (IOException e) {
            Toast.makeText(this, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private byte[] generarPdf() throws IOException {
        final int W = 595, H = 842, M = 40; // A4
        final float xPos = M, xNombre = M + 30, xNum = M + 250, xLineas = M + 400, xPago = W - M;

        Paint titulo = new Paint(Paint.ANTI_ALIAS_FLAG);
        titulo.setTextSize(20);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setColor(0xFF2E7D32);

        Paint normal = new Paint(Paint.ANTI_ALIAS_FLAG);
        normal.setTextSize(12);
        normal.setColor(0xFF333333);

        Paint negrita = new Paint(normal);
        negrita.setTypeface(Typeface.DEFAULT_BOLD);

        Paint derecha = new Paint(normal);
        derecha.setTextAlign(Paint.Align.RIGHT);

        Paint derechaNegrita = new Paint(negrita);
        derechaNegrita.setTextAlign(Paint.Align.RIGHT);

        Paint raya = new Paint();
        raya.setColor(0xFFBDBDBD);
        raya.setStrokeWidth(1);

        double precio = precio();
        String tipo = configActual != null ? textoConfig(configActual.tipoLabor) : "---";
        String cuadro = configActual != null ? textoConfig(configActual.numeroCuadro) : "---";
        String variedad = configActual != null ? textoConfig(configActual.variedadCuadro) : "---";

        PdfDocument pdf = new PdfDocument();
        int nPagina = 1;
        PdfDocument.Page page = pdf.startPage(new PdfDocument.PageInfo.Builder(W, H, nPagina).create());
        Canvas cv = page.getCanvas();
        float y = M + 10;

        // Encabezado
        cv.drawText("Reporte del dia - Sistema de Apuntado", M, y, titulo);
        y += 28;
        cv.drawText("Fecha: " + LaborCampo.fechaHoy(), M, y, normal);
        y += 18;
        cv.drawText("Labor: " + tipo + "   |   Cuadro: " + cuadro + "   |   Variedad: " + variedad, M, y, normal);
        y += 18;
        cv.drawText("Precio por linea: " + dinero(precio), M, y, normal);
        y += 18;
        cv.drawText("Total de lineas: " + totalLineasHoy + "   |   Trabajadores: " + listaActual.size(), M, y, normal);
        y += 28;

        // Tabla
        y = encabezadoTabla(cv, y, W, M, xPos, xNombre, xNum, xLineas, xPago, negrita, derechaNegrita, raya);

        double sumaPago = 0;
        for (int i = 0; i < listaActual.size(); i++) {
            if (y > H - 90) {
                pdf.finishPage(page);
                nPagina++;
                page = pdf.startPage(new PdfDocument.PageInfo.Builder(W, H, nPagina).create());
                cv = page.getCanvas();
                y = M + 10;
                y = encabezadoTabla(cv, y, W, M, xPos, xNombre, xNum, xLineas, xPago, negrita, derechaNegrita, raya);
            }

            Trabajador t = listaActual.get(i);
            double pago = t.totalLineas * precio;
            sumaPago += pago;

            String nombre = t.nombre != null ? t.nombre : "";
            if (nombre.length() > 28) nombre = nombre.substring(0, 27) + "...";

            cv.drawText(String.valueOf(i + 1), xPos, y, normal);
            cv.drawText(nombre, xNombre, y, normal);
            cv.drawText(t.numeroTrabajador != null ? t.numeroTrabajador : "", xNum, y, normal);
            cv.drawText(String.valueOf(t.totalLineas), xLineas, y, derecha);
            cv.drawText(dinero(pago), xPago, y, derecha);
            y += 20;
        }

        // Totales
        if (y > H - 80) {
            pdf.finishPage(page);
            nPagina++;
            page = pdf.startPage(new PdfDocument.PageInfo.Builder(W, H, nPagina).create());
            cv = page.getCanvas();
            y = M + 10;
        }
        cv.drawLine(M, y - 12, W - M, y - 12, raya);
        y += 6;
        cv.drawText("TOTAL", xNombre, y, negrita);
        cv.drawText(String.valueOf(totalLineasHoy), xLineas, y, derechaNegrita);
        cv.drawText(dinero(sumaPago), xPago, y, derechaNegrita);
        y += 22;
        cv.drawText(totalLineasHoy + " lineas x " + dinero(precio) + " = " + dinero(sumaPago), xNombre, y, normal);

        pdf.finishPage(page);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        pdf.writeTo(bos);
        pdf.close();
        return bos.toByteArray();
    }

    private float encabezadoTabla(Canvas cv, float y, int W, int M,
                                  float xPos, float xNombre, float xNum, float xLineas, float xPago,
                                  Paint negrita, Paint derechaNegrita, Paint raya) {
        cv.drawText("#", xPos, y, negrita);
        cv.drawText("Nombre", xNombre, y, negrita);
        cv.drawText("No. Empleado", xNum, y, negrita);
        cv.drawText("Lineas", xLineas, y, derechaNegrita);
        cv.drawText("Pago", xPago, y, derechaNegrita);
        cv.drawLine(M, y + 6, W - M, y + 6, raya);
        return y + 26;
    }

    // ================= Guardar archivo =================

    private void guardarArchivo(String nombre, byte[] datos) {
        new Thread(() -> {
            String mensaje;
            try {
                File destino = new File(ReportesArchivos.carpeta(this), nombre);
                try (FileOutputStream fos = new FileOutputStream(destino)) {
                    fos.write(datos);
                }
                mensaje = "Reporte guardado";
            } catch (IOException e) {
                mensaje = "Error al guardar: " + e.getMessage();
            }

            final String msg = mensaje;
            runOnUiThread(() -> {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                cargarListaReportes();
            });
        }).start();
    }
    // ================= Lista de reportes =================

    private void cargarListaReportes() {
        new Thread(() -> {
            List<File> archivos = ReportesArchivos.listar(this);
            runOnUiThread(() -> pintarReportes(archivos));
        }).start();
    }

    private void pintarReportes(List<File> archivos) {
        containerReportes.removeAllViews();

        if (archivos.isEmpty()) {
            txtReportesVacio.setVisibility(View.VISIBLE);
            return;
        }
        txtReportesVacio.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (File f : archivos) {
            View fila = inflater.inflate(R.layout.item_reporte, containerReportes, false);
            boolean esPdf = ReportesArchivos.esPdf(f);

            TextView tipo = fila.findViewById(R.id.txtTipoReporte);
            tipo.setText(esPdf ? "PDF" : "XLSX");
            tipo.setBackgroundColor(esPdf ? 0xFF2962FF : 0xFF00C853);

            ((TextView) fila.findViewById(R.id.txtNombreReporte)).setText(f.getName());
            ((TextView) fila.findViewById(R.id.txtInfoReporte)).setText(
                    fmt.format(new Date(f.lastModified()))
                            + "  •  " + String.format(Locale.US, "%.1f KB", f.length() / 1024.0));

            // Abrir dentro de la app
            fila.setOnClickListener(v -> {
                Intent i = new Intent(this, VisorReporte.class);
                i.putExtra(VisorReporte.EXTRA_RUTA, f.getAbsolutePath());
                startActivity(i);
            });

            // Compartir por WhatsApp
            fila.findViewById(R.id.btnCompartirReporte)
                    .setOnClickListener(v -> ReportesArchivos.compartirWhatsApp(this, f));

            // Eliminar
            fila.findViewById(R.id.btnEliminarReporte).setOnClickListener(v ->
                    new AlertDialog.Builder(this)
                            .setTitle("Eliminar reporte")
                            .setMessage("¿Eliminar " + f.getName() + "?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Eliminar", (d, w) -> {
                                f.delete();
                                cargarListaReportes();
                            })
                            .show());

            containerReportes.addView(fila);
        }
    }

    // ================= Navegación (tu XML usa android:onClick) =================

    public void NavRegistro(View v) {
        startActivity(new Intent(this, Registros.class));
        finish();
    }

    public void NavReportes(View v) {
        // Ya estás en Reportes
    }

    public void NavConfig(View v) {
        startActivity(new Intent(this, Configuracion.class));
        finish();
    }
}