package com.example.playyoursong;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    // Код для идентификации запроса разрешения
    private static final int REQUEST_CODE_PERMISSION = 100;

    // Переменная для хранения выбранного аудио URI
    private Uri selectedAudioUri;

    // Текстовое поле, которое будет отображать имя выбранного аудио
    private TextView selectedAudioText;

    // Лаунчер для получения результата выбора аудио из хранилища
    private final ActivityResultLauncher<Intent> selectAudioLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Если выбор аудио прошел успешно (код результата OK) и есть данные
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Получаем URI выбранного аудиофайла
                    selectedAudioUri = result.getData().getData();
                    // Обновляем текстовое поле с информацией о выбранном аудио
                    updateSelectedAudioText();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Устанавливаем макет из XML

        // Находим кнопку "Выбрать аудио" в макете
        Button selectAudioButton = findViewById(R.id.select_audio_button);

        // Находим текстовое поле, которое будет отображать имя выбранного аудио
        selectedAudioText = findViewById(R.id.selected_audio_text);

        // Устанавливаем обработчик нажатия на кнопку
        selectAudioButton.setOnClickListener(v -> checkPermissionAndSelectAudio());
    }

    /**
     * Метод для проверки разрешений и открытия выбора аудио, если разрешения даны
     */
    private void checkPermissionAndSelectAudio() {
        // Проверяем, на какой версии Android работает устройство
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Если версия Android 13 или выше
            // Проверяем, есть ли разрешение на чтение аудиофайлов
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                // Если разрешение не дано, запрашиваем его
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE_PERMISSION);
            } else {
                // Если разрешение уже дано, открываем выбор аудио
                openAudioPicker();
            }
        } else {  // Если версия Android ниже 13
            // Проверяем, есть ли разрешение на чтение внешнего хранилища
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Если разрешение не дано, запрашиваем его
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            } else {
                // Если разрешение уже дано, открываем выбор аудио
                openAudioPicker();
            }
        }
    }

    /**
     * Метод для открытия окна выбора аудиофайла
     */
    private void openAudioPicker() {
        // Создаем Intent для выбора файла
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");  // Устанавливаем тип файлов (только аудио)
        selectAudioLauncher.launch(intent);  // Запускаем процесс выбора аудио
    }

    /**
     * Обработка результата запроса разрешений
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            // Проверяем, дали ли пользователи запрашиваемое разрешение
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Если разрешение дано, открываем выбор аудио
                openAudioPicker();
            } else {
                // Если разрешение не дано, показываем сообщение об отказе
                Toast.makeText(this, R.string.permission_denied_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Обновляем текстовое поле с именем выбранного аудиофайла
     */
    private void updateSelectedAudioText() {
        if (selectedAudioUri != null) {
            // Получаем имя файла аудио
            String audioName = selectedAudioUri.getLastPathSegment();
            // Устанавливаем текст с использованием строки из ресурсов
            selectedAudioText.setText(getString(R.string.selected_audio, audioName));
        } else {
            // Если файл не выбран, показываем сообщение "Аудио не выбрано"
            selectedAudioText.setText(R.string.audio_not_selected);
        }
    }
}
