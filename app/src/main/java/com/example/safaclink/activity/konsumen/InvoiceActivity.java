package com.example.safaclink.activity.konsumen;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.safaclink.databinding.ActivityInvoiceBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceActivity extends AppCompatActivity {
    private ActivityInvoiceBinding binding;
    private String orderId, namaPaket, tipePaket, totalHarga, deskripsi, statusOrder;
    private String namaPelanggan, emailPelanggan, nohpPelanggan, tanggalTransaksi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInvoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get data from intent
        getDataFromIntent();

        // Setup toolbar
        setupToolbar();

        // Setup invoice data
        setupInvoiceData();

        // Setup button listeners
        setupButtonListeners();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        orderId = intent.getStringExtra("ORDER_ID");
        namaPaket = intent.getStringExtra("NAMA_PAKET");
        tipePaket = intent.getStringExtra("TIPE_PAKET");
        totalHarga = intent.getStringExtra("TOTAL_HARGA");
        deskripsi = intent.getStringExtra("DESKRIPSI");
        statusOrder = intent.getStringExtra("STATUS_ORDER");
        namaPelanggan = intent.getStringExtra("NAMA_PELANGGAN");
        emailPelanggan = intent.getStringExtra("EMAIL_PELANGGAN");
        nohpPelanggan = intent.getStringExtra("NOHP_PELANGGAN");
        tanggalTransaksi = intent.getStringExtra("TANGGAL_TRANSAKSI");
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Invoice");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupInvoiceData() {
        // Format tanggal
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String formattedDate = "";
        try {
            Date date = inputFormat.parse(tanggalTransaksi);
            formattedDate = outputFormat.format(date);
        } catch (Exception e) {
            formattedDate = tanggalTransaksi;
        }

        // Set invoice data
        binding.tvInvoiceNumber.setText("INV-" + orderId);
        binding.tvInvoiceDate.setText(formattedDate);
        binding.tvCustomerName.setText(namaPelanggan);
        binding.tvCustomerEmail.setText(emailPelanggan);
        binding.tvCustomerPhone.setText(nohpPelanggan);

        // Set service details
        binding.tvServiceName.setText(namaPaket);
        binding.tvServiceType.setText(tipePaket);
        binding.tvServiceDescription.setText(deskripsi);
        binding.tvServicePrice.setText(formatRupiah(totalHarga));
        binding.tvTotalAmount.setText(formatRupiah(totalHarga));

        // Set status
        binding.tvOrderStatus.setText(getStatusText(statusOrder));

        // Set status color
        setStatusColor(statusOrder);
    }

    private void setupButtonListeners() {
        binding.btnDownloadPdf.setOnClickListener(v -> generatePDF());
        binding.btnShareInvoice.setOnClickListener(v -> shareInvoice());
    }

    private String formatRupiah(String amount) {
        try {
            double value = Double.parseDouble(amount);
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            return formatter.format(value);
        } catch (NumberFormatException e) {
            return "Rp " + amount;
        }
    }

    private String getStatusText(String status) {
        if (status == null) {
            return "Menunggu Konfirmasi";
        }

        switch (status) {
            case "dijemput":
                return "Sedang Dijemput";
            case "dikerjakan":
                return "Sedang Dikerjakan";
            case "dikonfirmasi":
                return "Dikonfirmasi";
            case "selesai":
                return "Selesai";
            default:
                return "Menunggu Konfirmasi";
        }
    }

    private void setStatusColor(String status) {
        // Add null check to prevent NullPointerException
        if (status == null) {
            status = ""; // Set to empty string or default value
        }

        int color;
        switch (status) {
            case "selesai":
                color = Color.parseColor("#4CAF50"); // Green
                break;
            case "dikerjakan":
                color = Color.parseColor("#FF9800"); // Orange
                break;
            case "dijemput":
                color = Color.parseColor("#2196F3"); // Blue
                break;
            case "dikonfirmasi":
                color = Color.parseColor("#9C27B0"); // Purple
                break;
            default:
                color = Color.parseColor("#757575"); // Gray
                break;
        }
        binding.tvOrderStatus.setTextColor(color);
    }

    private void generatePDF() {
        try {
            // Create PDF document
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            // Draw header
            paint.setTextSize(24);
            paint.setColor(Color.BLACK);
            paint.setFakeBoldText(true);
            canvas.drawText("SAFAC LINK", 50, 80, paint);

            paint.setTextSize(16);
            paint.setFakeBoldText(false);
            canvas.drawText("Layanan Cuci Karpet & Sofa", 50, 110, paint);

            // Draw invoice title
            paint.setTextSize(28);
            paint.setFakeBoldText(true);
            canvas.drawText("INVOICE", 400, 80, paint);

            // Draw invoice details
            paint.setTextSize(14);
            paint.setFakeBoldText(false);
            canvas.drawText("No. Invoice: INV-" + orderId, 50, 180, paint);
            canvas.drawText("Tanggal: " + binding.tvInvoiceDate.getText(), 50, 210, paint);

            // Draw customer info
            paint.setTextSize(16);
            paint.setFakeBoldText(true);
            canvas.drawText("Pelanggan:", 50, 270, paint);

            paint.setTextSize(14);
            paint.setFakeBoldText(false);
            canvas.drawText(namaPelanggan, 50, 300, paint);
            canvas.drawText(emailPelanggan, 50, 330, paint);
            canvas.drawText(nohpPelanggan, 50, 360, paint);

            // Draw service details
            paint.setTextSize(16);
            paint.setFakeBoldText(true);
            canvas.drawText("Detail Layanan:", 50, 420, paint);

            paint.setTextSize(14);
            paint.setFakeBoldText(false);
            canvas.drawText("Nama Paket: " + namaPaket, 50, 450, paint);
            canvas.drawText("Tipe: " + tipePaket, 50, 480, paint);
            canvas.drawText("Deskripsi: " + deskripsi, 50, 510, paint);
            canvas.drawText("Status: " + getStatusText(statusOrder), 50, 540, paint);

            // Draw total
            paint.setTextSize(18);
            paint.setFakeBoldText(true);
            canvas.drawText("Total: " + formatRupiah(totalHarga), 50, 600, paint);

            // Draw footer
            paint.setTextSize(12);
            paint.setFakeBoldText(false);
            canvas.drawText("Terima kasih telah menggunakan layanan kami!", 50, 750, paint);
            canvas.drawText("Untuk informasi lebih lanjut, hubungi customer service kami.", 50, 780, paint);

            document.finishPage(page);

            // Save PDF
            String fileName = "Invoice_" + orderId + ".pdf";
            File directory = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoices");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            Toast.makeText(this, "Invoice berhasil disimpan: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareInvoice() {
        try {
            // Create a simple text to share
            String shareText = "INVOICE - SAFAC LINK\n\n" +
                    "No. Invoice: INV-" + orderId + "\n" +
                    "Tanggal: " + binding.tvInvoiceDate.getText() + "\n\n" +
                    "Pelanggan: " + namaPelanggan + "\n" +
                    "Email: " + emailPelanggan + "\n" +
                    "No. HP: " + nohpPelanggan + "\n\n" +
                    "Detail Layanan:\n" +
                    "- Nama Paket: " + namaPaket + "\n" +
                    "- Tipe: " + tipePaket + "\n" +
                    "- Status: " + getStatusText(statusOrder) + "\n\n" +
                    "Total: " + formatRupiah(totalHarga) + "\n\n" +
                    "Terima kasih telah menggunakan layanan SAFAC LINK!";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoice SAFAC LINK - " + orderId);

            startActivity(Intent.createChooser(shareIntent, "Bagikan Invoice"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membagikan invoice", Toast.LENGTH_SHORT).show();
        }
    }
}