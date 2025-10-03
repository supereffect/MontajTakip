package com.egehan.montajhattitakip.Activities;

import static com.egehan.montajhattitakip.Utils.GeneralParameters.recordDB;
import static com.egehan.montajhattitakip.Utils.GeneralParameters.recordHistoryDB;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.egehan.montajhattitakip.Model.Record;
import com.egehan.montajhattitakip.R;
import com.egehan.montajhattitakip.Repository.Abstract.IRepositoryCallback;
import com.egehan.montajhattitakip.Repository.Concrete.RecordRepository;
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

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    EditText etBarcode, etReason;
    LinearLayout llSummaryInfo;
    TextView tvYerdekiUrunSayisi, tvHattakiUrunSayisi, tvSokulenUrunSayisi;
    CheckBox chkRemoved;
    Button btnScan, btnYereAl, btnHattaYukle, btnHistory;
    SharedPreferences prefs;
    RadioGroup radioGroupHat;

    @Inject
    RecordRepository repository;

    Gson gson = new Gson();
    private ProgressBar progressBar;
    private static final int REQUEST_CODE_BARCODE1 = 493741;
    private static final int REQUEST_CODE_BARCODE2 = 493742;
    private EditText currentBarcodeEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        prefs = getSharedPreferences("AppData", MODE_PRIVATE);
        radioGroupHat = findViewById(R.id.radioGroupHat);

        etBarcode = findViewById(R.id.etBarcode);
        etReason = findViewById(R.id.etReason);
        btnScan = findViewById(R.id.btnScan);
        btnYereAl = findViewById(R.id.btnYereAl);
        btnHattaYukle = findViewById(R.id.btnHattaYukle);
        btnHistory = findViewById(R.id.btnHistory);
        chkRemoved = findViewById(R.id.chkRemoved);
        tvYerdekiUrunSayisi = findViewById(R.id.tvYerdekiUrunSayisi);
        tvHattakiUrunSayisi = findViewById(R.id.tvHattakiUrunSayisi);
        tvSokulenUrunSayisi = findViewById(R.id.tvSokulenUrunSayisi);
        llSummaryInfo = findViewById(R.id.llSummaryInfo);

        repository.listenToLatestRecords(new IRepositoryCallback<List<Record>>() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(List<Record> records) {
                // Liste güncelle
                updateProductCounts(records);


            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getApplicationContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        addProgressBar();

        String[] hats = {"Hat 1", "Hat 2", "Hat 3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hats);

        btnScan.setOnClickListener(v -> scanBarcode(etBarcode));

        btnYereAl.setOnClickListener(v -> {
            if (!validateInput(true)) return;

            String type = chkRemoved.isChecked() ? "Söküm" : "Yere Al";

            repository.saveRecord(
                    type,
                    etBarcode.getText().toString(),
                    getSelectedHat(),
                    etReason.getText().toString(),
                    new IRepositoryCallback<Void>() {
                        @Override
                        public void onStart() {
                            showProgress();
                        }

                        @Override
                        public void onComplete(Void result) {
                            hideProgress();
                            showToast("Kayıt başarılı");
                        }

                        @Override
                        public void onError(Exception e) {
                            hideProgress();
                            showToast("Hata: " + e.getMessage());
                        }
                    }
            );
        });

        btnHattaYukle.setOnClickListener(v -> {
            if (!validateInput(false)) return;
            repository.saveRecord("Hatta Yükle", etBarcode.getText().toString(), getSelectedHat(), "", new IRepositoryCallback<Void>() {
                @Override
                public void onStart() {
                    showProgress();
                }

                @Override
                public void onComplete(Void result) {
                    hideProgress();
                    showToast("Kayıt başarılı");
                }

                @Override
                public void onError(Exception e) {
                    hideProgress();
                    showToast("Hata: " + e.getMessage());
                }
            });
        });

        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
    }

    private void addProgressBar() {
        progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        FrameLayout rootLayout = findViewById(android.R.id.content);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        rootLayout.addView(progressBar, params);
    }

    private void alarmSummaryInfoLL() {
        // Arka plan rengini değiştirme
        int originalColor = ((ColorDrawable) llSummaryInfo.getBackground()).getColor();
        llSummaryInfo.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));

        llSummaryInfo.postDelayed(() -> {
            llSummaryInfo.setBackgroundColor(originalColor);
        }, 2000); // 2000ms = 2 saniye

        // Bip sesi çalma
        android.media.ToneGenerator toneGen = new android.media.ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 100);
        toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 100); // 500ms bip

        // Titreşim ekleme
        android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500); // eski API'ler için
            }
        }
    }


    private void updateProductCounts(List<Record> records) {
        int yerdeki = 0;
        int hattaki = 0;
        int sokulen = 0;

        for (Record record : records) {
            if ("Yere Al".equals(record.getType())) {
                yerdeki++;
                alarmSummaryInfoLL();
            } else if ("Hatta Yükle".equals(record.getType())) {
                hattaki++;
                alarmSummaryInfoLL();
            } else if ("Söküm".equals(record.getType())) {
                sokulen++;
                alarmSummaryInfoLL();
            }
        }

        tvYerdekiUrunSayisi.setText("Yerdeki " + yerdeki);
        tvHattakiUrunSayisi.setText("Hattaki " + hattaki);
        tvSokulenUrunSayisi.setText("Sökülmüş " + sokulen);
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private boolean validateInput(boolean requireReason) {
        if (radioGroupHat.getCheckedRadioButtonId() == -1) {
            showToast("Lütfen bir hat seçin.");
            return false;
        }
        if (etBarcode.getText().toString().trim().isEmpty()) {
            showToast("Lütfen barkod okutun.");
            return false;
        }
        if (requireReason && etReason.getText().toString().trim().isEmpty()) {
            showToast("Lütfen gerekçe girin.");
            return false;
        }
        return true;
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

    private String getSelectedHat() {
        int selectedId = radioGroupHat.getCheckedRadioButtonId();
        return ((RadioButton) findViewById(selectedId)).getText().toString();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null && result.getContents() != null) {
            String barcodeContent = result.getContents();

            if (currentBarcodeEditText != null) {
                currentBarcodeEditText.setText(barcodeContent);
                Toast.makeText(this, "Barkod okundu: " + barcodeContent, Toast.LENGTH_SHORT).show();
                currentBarcodeEditText = null;

                // Repository'den kayıt çekme
                repository.getRecordByBarcode(barcodeContent, new IRepositoryCallback<Record>() {
                    @Override
                    public void onStart() {
                        // İstersen burada ProgressBar gösterebilirsin
                    }

                    @Override
                    public void onComplete(Record record) {

                        RadioButton radioHat1 = findViewById(R.id.radioHat1);
                        RadioButton radioHat2 = findViewById(R.id.radioHat2);
                        RadioButton radioHat3 = findViewById(R.id.radioHat3);
                        if (record != null) {
                            // Kaydı UI'ya yansıt
                            Toast.makeText(getApplicationContext(),
                                    "Kayıt bulundu: " + record.getType() + " - " + record.getHat(),
                                    Toast.LENGTH_LONG).show();
                            String hat = record.getHat();
                            if (hat != null) {
                                if (hat.equalsIgnoreCase("Hat 1")) {
                                    radioHat1.setChecked(true);
                                } else if (hat.equalsIgnoreCase("Hat 2")) {
                                    radioHat2.setChecked(true);
                                } else if (hat.equalsIgnoreCase("Hat 3")) {
                                    radioHat3.setChecked(true);
                                }
                            }
                            etReason.setText(record.getReason());
                            chkRemoved.setChecked(record.getType().equals("Söküm"));

                        } else {
                            Toast.makeText(getApplicationContext(), "Kayıt bulunamadı", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getApplicationContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(this, "HATA: Barkod hedefi belirlenemedi.", Toast.LENGTH_SHORT).show();
            }
        } else if (result != null && result.getContents() == null) {
            Toast.makeText(this, "Tarama iptal edildi.", Toast.LENGTH_SHORT).show();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}
