package com.example.playyoursong;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Кнопка для выбора аудио
        Button selectAudioButton = findViewById(R.id.select_audio_button);

        // Обработка нажатия на кнопку
        selectAudioButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });
    }

    // Регистрация ActivityResultLauncher для выбора аудио
    private final ActivityResultLauncher<Intent> selectAudioLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedAudioUri = result.getData().getData();
                    playSelectedAudio(selectedAudioUri);  // Воспроизведение аудиофайла
                }
            });

    // Регистрация ActivityResultLauncher для запроса разрешений
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    selectAudio();
                } else {
                    showPermissionRationale();
                }
            });

    // Метод для выбора аудиофайла
    private void selectAudio() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        selectAudioLauncher.launch(intent);
    }

    // Метод для воспроизведения выбранного аудиофайла
    private void playSelectedAudio(Uri audioUri) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, audioUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "Ошибка воспроизведения аудиофайла", e);  // Использование Log.e вместо printStackTrace
        } finally {
            // Освобождение ресурсов после воспроизведения
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        }
    }

    // Показать объяснение необходимости разрешений
    private void showPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Требуется разрешение")
                .setMessage("Этому приложению требуется доступ к аудиофайлам для их воспроизведения.")
                .setPositiveButton("OK", (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO);
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                })
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
