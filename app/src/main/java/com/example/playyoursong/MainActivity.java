package com.example.playyoursong;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Список для хранения нот
    private final List<Note> notes = new ArrayList<>();

    // Код для идентификации запроса разрешения
    private static final int REQUEST_CODE_PERMISSION = 100;

    // Переменная для хранения выбранного аудио URI
    private Uri selectedAudioUri;

    // Текстовое поле, которое будет отображать имя выбранного аудио
    private TextView selectedAudioText;

    // Переменная для воспроизведения выбранного аудиофайла
    private MediaPlayer mediaPlayer;

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

    /**
     * Метод для остановки текущего аудио
     */
    private void stopCurrentAudio() {
        // Проверяем, существует ли mediaPlayer и воспроизводит ли аудио
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // Останавливаем воспроизведение
            mediaPlayer.release(); // Освобождаем ресурсы
            mediaPlayer = null; // Устанавливаем переменную в null для избежания утечек памяти
        }
    }

    // Метод для создания и добавления нот
    private void addNoteToGame(int position, long startTime) {
        Note note = new Note(position, startTime); // Создаем новую ноту
        notes.add(note); // Добавляем созданную ноту в коллекцию
    }


    /**
     * Метод для генерации нот, например: генерируем 5 нот, каждая появляется каждые 1000 мс
     */
    private void generateNotes() {
        //
        for (int i = 0; i < 5; i++) {
            int position = 100 + (i * 100); // Позиция по оси Y
            long startTime = i * 1000; // Время появления
            notes.add(new Note(position, startTime)); // Добавляем ноту в список
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Устанавливаем макет из XML

        // Находим кнопку "Выбрать аудио" в макете
        Button selectAudioButton = findViewById(R.id.select_audio_button);

        // Находим текстовое поле, которое будет отображать имя выбранного аудио
        selectedAudioText = findViewById(R.id.selected_audio_text);

        // Устанавливаем обработчик нажатия на кнопку "Выбрать аудио"
        selectAudioButton.setOnClickListener(v -> checkPermissionAndSelectAudio());

        // Находим кнопку "Начать игру" в макете
        Button startGameButton = findViewById(R.id.start_game_button);

        // Устанавливаем обработчик нажатия на кнопку "Начать игру"
        startGameButton.setOnClickListener(v -> playSelectedAudio()); // Вызываем метод воспроизведения аудио

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
                // Если разрешение уже дано, останавливаем текущее аудио (если есть) и открываем выбор аудио
                stopCurrentAudio();  // Остановим текущее аудио перед выбором нового
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
                // Если разрешение уже дано, останавливаем текущее аудио (если есть) и открываем выбор аудио
                stopCurrentAudio();  // Остановим текущее аудио перед выбором нового
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
            // Получаем имя файла аудио из URI
            String audioName = selectedAudioUri.getLastPathSegment();
            // Устанавливаем текст с использованием строки из ресурсов
            selectedAudioText.setText(getString(R.string.selected_audio, audioName));

            // Если есть медиаплеер, останавливаем его перед началом нового воспроизведения
            if (mediaPlayer != null) {
                mediaPlayer.release(); // Освобождаем ресурсы медиаплеера
            }

            // Создаем новый экземпляр MediaPlayer
            mediaPlayer = MediaPlayer.create(this, selectedAudioUri);
            mediaPlayer.start(); // Запускаем воспроизведение аудио

            // Пример добавления нот
            addNoteToGame(100, mediaPlayer.getCurrentPosition() + 1000); // Добавляем ноту через 1 секунду
            addNoteToGame(200, mediaPlayer.getCurrentPosition() + 2000); // Добавляем ноту через 2 секунды

            // Устанавливаем обработчик завершения воспроизведения
            mediaPlayer.setOnCompletionListener(mp -> {
                // Действия при завершении воспроизведения аудио
                // Например, очищаем список нот
                notes.clear(); // Очищаем список нот
                selectedAudioText.setText(R.string.audio_not_selected); // Обновляем текст, когда аудио закончится
            });
        } else {
            // Если файл не выбран, показываем сообщение "Аудио не выбрано"
            selectedAudioText.setText(R.string.audio_not_selected);
        }
    }

    /**
     * Метод для воспроизведения выбранного аудиофайла
     */
    private void playSelectedAudio() {
        // Проверяем, выбрано ли аудио
        if (selectedAudioUri != null) {
            // Если есть активный MediaPlayer, останавливаем его и освобождаем ресурсы
            if (mediaPlayer != null) {
                mediaPlayer.stop(); // Останавливаем текущее воспроизведение
                mediaPlayer.release(); // Освобождаем ресурсы
            }

            // Создаем новый экземпляр MediaPlayer для воспроизведения выбранного аудиофайла
            mediaPlayer = MediaPlayer.create(this, selectedAudioUri);
            mediaPlayer.start(); // Начинаем воспроизведение
        } else {
            // Если аудио не выбрано, показываем сообщение об ошибке
            Toast.makeText(this, R.string.audio_not_selected, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Метод onDestroy(), чтобы освободить ресурсы MediaPlayer при закрытии активности
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();  // Вызываем метод родительского класса
        // Если mediaPlayer существует, освобождаем его ресурсы
        if (mediaPlayer != null) {
            mediaPlayer.release();  // Освобождаем ресурсы MediaPlayer
            mediaPlayer = null;  // Устанавливаем переменную в null для избежания утечек памяти
        }
    }
}
