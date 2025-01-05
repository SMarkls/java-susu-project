
package com.example.assistant;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssistantActivity extends AppCompatActivity {
    private TableLayout scoreTable;
    private TextView text;
    private final Parser parser = new Parser();
    private final List<DisciplineViewModel> disciplineList = new ArrayList<>();
    private final DisciplineLoader loader = new DisciplineLoader(this, disciplineList, this::updateTableFromList);
    private final List<Discipline> disciplines = new ArrayList<>();
    private File storageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        scoreTable = findViewById(R.id.score_table);
        Button addRowButton = findViewById(R.id.add_row_button);
        text = findViewById(R.id.below_table_text);

        updateTableFromList();

        addRowButton.setOnClickListener(v -> {
            String labName = String.format("Lab%d", disciplineList.size());
            addNewDiscipline(labName, 75.0, 1);
            reCalculateValues();
        });

        Button saveButton = findViewById(R.id.save_button);
        Button loadButton = findViewById(R.id.load_button);

        saveButton.setOnClickListener(v -> loader.showSaveDialog(getFilesDir()));
        loadButton.setOnClickListener(v -> loader.showLoadDialog(getFilesDir()));

        findViewById(R.id.btn_open_dialog).setOnClickListener(view -> openAuthDialog(this));
        findViewById(R.id.parse_from_univeris).setOnClickListener(view -> openParseDialog(this, d -> {
            new Thread(() -> {
                try {
                    List<ControlPoint> controlPoints = parser.findAllControlPoints(d.getJournalId());
                    Collections.sort(controlPoints, (p1, p2) -> {
                        return Integer.compare(p1.getOrderNumber(), p2.getOrderNumber());
                    });

                    runOnUiThread(() -> {
                        disciplineList.clear();
                        for (ControlPoint controlPoint : controlPoints) {
                            disciplineList.add(new DisciplineViewModel(
                                    controlPoint.getName(),
                                    Double.parseDouble(controlPoint.getRating()),
                                    1)
                            );
                        }

                        updateTableFromList();
                    });
                } catch (IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(AssistantActivity.this, "Ошибка при парсинге дисциплины!", Toast.LENGTH_LONG).show()
                    );
                }
            }).start();
        }));

        storageFile = new File(getFilesDir(),"creds.txt");
        loadCredentials();
    }

    private void addNewDiscipline(String name, double score, double weight) {
        DisciplineViewModel newItem = new DisciplineViewModel(name, score, weight);
        disciplineList.add(newItem);
        addDisciplineRow(newItem);
    }

    private void addDisciplineRow(@NonNull DisciplineViewModel item) {
        final TableRow newRow = new TableRow(this);
        newRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        final EditText nameEditText = new EditText(this);
        nameEditText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5));
        nameEditText.setHint("Название");
        nameEditText.setSingleLine(true);
        nameEditText.setText(item.getName());
        newRow.addView(nameEditText);

        final EditText scoreEditText = new EditText(this);
        scoreEditText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));
        scoreEditText.setHint("0.0");
        scoreEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        scoreEditText.setSingleLine(true);
        scoreEditText.setText(String.valueOf(item.getScore()));
        newRow.addView(scoreEditText);

        final EditText weightEditText = new EditText(this);
        weightEditText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));
        weightEditText.setHint("0.0");
        weightEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        weightEditText.setSingleLine(true);
        weightEditText.setText(String.valueOf(item.getWeight()));
        newRow.addView(weightEditText);

        Button deleteButton = getButton(item, newRow);
        newRow.addView(deleteButton);

        scoreEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double newScore = Double.parseDouble(s.toString());
                    item.setScore(newScore);
                    onScoreChanged();
                } catch (NumberFormatException ignored) {  }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        weightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double newWeight = Double.parseDouble(s.toString());
                    item.setWeight(newWeight);
                    onWeightChanged();
                } catch (NumberFormatException ignored) {  }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                item.setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        scoreTable.addView(newRow);
    }

    private void updateTableFromList() {
        int childCount = scoreTable.getChildCount();
        if (childCount > 1) {
            scoreTable.removeViews(1, childCount - 1);
        }

        for (DisciplineViewModel item : disciplineList) {
            addDisciplineRow(item);
        }

        reCalculateValues();
    }

    @NonNull
    private Button getButton(DisciplineViewModel item, TableRow newRow) {
        Button deleteButton = new Button(this);
        deleteButton.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, (float)1));
        deleteButton.setText("-");
        deleteButton.setOnClickListener(v -> {
            scoreTable.removeView(newRow);
            disciplineList.remove(item);
            reCalculateValues();
        });
        return deleteButton;
    }

    private void onScoreChanged() {
        reCalculateValues();
    }

    private void onWeightChanged() {
        reCalculateValues();
    }

    private void reCalculateValues() {
        double weightSum = 0;
        double scoreSum = 0;
        for (DisciplineViewModel discipline:
             disciplineList) {
            double currentWeight = discipline.getWeight();
            weightSum += currentWeight;
            scoreSum += discipline.getScore() / 100 * currentWeight;
        }
        if (weightSum == 0) {
            text.setText("");
            return;
        }

        text.setText(String.format("Текущий балл %.2f.", scoreSum / weightSum * 100));
    }

    private void openParseDialog(Context context, Consumer<Discipline> onSelected) {
        String[] disciplineNames = new String[disciplines.size()];

        for (int i = 0; i < disciplines.size(); i++) {
            disciplineNames[i] = disciplines.get(i).getName();
        }

        new AlertDialog.Builder(context)
                .setTitle("Выберите дисциплину")
                .setItems(disciplineNames, (dialog, which) -> {
                    Discipline selected = disciplines.get(which);
                    onSelected.accept(selected);
                })
                .show();
    }

    private void openAuthDialog(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText etLogin = new EditText(context);
        etLogin.setHint("Логин");
        if (parser.getLogin() != null) {
            etLogin.setText(parser.getLogin());
        }

        layout.addView(etLogin);

        EditText etPassword = new EditText(context);
        etPassword.setHint("Пароль");
        if (parser.getPassword() != null) {
            etPassword.setText(parser.getPassword());
        }

        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPassword);

        EditText etSemester = new EditText(context);
        etSemester.setHint("Номер семестра");
        etSemester.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etSemester);

        new AlertDialog.Builder(context)
                .setTitle("Введите данные")
                .setView(layout)
                .setPositiveButton("ОК", (dialog, which) -> {
                    String login = etLogin.getText().toString();
                    String password = etPassword.getText().toString();
                    String semester = etSemester.getText().toString();

                    if (!login.isEmpty() && !password.isEmpty() && !semester.isEmpty()) {
                        parseAllDisciplines(login, password, semester);
                    } else {
                        Toast.makeText(context, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void parseAllDisciplines(String login, String password, String semester) {
        new Thread(() -> {
            parser.setLogin(login);
            parser.setPassword(password);

            try {
                parser.configureAuthTokens();
                List<Discipline> fetchedDisciplines = parser.findAllDisciplines(semester);

                runOnUiThread(() -> {
                    this.disciplines.clear();
                    this.disciplines.addAll(fetchedDisciplines);
                    Toast.makeText(AssistantActivity.this, "Авторизация успешна", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(AssistantActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    protected void onStop() {
        saveCredentials();
        super.onStop();
    }

    private void saveCredentials() {
        String login = parser.getLogin();
        String password = parser.getPassword();

        // Пишем две строки: login + пароль
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile))) {
            writer.write(login);
            writer.newLine();
            writer.write(password);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCredentials() {
        if (!storageFile.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(storageFile))) {
            String login = br.readLine();     // первая строка
            String password = br.readLine();  // вторая строка

            // Устанавливаем в parser
            parser.setLogin(login);
            parser.setPassword(password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

