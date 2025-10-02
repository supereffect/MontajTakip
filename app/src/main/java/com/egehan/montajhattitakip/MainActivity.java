package com.egehan.montajhattitakip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.egehan.montajhattitakip.Model.Record;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    EditText etBarcode1, etReason, etBarcode2;
    CheckBox chkRemoved;
    Spinner spHat1, spHat2;
    Button btnScan1, btnYereAl, btnScan2, btnHattaYukle, btnHistory;
    SharedPreferences prefs;
    Gson gson = new Gson();
    private static final int REQUEST_CODE_BARCODE1 = 493741; // IntentIntegrator'ın default kodu
    private static final int REQUEST_CODE_BARCODE2 = 493742;
    private EditText currentBarcodeEditText = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        prefs = getSharedPreferences("AppData", MODE_PRIVATE);

        etBarcode1 = findViewById(R.id.etBarcode1);
        etReason = findViewById(R.id.etReason);
        etBarcode2 = findViewById(R.id.etBarcode2);
        spHat1 = findViewById(R.id.spHat1);
        spHat2 = findViewById(R.id.spHat2);
        btnScan1 = findViewById(R.id.btnScan1);
        btnYereAl = findViewById(R.id.btnYereAl);
        btnScan2 = findViewById(R.id.btnScan2);
        btnHattaYukle = findViewById(R.id.btnHattaYukle);
        btnHistory = findViewById(R.id.btnHistory);
        chkRemoved = findViewById(R.id.chkRemoved);

        String[] hats = {"Hat 1", "Hat 2", "Hat 3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hats);
        spHat1.setAdapter(adapter);
        spHat2.setAdapter(adapter);

        // **DÜZELTME 2:** scanBarcode metodunu hedef EditText ile çağır.
        btnScan1.setOnClickListener(v -> scanBarcode(etBarcode1));
        btnScan2.setOnClickListener(v -> scanBarcode(etBarcode2));

        btnYereAl.setOnClickListener(v -> {
            boolean removed = chkRemoved.isChecked();
            saveRecord("Yere Al", etBarcode1.getText().toString(), spHat1.getSelectedItem().toString(), etReason.getText().toString() + (removed ? " (Söküldü)" : ""));
        });
//        btnYereAl.setOnClickListener(v -> saveRecord("Yere Al", etBarcode1.getText().toString(), spHat1.getSelectedItem().toString(), etReason.getText().toString()));
        btnHattaYukle.setOnClickListener(v -> saveRecord("Hatta Yükle", etBarcode2.getText().toString(), spHat2.getSelectedItem().toString(), ""));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
    }

    private void scanBarcode(EditText target) {
        currentBarcodeEditText = target;

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Barkod Okut");
        integrator.setBeepEnabled(true);
        integrator.setCaptureActivity(PortraitCaptureActivity.class);
        integrator.initiateScan();

        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // **DÜZELTME 5:** Hata almamak için IntentIntegrator'ı her zaman ilk başta dene
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null && result.getContents() != null) {
            String barcodeContent = result.getContents();

            // Eğer bir hedef EditText belirlenmişse, sonucu yaz
            if (currentBarcodeEditText != null) {
                currentBarcodeEditText.setText(barcodeContent);
                Toast.makeText(this, "Barkod okundu: " + barcodeContent, Toast.LENGTH_SHORT).show();
                currentBarcodeEditText = null; // İşlem bitti, sıfırla
            } else {
                Toast.makeText(this, "HATA: Barkod hedefi belirlenemedi.", Toast.LENGTH_SHORT).show();
            }
        } else if (result != null && result.getContents() == null) {
            // Tarama iptal edildi.
            Toast.makeText(this, "Tarama iptal edildi.", Toast.LENGTH_SHORT).show();
        } else {
            // Başka bir Activity sonucu geldiyse, base metodu çağır
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void saveRecord(String type, String barcode, String hat, String reason) {
        String username = prefs.getString("username", "Unknown");
        Date now = new Date();
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(now);

        // Saat değerini al
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        String shift;
        if (hour >= 8 && hour < 16) {
            shift = "Gündüz";
        } else if (hour >= 16 && hour < 24) {
            shift = "Akşam";
        } else { // 00:00 - 08:00
            shift = "Gece";
        }

        Record record = new Record(username, type, barcode, hat, reason, timestamp, shift);

        String json = prefs.getString("records", "[]");
        List<Record> list = new ArrayList<>(Arrays.asList(gson.fromJson(json, Record[].class)));
        list.add(record);
        prefs.edit().putString("records", gson.toJson(list)).apply();

        Toast.makeText(this, "Kayıt eklendi", Toast.LENGTH_SHORT).show();
    }

}
