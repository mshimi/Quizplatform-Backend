package com.iubh.quizbackend.entity.quiz;

public enum SessionStatus {
    PLANNED,      // erstellt, noch nicht gestartet
    COUNTDOWN,    // 5->0 läuft (startAt gesetzt)
    RUNNING,      // Fragen laufen
    FINISHED,     // regulär beendet
    CANCELLED     // abgebrochen (z. B. Host disconnect)
}
