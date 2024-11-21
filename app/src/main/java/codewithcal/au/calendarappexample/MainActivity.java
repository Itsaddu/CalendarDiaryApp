package codewithcal.au.calendarappexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();
    }

    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public void previousMonthAction(View view) {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view) {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            String selectedDateString = dayText + " " + monthYearFromDate(selectedDate);
            showDiaryPopup(selectedDateString);
        }
    }

    private void showDiaryPopup(String initialDateString) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_daily_diary);

        // Make the popup window bigger
        dialog.getWindow().setLayout(1100, 800); // Increased dimensions for width and height

        // Initialize views in the dialog
        TextView popupSelectedDate = dialog.findViewById(R.id.popupSelectedDate);
        EditText editTextDiary = dialog.findViewById(R.id.editTextDiary);
        Button buttonSaveDiary = dialog.findViewById(R.id.buttonSaveDiary);
        Button buttonClose = dialog.findViewById(R.id.buttonClose); // Close button
        Button arrowLeft = dialog.findViewById(R.id.arrowLeft);
        Button arrowRight = dialog.findViewById(R.id.arrowRight);

        // Use a mutable container for the date string
        final String[] selectedDateString = {initialDateString};

        // Set the selected date in the title
        popupSelectedDate.setText(selectedDateString[0]);

        // Load the existing diary entry if any
        String existingDiary = loadDiaryForDate(selectedDateString[0]);
        if (existingDiary != null) {
            editTextDiary.setText(existingDiary);
        }

        // Save diary when dialog is dismissed
        dialog.setOnDismissListener(d -> {
            String diaryText = editTextDiary.getText().toString();
            saveDiaryForDate(selectedDateString[0], diaryText);
            Toast.makeText(this, "Diary saved for " + selectedDateString[0], Toast.LENGTH_SHORT).show();
        });

        // Save button functionality
        buttonSaveDiary.setOnClickListener(v -> {
            String diaryText = editTextDiary.getText().toString();
            saveDiaryForDate(selectedDateString[0], diaryText);
            Toast.makeText(this, "Diary saved for " + selectedDateString[0], Toast.LENGTH_SHORT).show();
        });

        // Close button functionality
        buttonClose.setOnClickListener(v -> dialog.dismiss());

        // Arrow buttons for navigation
        arrowLeft.setOnClickListener(v -> {
            selectedDateString[0] = adjustDate(selectedDateString[0], -1);
            popupSelectedDate.setText(selectedDateString[0]);
            editTextDiary.setText(loadDiaryForDate(selectedDateString[0]));
        });

        arrowRight.setOnClickListener(v -> {
            selectedDateString[0] = adjustDate(selectedDateString[0], 1);
            popupSelectedDate.setText(selectedDateString[0]);
            editTextDiary.setText(loadDiaryForDate(selectedDateString[0]));
        });

        dialog.show();
    }



    // Helper method to adjust the date
    private String adjustDate(String currentDateString, int dayOffset) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        LocalDate currentDate = LocalDate.parse(currentDateString, formatter);
        LocalDate adjustedDate = currentDate.plusDays(dayOffset);
        return adjustedDate.format(formatter);
    }

    // Method to save a diary entry for a specific date
    private void saveDiaryForDate(String date, String diaryText) {
        SharedPreferences sharedPreferences = getSharedPreferences("DiaryEntries", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(date, diaryText);
        editor.apply();
    }

    // Method to load a diary entry for a specific date
    private String loadDiaryForDate(String date) {
        SharedPreferences sharedPreferences = getSharedPreferences("DiaryEntries", MODE_PRIVATE);
        return sharedPreferences.getString(date, null);
    }
}
