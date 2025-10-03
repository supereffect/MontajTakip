package com.egehan.montajhattitakip.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.egehan.montajhattitakip.Model.Record;
import com.egehan.montajhattitakip.R;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditItemActivity extends AppCompatActivity {
    EditText etUsername, etType, etBarcode, etHat, etReason;
    Button btnSave,btnScanBarcode;
    SharedPreferences prefs;
    Gson gson = new Gson();
    List<Record> list;
    int recordIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        etUsername = findViewById(R.id.etUsername);
        etType = findViewById(R.id.etType);
        etBarcode = findViewById(R.id.etBarcode);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);
        etHat = findViewById(R.id.etHat);
        etReason = findViewById(R.id.etReason);
        btnSave = findViewById(R.id.btnSave);

        prefs = getSharedPreferences("AppData", MODE_PRIVATE);

        // Kayıtları al
        String json = prefs.getString("records", "[]");
        Record[] recordsArray = gson.fromJson(json, Record[].class);
        list = new ArrayList<>();
        if (recordsArray != null) {
            list.addAll(Arrays.asList(recordsArray));
        }

        // Gelen veriyi al
        recordIndex = getIntent().getIntExtra("record_index", -1);
        String recordJson = getIntent().getStringExtra("record_data");
        Record record = gson.fromJson(recordJson, Record.class);

        // EditText'lere doldur
        if (record != null) {
            etUsername.setText(record.getUsername());
            etType.setText(record.getType());
            etBarcode.setText(record.getBarcode());
            etHat.setText(record.getHat());
            etReason.setText(record.getReason());
        }

        // Kaydet butonu
        btnSave.setOnClickListener(v -> {
            if (recordIndex >= 0 && recordIndex < list.size()) {
                Record updated = list.get(recordIndex);
                updated.setUsername(etUsername.getText().toString());
                updated.setType(etType.getText().toString());
                updated.setBarcode(etBarcode.getText().toString());
                updated.setHat(etHat.getText().toString());
                updated.setReason(etReason.getText().toString());

                prefs.edit().putString("records", gson.toJson(list)).apply();
                finish(); // geri dön
            }
        });
        btnScanBarcode.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setPrompt("Barkod okut");
            integrator.setBeepEnabled(true);
            integrator.setCaptureActivity(PortraitCaptureActivity.class);
            integrator.initiateScan();
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                etBarcode.setText(result.getContents());
                Toast.makeText(this, "Barkod: " + result.getContents(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Barkod iptal edildi", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
