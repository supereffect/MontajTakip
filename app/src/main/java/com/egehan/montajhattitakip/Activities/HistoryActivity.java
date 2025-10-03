package com.egehan.montajhattitakip.Activities;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.egehan.montajhattitakip.Adapter.RecordAdapter;
import com.egehan.montajhattitakip.Model.Record;
import com.egehan.montajhattitakip.R;
import com.egehan.montajhattitakip.Repository.Abstract.IRepositoryCallback;
import com.egehan.montajhattitakip.Repository.Concrete.RecordRepository;
import com.google.api.Distribution;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class HistoryActivity extends AppCompatActivity {
    ListView listView;
    Button btnExport, btnClear;

    @Inject
    RecordRepository repository;
    List<Record> list = new ArrayList<>();
    RecordAdapter adapter;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        listView = findViewById(R.id.listView);
        btnExport = findViewById(R.id.btnExport);
        btnClear = findViewById(R.id.btnClear);
        progressBar = findViewById(R.id.progressBar);


        adapter = new RecordAdapter(this, list, repository); // prefs yok artık
        listView.setAdapter(adapter);

        listenRecords() ;

        btnClear.setOnClickListener(v -> clearHistory());
        btnExport.setOnClickListener(v -> exportToExcel());
    }


    private void listenRecords() {
        progressBar.setVisibility(View.VISIBLE);

        repository.listenToHistoryChanges(new IRepositoryCallback<List<Record>>() {
            @Override
            public void onStart() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onComplete(List<Record> result) {
                progressBar.setVisibility(View.GONE);

                // Tarihe göre sıralama (yeniden eskiye)
                result.sort((r1, r2) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                        Date date1 = sdf.parse(r1.getTimestamp());
                        Date date2 = sdf.parse(r2.getTimestamp());
                        return date2.compareTo(date1); // büyükten küçüğe
                    } catch (Exception e) {
                        e.printStackTrace();
                        return 0;
                    }
                });

                list.clear();
                list.addAll(result);
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void clearHistory() {
        // Firestore'daki history koleksiyonunu temizlemek için:
        listenRecords();
    }

    private void exportToExcel() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Kayıtlar");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Tarih");
            header.createCell(1).setCellValue("Kullanıcı");
            header.createCell(2).setCellValue("İşlem");
            header.createCell(3).setCellValue("Barkod");
            header.createCell(4).setCellValue("Hat");
            header.createCell(5).setCellValue("Neden");
            header.createCell(6).setCellValue("Vardiya");

            for (int i = 0; i < list.size(); i++) {
                Record r = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(r.getTimestamp());
                row.createCell(1).setCellValue(r.getUsername());
                row.createCell(2).setCellValue(r.getType());
                row.createCell(3).setCellValue(r.getBarcode());
                row.createCell(4).setCellValue(r.getHat());
                row.createCell(5).setCellValue(r.getReason());
                row.createCell(6).setCellValue(r.getShift());
            }

            // Excel dosya adı: uygulamaadı_yyyyMMdd_HHmmss.xlsx
            String appName = getString(R.string.app_name).replaceAll("\\s+", "_");
            String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = appName + "_" + dateTime + ".xlsx";

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            FileOutputStream out = new FileOutputStream(file);
            workbook.write(out);
            out.close();
            workbook.close();

            Toast.makeText(this, "Excel kaydedildi: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
