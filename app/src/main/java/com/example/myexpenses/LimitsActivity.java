package com.example.myexpenses;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.myexpenses.customValueFormatter.IntValueFormatter;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.repository.TransactionRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class LimitsActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    //main window
    private TextView currentChosenMonthAndYear;
    private TextView dailyLimitSetAmount, dailyLimitLeftAmount;
    private TextView monthlyLimitSetAmount, monthlyLimitLeftAmount;
    private TextView expenseTotalSum;
    private TextView expenseMonthlyAverage;
    private BarChart monthlyBarChart;
    //popup
    private PopupWindow popupWindow;
    private EditText setDailyLimit, setMonthlyLimit;

    private TransactionRepository transactionRepository;
    private SharedPreferences sharedPreferences;
    private Calendar calendar;
    private int currentChosenMonth;
    private int currentChosenYear;
    private Float dailyLimit;
    private Float monthlyLimit;
    private double sumOfDailyExpenses;
    private double sumOfMonthlyExpenses;

    private static class BarEntryHolder {
        float xVal;
        float yVal;

        public BarEntryHolder(float xVal, float yVal) {
            this.xVal = xVal;
            this.yVal = yVal;
        }

        public float getxVal() {
            return xVal;
        }

        public float getyVal() {
            return yVal;
        }
    }

    private class MyBarDataSet extends BarDataSet {
        public MyBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            if (getEntryForIndex(index).getY() < monthlyLimit)
                return mColors.get(0);
            else
                return mColors.get(1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_limits);


        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.set_limits));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //onCreate
        transactionRepository = new TransactionRepository(this);
        sharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);

        calendar = Calendar.getInstance();
        int actualDay = calendar.get(Calendar.DAY_OF_MONTH);
        currentChosenMonth = calendar.get(Calendar.MONTH);
        currentChosenYear = calendar.get(Calendar.YEAR);

        //read it only once
        sumOfDailyExpenses = Math.abs(transactionRepository.getSumOfDailyExpenses(actualDay, currentChosenMonth, currentChosenYear));

        //default values if they not have been initialized yet
        dailyLimit = sharedPreferences.getFloat("Daily limit", 1000);
        monthlyLimit = sharedPreferences.getFloat("Monthly limit", 5000);


        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//        setContentView(R.layout.fragment_limits);

        // Inflate the layout for this fragment
        ImageButton previousMonth = findViewById(R.id.previousMonth);

        previousMonth.setOnClickListener(this);
        currentChosenMonthAndYear = findViewById(R.id.currentChosenMonthAndYear);
        ImageButton nextMonth = findViewById(R.id.nextMonth);
        nextMonth.setOnClickListener(this);

        dailyLimitSetAmount = findViewById(R.id.dailyLimitSetAmount);
        dailyLimitLeftAmount = findViewById(R.id.dailyLimitLeftAmount);
        monthlyLimitSetAmount = findViewById(R.id.monthlyLimitSetAmount);
        monthlyLimitLeftAmount = findViewById(R.id.monthlyLimitLeftAmount);
        Button setDailyLimitButton = findViewById(R.id.setDailyLimitButton);
        setDailyLimitButton.setOnClickListener(this);

        expenseMonthlyAverage = findViewById(R.id.expenseMonthlyAverage);
        expenseTotalSum = findViewById(R.id.expenseTotalSum);
        monthlyBarChart = findViewById(R.id.monthlyBarchart);

        updateView();
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_limits, container, false);
        ImageButton previousMonth = view.findViewById(R.id.previousMonth);
        previousMonth.setOnClickListener(this);
        currentChosenMonthAndYear = view.findViewById(R.id.currentChosenMonthAndYear);
        ImageButton nextMonth = view.findViewById(R.id.nextMonth);
        nextMonth.setOnClickListener(this);

        dailyLimitSetAmount = view.findViewById(R.id.dailyLimitSetAmount);
        dailyLimitLeftAmount = view.findViewById(R.id.dailyLimitLeftAmount);
        monthlyLimitSetAmount = view.findViewById(R.id.monthlyLimitSetAmount);
        monthlyLimitLeftAmount = view.findViewById(R.id.monthlyLimitLeftAmount);
        Button setDailyLimitButton = view.findViewById(R.id.setDailyLimitButton);
        setDailyLimitButton.setOnClickListener(this);

        expenseMonthlyAverage = view.findViewById(R.id.expenseMonthlyAverage);
        expenseTotalSum = view.findViewById(R.id.expenseTotalSum);
        monthlyBarChart = view.findViewById(R.id.monthlyBarchart);

        updateView();

        return view;
    }

    @SuppressLint({"DefaultLocale", "ClickableViewAccessibility"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previousMonth: {
                if (currentChosenMonth == 0) { //months are indexed starting from zero
                    currentChosenMonth = 11;
                    currentChosenYear--;
                } else {
                    currentChosenMonth--;
                }
                updateView();
                break;
            }

            case R.id.nextMonth: {
                if (currentChosenMonth == 11) { //months are indexed starting from zero
                    currentChosenMonth = 0;
                    currentChosenYear++;
                } else {
                    currentChosenMonth++;
                }
                updateView();
                break;
            }
            case R.id.setDailyLimitButton: {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                //popupWindow
                View popupView = inflater.inflate(R.layout.popup_change_limits, null);

                int popupWidth = 565;
                int popupHeight = 548;
                popupWindow = new PopupWindow(popupView, popupWidth, popupHeight, true);
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, -125);

                //if the touch is outside the bounds the touch is consumed and not passed to the popupWindow so it is not dismissed
                popupWindow.setTouchInterceptor((v12, event) -> {
                    if (event.getX() < 0 || event.getX() > popupWidth) return true;
                    if (event.getY() < 0 || event.getY() > popupHeight) return true;

                    return false;
                });

                //dim background when popup shows
                View container = (View) popupWindow.getContentView().getParent();
                WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) container.getLayoutParams();
                layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                layoutParams.dimAmount = 0.6f;
                windowManager.updateViewLayout(container, layoutParams);

                Toolbar toolbar = popupView.findViewById(R.id.tool_bar);
                ImageButton toolbarClose = popupView.findViewById(R.id.toolbarClose);
                toolbarClose.setOnClickListener(this);

                TextView toolbarText = popupView.findViewById(R.id.toolbarText);
                toolbarText.setText("Set limits");

                setDailyLimit = popupView.findViewById(R.id.setDailyLimit);
                setDailyLimit.setText(String.format("%.2f", dailyLimit));
                setDailyLimit.setOnTouchListener(this);

                setMonthlyLimit = popupView.findViewById(R.id.setMonthlyLimit);
                setMonthlyLimit.setText(String.format("%.2f", monthlyLimit));
                setMonthlyLimit.setOnTouchListener(this);

                Button saveNewLimitsButton = popupView.findViewById(R.id.saveNewLimitsButton);
                saveNewLimitsButton.setOnClickListener(this);

                break;
            }

            case R.id.saveNewLimitsButton: {

                SharedPreferences.Editor editor = sharedPreferences.edit();

                //check if amount is empty if so we set it as zero
                try {
                    dailyLimit = Float.parseFloat(String.valueOf(setDailyLimit.getText()));
                } catch (NumberFormatException e) {
                    dailyLimit = 0f;
                }

                //check if amount is empty if so we set it as zero
                try {
                    monthlyLimit = Float.parseFloat(String.valueOf(setMonthlyLimit.getText()));
                } catch (NumberFormatException e) {
                    monthlyLimit = 0f;
                }

                editor.putFloat("Daily limit", dailyLimit);
                editor.putFloat("Monthly limit", monthlyLimit);
                editor.apply();

                updateView();
//
//                Snackbar snackbar = Snackbar.make(getView(), "New limits set", Snackbar.LENGTH_LONG);
//                snackbar.show();

                popupWindow.dismiss();
                break;
            }

            case R.id.toolbarClose: {
                Float valueOfSetDailyLimit;
                try {
                    valueOfSetDailyLimit = Float.valueOf(setDailyLimit.getText().toString());
                } catch (NumberFormatException e) {
                    valueOfSetDailyLimit = 0f;
                }

                Float valueOfSetMonthlyLimit;
                try {
                    valueOfSetMonthlyLimit = Float.valueOf(setMonthlyLimit.getText().toString());
                } catch (NumberFormatException e) {
                    valueOfSetMonthlyLimit = 0f;
                }

                if (!valueOfSetDailyLimit.equals(dailyLimit) || !valueOfSetMonthlyLimit.equals(monthlyLimit)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(false);
                    builder.setMessage(R.string.unsaved_data_message);
                    builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> popupWindow.dismiss());
                    builder.setNegativeButton(R.string.limit_no_button, (dialog, which) -> {
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    popupWindow.dismiss();
                }
                break;
            }

            default:
                break;
        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateView() {
        currentChosenMonthAndYear.setText(convertMonthToString(currentChosenMonth + 1, currentChosenYear)); //months are indexed starting from zero

        calendar.set(Calendar.MONTH, currentChosenMonth);
        calendar.set(Calendar.YEAR, currentChosenYear);
        int numberOfDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<Transaction> monthlyExpenses = transactionRepository.findExpensesInMonth(currentChosenMonth, currentChosenYear);
        Collections.sort(monthlyExpenses); //dates must be in order

        sumOfMonthlyExpenses = monthlyExpenses.stream()
                .mapToDouble(o -> Math.abs(o.getAmount()))
                .sum();

        dailyLimitSetAmount.setText(String.format("%.2f", dailyLimit));
        monthlyLimitSetAmount.setText(String.format("%.2f", monthlyLimit));

        if (dailyLimit - sumOfDailyExpenses < 0) {
            dailyLimitLeftAmount.setText(String.format("%.2f", dailyLimit - sumOfDailyExpenses));
            dailyLimitLeftAmount.setTextColor(ContextCompat.getColor(Objects.requireNonNull(this), R.color.limit_reached));
        } else {
            dailyLimitLeftAmount.setText(String.format("+%.2f", dailyLimit - sumOfDailyExpenses));
            dailyLimitLeftAmount.setTextColor(ContextCompat.getColor(Objects.requireNonNull(this), R.color.sum_greater_than_zero));
        }

        if (monthlyLimit - sumOfMonthlyExpenses < 0) {
            monthlyLimitLeftAmount.setText(String.format("%.2f", monthlyLimit - sumOfMonthlyExpenses));
            monthlyLimitLeftAmount.setTextColor(ContextCompat.getColor(Objects.requireNonNull(this), R.color.limit_reached));
        } else {
            monthlyLimitLeftAmount.setText(String.format("+%.2f", monthlyLimit - sumOfMonthlyExpenses));
            monthlyLimitLeftAmount.setTextColor(ContextCompat.getColor(Objects.requireNonNull(this), R.color.sum_greater_than_zero));
        }

        expenseTotalSum.setText(String.format("-%.2f", sumOfMonthlyExpenses));
        expenseMonthlyAverage.setText(String.format("Average: %.2f / day", sumOfMonthlyExpenses / numberOfDaysInMonth));
        drawMonthlyExpensesBarChart(monthlyExpenses, numberOfDaysInMonth);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawMonthlyExpensesBarChart(List<Transaction> monthlyExpenses, int numberOfDaysInMonth) {
        //configure barChart appearance
        monthlyBarChart.getLegend().setEnabled(false);
        monthlyBarChart.getDescription().setEnabled(false);
        monthlyBarChart.getAxisRight().setDrawLabels(false);
        monthlyBarChart.getAxisRight().setDrawAxisLine(false);
        monthlyBarChart.getAxisRight().setDrawGridLines(false);
        LimitLine limitLine = new LimitLine(monthlyLimit, "");
        limitLine.setLineColor(Color.DKGRAY);
        limitLine.setLineWidth(4f);
        limitLine.enableDashedLine(10f, 10f, 0f);
        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        limitLine.setTextSize(10f);
        monthlyBarChart.getAxisLeft().removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        monthlyBarChart.getAxisLeft().addLimitLine(limitLine);
        monthlyBarChart.getAxisLeft().setDrawAxisLine(false);
        monthlyBarChart.getAxisLeft().setGridColor(getResources().getColor(R.color.grid_color)); //color of left Y axis
        monthlyBarChart.getAxisLeft().setTextSize(12);
        monthlyBarChart.getAxisLeft().setXOffset(10);
        monthlyBarChart.getAxisLeft().setValueFormatter(new IntValueFormatter());
        XAxis xAxis = monthlyBarChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setGridColor(getResources().getColor(R.color.grid_color));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(-1);
        xAxis.setTextSize(12);
        if (numberOfDaysInMonth == 31) {
            xAxis.setAxisMaximum(31);
        } else if (numberOfDaysInMonth == 30) {
            xAxis.setAxisMaximum(29.5f); //to last bar look better
        } else xAxis.setAxisMaximum(28); //february leap and non-leap year
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value + 1); //this is not the most elegant way but it works...
            }
        });

        ArrayList<BarEntryHolder> holderOfBarEntries = new ArrayList<>();
        int numberOfDayInMonth = 0;
        float sumOfExpensesInMonth = 0;
        Calendar calendarToGetDayOfMonth = Calendar.getInstance();

        for (int i = 0; i < monthlyExpenses.size(); i++) {
            Date dateOfTransaction = monthlyExpenses.get(i).getDate();
            calendarToGetDayOfMonth.setTime(dateOfTransaction);
            int dayOfMonth = calendarToGetDayOfMonth.get(Calendar.DAY_OF_MONTH);
            int dayOfTransaction = dayOfMonth - 1; //because at getFormattedValue for xAxis +1 is added

            while (numberOfDayInMonth <= dayOfTransaction) {
                holderOfBarEntries.add(new BarEntryHolder(numberOfDayInMonth, sumOfExpensesInMonth));
                numberOfDayInMonth++;
            }

            numberOfDayInMonth = dayOfTransaction;
            float valueOfExpense = Math.abs(monthlyExpenses.get(i).getAmount()); //abs because expense numbers are stored with '-' sign
            sumOfExpensesInMonth = sumOfExpensesInMonth + valueOfExpense;

            //checking if there is already a transaction with the same date, if so, actualize total sum
            int index = IntStream.range(0, holderOfBarEntries.size())
                    .filter(k -> holderOfBarEntries.get(k).getxVal() == dayOfTransaction)
                    .findFirst()
                    .orElse(-1);
            if (index != -1) {
                holderOfBarEntries.set(index, new BarEntryHolder(dayOfTransaction, sumOfExpensesInMonth));
            } else {
                holderOfBarEntries.add(new BarEntryHolder(dayOfTransaction, sumOfExpensesInMonth));
            }
        }
        //fil rest of months day
        for (int j = numberOfDayInMonth; j < numberOfDaysInMonth; j++) {
            holderOfBarEntries.add(new BarEntryHolder(j, sumOfExpensesInMonth));
        }

        monthlyBarChart.getAxisLeft().setAxisMinimum(-(sumOfExpensesInMonth * 0.075f)); //make additional space between bottom of the chart and labels
        if (sumOfExpensesInMonth < monthlyLimit) {
            monthlyBarChart.getAxisLeft().setAxisMaximum(monthlyLimit * 1.075f);
        } else {
            monthlyBarChart.getAxisLeft().setAxisMaximum(sumOfExpensesInMonth * 1.075f);
        }

        //copy data from barEntryHolder to barEntriesList
        ArrayList barEntriesOfCurrentCategoryInMonth = new ArrayList<>();
        for (int m = 0; m < holderOfBarEntries.size(); m++) {
            barEntriesOfCurrentCategoryInMonth.add(new BarEntry(holderOfBarEntries.get(m).getxVal(), holderOfBarEntries.get(m).getyVal()));
        }

        MyBarDataSet myBarDataSet = new MyBarDataSet(barEntriesOfCurrentCategoryInMonth, "");
        myBarDataSet.setColors(
                ContextCompat.getColor(this, R.color.below_limit),
                ContextCompat.getColor(this, R.color.over_limit));
        myBarDataSet.setDrawValues(false);

        BarData barData = new BarData(myBarDataSet);

        monthlyBarChart.setData(barData);
        monthlyBarChart.invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.setDailyLimit: {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (setDailyLimit.getRight() - setDailyLimit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        setDailyLimit.setText("");
                        v.performClick();
                        return false;
                    }
                }
                break;
            }
            case R.id.setMonthlyLimit: {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (setMonthlyLimit.getRight() - setMonthlyLimit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        setMonthlyLimit.setText("");
                        v.performClick();
                        return false;
                    }
                }
            }
            break;
        }
        return false;
    }

    public String convertMonthToString(int month, int year) {
        //months are indexed starting at 0
        String MM = "" + month;
        String yyyy = "" + year;

        if (month < 10) {
            MM = "0" + month;
        }

        return MM + "/" + yyyy;
    }
}