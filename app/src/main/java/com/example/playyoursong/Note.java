// Note.java - представляет каждую ноту, включая её позицию на экране и время появления.
package com.example.playyoursong;

public class Note {
    private final int position; // Позиция ноты по оси Y
    private final long startTime; // Время, когда нота должна появиться

    // Конструктор
    public Note(int position, long startTime) {
        this.position = position;
        this.startTime = startTime;
    }

    // Геттеры
    public int getPosition() {
        return position;
    }

    public long getStartTime() {
        return startTime;
    }
}
