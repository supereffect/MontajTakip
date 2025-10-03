

package com.egehan.montajhattitakip.Repository.Concrete;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.LinearLayout;

import com.egehan.montajhattitakip.Model.Record;
import com.egehan.montajhattitakip.Repository.Abstract.IRepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dagger.hilt.android.qualifiers.ApplicationContext;

import javax.inject.Inject;
import javax.inject.Singleton; // ✅ düzeltme
@Singleton

public class RecordRepository {


    private FirebaseFirestore db;
    private CollectionReference recordsRef;
    private CollectionReference historyRef;
    Context context;
    LinearLayout progressBar;
    @Inject
    public RecordRepository(@ApplicationContext Context context, FirebaseFirestore db) {
        this.context = context;
        this.db = db;
        recordsRef = db.collection("records");         // Güncel kayıtlar
        historyRef = db.collection("recordHistory");   // Tüm log kayıtları
    }

    public void saveRecord(
            String type, String barcode, String hat, String reason,
            final IRepositoryCallback<Void> callback) {

        callback.onStart();


        if (barcode == null || barcode.trim().isEmpty()) {
            callback.onError(new Exception("Barcode boş olamaz"));
            return;
        }

        Date now = new Date();
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(now);

        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        String shift;
        if (hour >= 8 && hour < 16) {
            shift = "Gündüz";
        } else if (hour >= 16 && hour < 24) {
            shift = "Akşam";
        } else {
            shift = "Gece";
        }

        // Yeni Document Reference oluştur (Firestore ID alınacak)
//        String docId = historyRef.document().getId();
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        Record record = new Record(docId, user.getEmail(), type, barcode, hat, reason, timestamp, shift);



        String docId = historyRef.document().getId();

// SharedPreferences'ten email al
        SharedPreferences prefs = context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("email", "unknown@montaj.com"); // default değer

        Record record = new Record(docId, email, type, barcode, hat, reason, timestamp, shift);
        // Güncel kayıtlar
        recordsRef.document(barcode)
                .set(record)
                .addOnSuccessListener(aVoid -> {
                    // Geçmiş kayıt ekleme
                    historyRef.document(docId) // ID’yi aynı kullan
                            .set(record)
                            .addOnSuccessListener(documentReference -> {
                                callback.onComplete(null);
                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                callback.onError(e);
                            });
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onError(e);
                });
    }



    // Barcode’a göre güncel kayıt çekme
    public void getRecordByBarcode(String barcode, final IRepositoryCallback<Record> callback) {
        callback.onStart();

        historyRef
                .whereEqualTo("barcode", barcode)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);

                        try {
                            Record record = doc.toObject(Record.class);
                            record.setId(doc.getId());
                            callback.onComplete(record);
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.onError(e);
                        }
                    } else {
                        Exception e = task.getException() != null ? task.getException() : new Exception("Kayıt bulunamadı");
                        callback.onError(e);
                    }
                });
    }

    // Tüm geçmiş logları çekme
    public void getAllHistory(final IRepositoryCallback<List<Record>> callback) {
        callback.onStart();

        historyRef.get()
                .addOnCompleteListener(task -> {
                    List<Record> historyList = new ArrayList<>();

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            try {
                                Record record = doc.toObject(Record.class);
                                record.setId(doc.getId()); // ID’yi kaydet
                                historyList.add(record);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        callback.onComplete(historyList);
                    } else {
                        Exception e = task.getException() != null ? task.getException() : new Exception("Veri çekilemedi");
                        callback.onError(e);
                    }
                });
    }


    public void deleteRecordById(String id, final IRepositoryCallback<Void> callback) {
        callback.onStart();

        // 1. Güncel kayıtlardan sil
        recordsRef.document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    // 2. Geçmiş kayıtlar koleksiyonundan sil
                    historyRef.document(id).delete()
                            .addOnSuccessListener(aVoid2 -> callback.onComplete(null))
                            .addOnFailureListener(e -> callback.onError(e));
                })
                .addOnFailureListener(e -> callback.onError(e));
    }
    public void listenToHistoryChanges(IRepositoryCallback<List<Record>> callback) {
        historyRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                callback.onError(error);
                return;
            }

            List<Record> updatedList = new ArrayList<>();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Record record = doc.toObject(Record.class);
                    record.setId(doc.getId()); // id ekle
                    updatedList.add(record);
                }
            }
            callback.onComplete(updatedList);
        });
    }
    public void listenToLatestRecords(IRepositoryCallback<List<Record>> callback) {
        historyRef.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }

                    Map<String, Record> latestRecordsMap = new HashMap<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Record record = doc.toObject(Record.class);
                            record.setId(doc.getId()); // ID ekle
                            String barcode = record.getBarcode();

                            // Barcode bazında en son kaydı tut
                            if (!latestRecordsMap.containsKey(barcode)) {
                                latestRecordsMap.put(barcode, record);
                            }
                        }
                    }
                    callback.onComplete(new ArrayList<>(latestRecordsMap.values()));
                });
    }

    public void getRecordById(String id, final IRepositoryCallback<Record> callback) {
        callback.onStart();

        historyRef.document(id).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Record record = task.getResult().toObject(Record.class);
                        record.setId(task.getResult().getId());
                        callback.onComplete(record);
                    } else {
                        Exception e = task.getException() != null ? task.getException() : new Exception("Kayıt bulunamadı");
                        callback.onError(e);
                    }
                });
    }


    // Listener arayüzleri
}
