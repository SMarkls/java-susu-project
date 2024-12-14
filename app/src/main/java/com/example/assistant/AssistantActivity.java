
package com.example.assistant;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AssistantActivity extends AppCompatActivity {
    private TableLayout scoreTable;
    private TextView text;
    private final List<DisciplineViewModel> disciplineList = new ArrayList<>();
    private final DisciplineLoader loader = new DisciplineLoader(this, disciplineList, this::updateTableFromList);

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
        loadButton.setOnClickListener(v -> {
            loader.showLoadDialog(getFilesDir());
        });
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
                    onScoreChanged(s.toString());
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
                    onWeightChanged(s.toString());
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
        deleteButton.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, (float)0.7));
        deleteButton.setText("-");
        deleteButton.setOnClickListener(v -> {
            scoreTable.removeView(newRow);
            disciplineList.remove(item);
            reCalculateValues();
        });
        return deleteButton;
    }

    private void onScoreChanged(String newValue) {
        reCalculateValues();
    }

    private void onWeightChanged(String newValue) {
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

        text.setText(String.format("Текущий балл %.2f.", scoreSum / weightSum * 100));
    }
}

