package com.example.weatherforcastapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforcastapp.databinding.ActivityAiBinding;
import com.example.weatherforcastapp.ui.AIAdapter;

import java.util.ArrayList;
import java.util.List;

public class AIActivity extends AppCompatActivity {
    private AIAdapter adapter;
    private RecyclerView recyclerView;
    private ActivityAiBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recyclerView = binding.rvSuggestions;

        List<String> list = new ArrayList<>();
        list.add("Thời tiết hôm nay th nào?");
        list.add("Dự báo 3 ngày");
        list.add("Nhiệt độ cảm nhận của nhiệt độ");

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AIAdapter(list, suggestion -> {

            binding.edtInput.setText(suggestion);//lay du lieu tu item vao o input

            binding.edtInput.setSelection(
                    binding.edtInput.getText().length()
            );//dua con tro ve cuoi dong
        });
        recyclerView.setAdapter(adapter);

        binding.buttonBackForecast.setOnClickListener(view -> {//su kien de quay lai trang dư bao thoi tiet
            finish();
        });
        binding.btnSend.setOnClickListener(view -> {//su kien nhan nut de gui
            String message = binding.edtInput.getText().toString();
        });

        binding.edtInput.addTextChangedListener(new TextWatcher() {//su kien khi nhap du lieu vao input
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                String text = s.toString().trim();

                if (text.isEmpty()) {
                    binding.btnSend.setEnabled(false);
                    binding.btnSend.setBackgroundTintList(
                            ColorStateList.valueOf(Color.parseColor("#808080"))
                    );
                } else {
                    binding.btnSend.setEnabled(true);
                    binding.btnSend.setBackgroundTintList(
                            ColorStateList.valueOf(Color.parseColor("#1E3C72"))
                    );
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }
}
