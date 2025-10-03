package com.egehan.montajhattitakip.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.egehan.montajhattitakip.Model.Record;
import com.egehan.montajhattitakip.R;
import com.egehan.montajhattitakip.Repository.Abstract.IRepositoryCallback;
import com.egehan.montajhattitakip.Repository.Concrete.RecordRepository;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import android.widget.RadioButton;
import android.widget.RadioGroup;

@AndroidEntryPoint

public class EditItemActivity extends BaseActivity {

    EditText etUsername, etBarcode, etReason;
    Button btnSave, btnScanBarcode;
    RadioGroup radioGroupType, radioGroupHat;
    RadioButton radioYereAl, radioHattaYukle, radioSokum;

    @Inject
    RecordRepository recordRepository;

    Record currentRecord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        etUsername = findViewById(R.id.etUsername);
        etBarcode = findViewById(R.id.etBarcode);
        etReason = findViewById(R.id.etReason);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);
        btnSave = findViewById(R.id.btnSave);

        radioGroupType = findViewById(R.id.radioGroupType);
        radioGroupHat = findViewById(R.id.radioGroupHat);

        radioYereAl = findViewById(R.id.radioYereAl);
        radioHattaYukle = findViewById(R.id.radioHattaYukle);
        radioSokum = findViewById(R.id.radioSokum);

        String recordId = getIntent().getStringExtra("record_id");
        if (recordId != null && !recordId.isEmpty()) {
            loadRecordById(recordId);
        }

        btnSave.setOnClickListener(v -> saveRecord());
        btnScanBarcode.setOnClickListener(v -> scanBarcode());
    }

    private void loadRecordById(String recordId) {
        recordRepository.getRecordById(recordId, new IRepositoryCallback<Record>() {
            @Override
            public void onStart() {}

            @Override
            public void onComplete(Record record) {
                if (record != null) {
                    currentRecord = record;
                    etUsername.setText(record.getUsername());
                    etBarcode.setText(record.getBarcode());
                    etReason.setText(record.getReason());

                    // İşlem tipi setleme
                    if ("Yere Al".equals(record.getType())) radioYereAl.setChecked(true);
                    else if ("Hatta Yükle".equals(record.getType())) radioHattaYukle.setChecked(true);
                    else if ("Söküm".equals(record.getType())) radioSokum.setChecked(true);

                    // Hat setleme
                    if ("Hat 1".equals(record.getHat())) radioGroupHat.check(R.id.radioHat1);
                    else if ("Hat 2".equals(record.getHat())) radioGroupHat.check(R.id.radioHat2);
                    else if ("Hat 3".equals(record.getHat())) radioGroupHat.check(R.id.radioHat3);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditItemActivity.this, "Kayıt yüklenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveRecord() {
        if (currentRecord != null) {
            currentRecord.setBarcode(etBarcode.getText().toString());
            currentRecord.setReason(etReason.getText().toString());

            // İşlem tipi al
            int selectedTypeId = radioGroupType.getCheckedRadioButtonId();
            RadioButton selectedType = findViewById(selectedTypeId);
            currentRecord.setType(selectedType.getText().toString());

            // Hat tipi al
            int selectedHatId = radioGroupHat.getCheckedRadioButtonId();
            RadioButton selectedHat = findViewById(selectedHatId);
            currentRecord.setHat(selectedHat.getText().toString());

            recordRepository.saveRecord(
                    currentRecord.getType(),
                    currentRecord.getBarcode(),
                    currentRecord.getHat(),
                    currentRecord.getReason(),
                    new IRepositoryCallback<Void>() {
                        @Override
                        public void onStart() {}

                        @Override
                        public void onComplete(Void unused) {
                            Toast.makeText(EditItemActivity.this, "Kayıt güncellendi", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(EditItemActivity.this, "Kayıt güncellenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Kayıt yüklenmedi.", Toast.LENGTH_SHORT).show();
        }
    }

    private void scanBarcode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Barkod okut");
        integrator.setBeepEnabled(true);
        integrator.setCaptureActivity(PortraitCaptureActivity.class);
        integrator.initiateScan();
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