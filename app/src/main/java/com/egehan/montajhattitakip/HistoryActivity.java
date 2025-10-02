package com.egehan.montajhattitakip;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.egehan.montajhattitakip.Adapter.RecordAdapter;
import com.egehan.montajhattitakip.Model.Record;
import com.google.gson.Gson;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    ListView listView;
    Button btnExport, btnClear;
    SharedPreferences prefs;
    Gson gson = new Gson();
    List<Record> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        listView = findViewById(R.id.listView);
        btnExport = findViewById(R.id.btnExport);
        btnClear = findViewById(R.id.btnClear);

        prefs = getSharedPreferences("AppData", MODE_PRIVATE);

        loadRecords();

        btnClear.setOnClickListener(v -> {
            prefs.edit().remove("records").apply();
            loadRecords();
        });

        btnExport.setOnClickListener(v -> exportToExcel());
    }

    private void loadRecords() {
        String json = prefs.getString("records", "[]");
        Record[] recordsArray = gson.fromJson(json, Record[].class);
        list = new ArrayList<>();
        if (recordsArray != null) {
            list.addAll(Arrays.asList(recordsArray));
        }

        RecordAdapter adapter = new RecordAdapter(this, list, prefs);
        listView.setAdapter(adapter);
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

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "kayitlar.xlsx");
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
