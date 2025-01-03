package com.example.assistant;

/**
 * Сущность контрольной точки.
 */
public class ControlPoint {
    /**
     * Название контрольной точки.
     */
    private String name;

    /**
     * Оценка.
     */
    private String mark;

    /**
     * Рейтинг, то есть проценты.
     */
    private String rating;

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}

