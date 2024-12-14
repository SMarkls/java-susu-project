package com.example.assistant;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class DisciplineLoader {
    private final Context ctx;
    private final List<DisciplineViewModel> disciplineList;
    private final Runnable callback;

    public DisciplineLoader(Context ctx, List<DisciplineViewModel> disciplineList, Runnable callback) {
        this.ctx = ctx;
        this.disciplineList = disciplineList;
        this.callback = callback;
    }
    public void showSaveDialog(File dir) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Введите название дисциплины");

        final EditText input = new EditText(ctx);
        input.setHint("Название дисциплины");
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String disciplineName = input.getText().toString().trim();
            if (!disciplineName.isEmpty()) {
                saveDisciplineList(disciplineName, dir);
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void saveDisciplineList(String name, File dir) {
        Gson gson = new Gson();
        String json = gson.toJson(disciplineList);

        File file = new File(dir, name + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showLoadDialog(File dir) {
        File[] files = dir.listFiles((d, s) -> s.endsWith(".json"));

        if (files == null || files.length == 0) {
            Toast.makeText(ctx, "Нет сохранённых дисциплин", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName().replace(".json", "");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Выберите дисциплину");
        builder.setItems(names, (dialog, which) -> {
            String selectedName = names[which];
            showDisciplineOptionsDialog(selectedName, dir);
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void showDisciplineOptionsDialog(String name, File dir) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Действия над \"" + name + "\"");

        builder.setPositiveButton("Загрузить", (dialog, which) -> {
            loadDisciplineList(name, dir);
            callback.run();
        });

        builder.setNegativeButton("Удалить", (dialog, which) -> {
            deleteDisciplineFile(name, dir);
        });

        builder.setNeutralButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void deleteDisciplineFile(String name, File dir) {
        File file = new File(dir, name + ".json");
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Toast.makeText(ctx, "Дисциплина \"" + name + "\" удалена", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ctx, "Не удалось удалить файл", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ctx, "Файл не найден", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadDisciplineList(String name, File dir) {
        File file = new File(dir, name + ".json");

        if (!file.exists()) {
            Toast.makeText(ctx, "Файл не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<DisciplineViewModel>>() {}.getType();
            List<DisciplineViewModel> loadedList = gson.fromJson(reader, type);
            disciplineList.clear();
            disciplineList.addAll(loadedList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
