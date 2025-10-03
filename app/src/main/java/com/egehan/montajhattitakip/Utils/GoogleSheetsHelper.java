package com.egehan.montajhattitakip.Utils;

import android.os.Handler;
import android.os.Looper;

import com.egehan.montajhattitakip.Model.Record;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoogleSheetsHelper {

    private final String baseUrl;
    private final ExecutorService executor;
    private final Handler handler;


    // AKfycbz9A8-8ywWQrjY28td0QuviII3am5VPSUzugybweC26dH_YhV4zvnGcG_NJ5Uu4tvPwBw


    public GoogleSheetsHelper() {
        this.baseUrl = "https://script.google.com/macros/s/AKfycbz9A8-8ywWQrjY28td0QuviII3am5VPSUzugybweC26dH_YhV4zvnGcG_NJ5Uu4tvPwBw/exec";
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void writeRecord(Record record, boolean isHistory) {
        String sheetName = isHistory ? "Gecmis" : "Guncel";

        executor.execute(() -> {
            try {
                String params = "action=write"
                        + "&sheet=" + URLEncoder.encode(sheetName, "UTF-8")
                        + "&timestamp=" + URLEncoder.encode(record.getTimestamp(), "UTF-8")
                        + "&username=" + URLEncoder.encode(record.getUsername(), "UTF-8")
                        + "&type=" + URLEncoder.encode(record.getType(), "UTF-8")
                        + "&barcode=" + URLEncoder.encode(record.getBarcode(), "UTF-8")
                        + "&hat=" + URLEncoder.encode(record.getHat(), "UTF-8")
                        + "&reason=" + URLEncoder.encode(record.getReason(), "UTF-8")
                        + "&shift=" + URLEncoder.encode(record.getShift(), "UTF-8");

                URL url = new URL(baseUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(params.getBytes());
                os.flush();
                os.close();

                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void clearSheet(boolean isHistory) {
        String sheetName = isHistory ? "Gecmis" : "Guncel";

        executor.execute(() -> {
            try {
                String params = "action=clear&sheet=" + URLEncoder.encode(sheetName, "UTF-8");

                URL url = new URL(baseUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(params.getBytes());
                os.flush();
                os.close();

                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void readSheet(boolean isHistory, SheetReadCallback callback) {
        String sheetName = isHistory ? "Gecmis" : "Guncel";

        executor.execute(() -> {
            String result = null;
            try {
                URL url = new URL(baseUrl + "?sheet=" + URLEncoder.encode(sheetName, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                conn.disconnect();
                result = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalResult = result;
            handler.post(() -> {
                if (callback != null) callback.onRead(finalResult);
            });
        });
    }

    public interface SheetReadCallback {
        void onRead(String jsonData);
    }
}
