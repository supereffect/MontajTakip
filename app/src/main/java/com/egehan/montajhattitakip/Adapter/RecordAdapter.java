package com.egehan.montajhattitakip.Adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.egehan.montajhattitakip.EditItemActivity;
import com.egehan.montajhattitakip.Model.Record;
import com.egehan.montajhattitakip.R;
import com.google.gson.Gson;

import java.util.List;

public class RecordAdapter extends ArrayAdapter<Record> {
    private Context context;
    private List<Record> records;
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    public RecordAdapter(Context context, List<Record> records, SharedPreferences prefs) {
        super(context, 0, records);
        this.context = context;
        this.records = records;
        this.prefs = prefs;
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
            Intent intent = new Intent(context, EditItemActivity.class);
            intent.putExtra("record_index", position); // hangi kaydı düzenlediğimizi gönderdik
            intent.putExtra("record_data", gson.toJson(record)); // kaydı JSON olarak gönderdik
            context.startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            records.remove(position);
            saveRecords();
            notifyDataSetChanged();
        });

        return convertView;
    }

    private void saveRecords() {
        prefs.edit().putString("records", gson.toJson(records)).apply();
    }
}
