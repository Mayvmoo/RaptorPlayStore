package com.example.raptor.ui.orders

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.raptor.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Schedule Picker Activity
 * Matching iOS SchedulePickerView exactly
 * Allows user to select date and time for scheduled delivery
 */
class SchedulePickerActivity : AppCompatActivity() {

    private lateinit var selectedDateText: TextView
    private lateinit var selectedTimeText: TextView
    private lateinit var datePickerButton: Button
    private lateinit var timePickerButton: Button
    private lateinit var confirmButton: Button

    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedTime: Calendar = Calendar.getInstance()

    private val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("nl", "NL"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("nl", "NL"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_picker)

        // Get initial date/time from intent
        intent.getLongExtra("selected_date", -1).takeIf { it != -1L }?.let {
            selectedDate.timeInMillis = it
        }
        intent.getLongExtra("selected_time", -1).takeIf { it != -1L }?.let {
            selectedTime.timeInMillis = it
        }

        initializeViews()
        setupClickListeners()
        updateUI()
    }

    private fun initializeViews() {
        selectedDateText = findViewById(R.id.selectedDateText)
        selectedTimeText = findViewById(R.id.selectedTimeText)
        datePickerButton = findViewById(R.id.datePickerButton)
        timePickerButton = findViewById(R.id.timePickerButton)
        confirmButton = findViewById(R.id.confirmButton)

        // Close button
        findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        datePickerButton.setOnClickListener {
            showDatePicker()
        }

        timePickerButton.setOnClickListener {
            showTimePicker()
        }

        confirmButton.setOnClickListener {
            // Return selected date and time
            val resultIntent = Intent().apply {
                putExtra("selected_date", selectedDate.timeInMillis)
                putExtra("selected_time", selectedTime.timeInMillis)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            R.style.DatePickerTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate.set(Calendar.YEAR, selectedYear)
                selectedDate.set(Calendar.MONTH, selectedMonth)
                selectedDate.set(Calendar.DAY_OF_MONTH, selectedDay)
                updateUI()
            },
            year,
            month,
            day
        )

        // Set minimum date to today
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000

        datePicker.show()
    }

    private fun showTimePicker() {
        val hour = selectedTime.get(Calendar.HOUR_OF_DAY)
        val minute = selectedTime.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(
            this,
            R.style.TimePickerTheme,
            { _, selectedHour, selectedMinute ->
                selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedTime.set(Calendar.MINUTE, selectedMinute)
                updateUI()
            },
            hour,
            minute,
            true // 24-hour format
        )

        timePicker.show()
    }

    private fun updateUI() {
        selectedDateText.text = dateFormat.format(selectedDate.time)
        selectedTimeText.text = timeFormat.format(selectedTime.time)
    }
}

