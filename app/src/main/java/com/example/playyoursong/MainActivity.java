package com.example.playyoursong;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView selectedAudioText;
    private Uri selectedAudioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button selectAudioButton = findViewById(R.id.select_audio_button);
        selectedAudioText = findViewById(R.id.selected_audio_text);

        // Лаунчер для выбора аудио
        ActivityResultLauncher<Intent> audioPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedAudioUri = result.getData().getData();
                        if (selectedAudioUri != null) { // Проверка на null
                            String audioName = getFileName(selectedAudioUri); // Метод для получения имени файла
                            selectedAudioText.setText(audioName);
                        }
                    }
                });

        // Кнопка для выбора аудио
        selectAudioButton.setOnClickListener(v -> {
            // Проверяем разрешения
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 1);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }

            // Открываем аудио-пикер для выбора песни
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            audioPickerLauncher.launch(intent);
        });
    }

    // Метод для получения имени аудиофайла
    private String getFileName(Uri uri) {
        return uri.getLastPathSegment(); // Пример получения имени файла из URI
    }
}
