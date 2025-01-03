package com.example.assistant;
/**
 * Сущность дисциплины (предмета).
 */
public class Discipline {
    /**
     * Название дисциплины (предмета).
     */
    private String name;

    /**
     * ID журнала. Используется для запроса на сервер для получения оценок.
     */
    private String journalId;

    /**
     * Семестр дисциплины.
     */
    private String semestr;

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJournalId() {
        return journalId;
    }

    public void setJournalId(String journalId) {
        this.journalId = journalId;
    }

    public String getSemestr() {
        return semestr;
    }

    public void setSemestr(String semestr) {
        this.semestr = semestr;
    }
}