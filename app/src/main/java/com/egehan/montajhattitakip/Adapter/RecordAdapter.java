package com.egehan.montajhattitakip.Adapter;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.egehan.montajhattitakip.Activities.EditItemActivity;
import com.egehan.montajhattitakip.Activities.LoginActivity;
import com.egehan.montajhattitakip.Model.Record;
import com.egehan.montajhattitakip.R;
import com.egehan.montajhattitakip.Repository.Abstract.IRepositoryCallback;
import com.egehan.montajhattitakip.Repository.Concrete.RecordRepository;
import com.google.gson.Gson;

import java.util.List;

import javax.inject.Inject;

public class RecordAdapter extends ArrayAdapter<Record> {
    private Context context;
    private List<Record> records;
    private Gson gson = new Gson();
    @Inject
    RecordRepository repository;

    public RecordAdapter(Context context, List<Record> records, RecordRepository repository) {
        super(context, 0, records);
        this.context = context;
        this.records = records;
        this.repository = repository; // Hilt değil manuel inject
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.recoord_list_item, parent, false);
        }

        Record record = records.get(position);

        TextView tvRecord = convertView.findViewById(R.id.tvRecord);
        tvRecord.setText(record.getTimestamp() + " - " + record.getUsername() + " - " + record.getType() + " - " + record.getBarcode());

        Button btnEdit = convertView.findViewById(R.id.btnEdit);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);

        btnEdit.setOnClickListener(v -> {
//            Toast.makeText(context, "Admin yetkisi gerekli!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, EditItemActivity.class);
            intent.putExtra("record_id", record.getId()); // sadece id gönderiyoruz
            context.startActivity(intent);

        });

        btnDelete.setOnClickListener(v -> {
            if (LoginActivity.email.equals("admin"))
                deleteRecord(record.getId(), position);
            else
                Toast.makeText(context, "Admin yetkisi gerekli!", Toast.LENGTH_SHORT).show();

        });

        return convertView;
    }

    private void deleteRecord(String recordId, int position) {
        repository.deleteRecordById(recordId, new IRepositoryCallback<Void>() {
            @Override
            public void onStart() {
                // ProgressBar gösterebilirsin
            }

            @Override
            public void onComplete(Void result) {
//                recor ds.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "Kayıt silindi", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(context, "Silme hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
