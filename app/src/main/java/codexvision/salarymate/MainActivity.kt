package com.codexvision.salarymate

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("salary_mate", MODE_PRIVATE) }
    private var darkMode = false
    private var history = JSONArray()
    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout
    private lateinit var grossValue: TextView
    private lateinit var netValue: TextView
    private lateinit var historyBox: LinearLayout
    private lateinit var basic: EditText
    private lateinit var allowance: EditText
    private lateinit var overtime: EditText
    private lateinit var deductions: EditText
    private lateinit var advance: EditText
    private lateinit var month: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        darkMode = prefs.getBoolean("dark", false)
        history = try { JSONArray(prefs.getString("history", "[]")) } catch (_: Exception) { JSONArray() }
        buildScreen()
    }

    private fun buildScreen() {
        val bg = if (darkMode) Color.rgb(17, 19, 32) else Color.rgb(246, 247, 251)
        val card = if (darkMode) Color.rgb(29, 32, 49) else Color.WHITE
        val primaryText = if (darkMode) Color.rgb(245, 246, 255) else Color.rgb(29, 32, 53)
        val secondary = if (darkMode) Color.rgb(174, 179, 201) else Color.rgb(111, 117, 139)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = if (darkMode) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this)
        body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(28))
        }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(text("SalaryMate", 27f, primaryText, true))
        brand.addView(text("Your salary, clearly managed", 13f, secondary, false))
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val themeSwitch = Switch(this).apply {
            text = "Dark"
            setTextColor(primaryText)
            isChecked = darkMode
            setOnCheckedChangeListener { _, checked ->
                darkMode = checked
                prefs.edit().putBoolean("dark", checked).apply()
                buildScreen()
            }
        }
        top.addView(themeSwitch)
        body.addView(top)
        spacer(body, 20)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            background = rounded(Color.rgb(92, 83, 245), 24)
        }
        hero.addView(text("TAKE-HOME PAY", 12f, Color.rgb(224, 224, 255), true))
        netValue = text("Rs 0", 34f, Color.WHITE, true)
        hero.addView(netValue)
        spacer(hero, 12)
        val heroBottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val grossCol = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        grossCol.addView(text("Gross earnings", 12f, Color.rgb(224, 224, 255), false))
        grossValue = text("Rs 0", 17f, Color.WHITE, true)
        grossCol.addView(grossValue)
        heroBottom.addView(grossCol, LinearLayout.LayoutParams(0, -2, 1f))
        hero.addView(heroBottom)
        body.addView(hero)
        spacer(body, 22)

        body.addView(text("Salary details", 20f, primaryText, true))
        body.addView(text("Enter your monthly figures in Pakistani rupees.", 13f, secondary, false))
        spacer(body, 12)

        val fieldsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(6), dp(16), dp(8))
            background = rounded(card, 20)
        }
        month = field(fieldsCard, "Salary month", "e.g. October 2026", false)
        basic = field(fieldsCard, "Basic salary (Rs)", "e.g. 50000", true)
        allowance = field(fieldsCard, "Allowances / bonus (Rs)", "e.g. 5000", true)
        overtime = field(fieldsCard, "Overtime (Rs)", "e.g. 1500", true)
        deductions = field(fieldsCard, "Tax / other deductions (Rs)", "e.g. 1000", true)
        advance = field(fieldsCard, "Advance / loan deduction (Rs)", "e.g. 0", true)
        body.addView(fieldsCard)
        spacer(body, 14)

        val calc = button("Calculate salary", Color.rgb(92, 83, 245), Color.WHITE)
        calc.setOnClickListener { calculate() }
        body.addView(calc, matchWrap())
        spacer(body, 10)
        val save = button("Save this month", if (darkMode) Color.rgb(48, 52, 75) else Color.rgb(232, 231, 255), if (darkMode) Color.WHITE else Color.rgb(76, 66, 210))
        save.setOnClickListener { saveRecord() }
        body.addView(save, matchWrap())
        spacer(body, 22)

        val historyHeader = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        historyHeader.addView(text("Salary history", 20f, primaryText, true), LinearLayout.LayoutParams(0, -2, 1f))
        val clear = TextView(this).apply {
            text = "Clear"
            textSize = 13f
            setTextColor(Color.rgb(220, 82, 100))
            setPadding(dp(10), dp(8), dp(4), dp(8))
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Clear salary history?")
                    .setMessage("Saved salary records will be removed from this phone.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Clear") { _, _ ->
                        history = JSONArray()
                        persistHistory()
                        renderHistory(card, primaryText, secondary)
                    }.show()
            }
        }
        historyHeader.addView(clear)
        body.addView(historyHeader)
        spacer(body, 10)
        historyBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(historyBox)
        renderHistory(card, primaryText, secondary)
    }

    private fun calculate(): Double {
        val gross = amount(basic) + amount(allowance) + amount(overtime)
        val net = (gross - amount(deductions) - amount(advance)).coerceAtLeast(0.0)
        grossValue.text = money(gross)
        netValue.text = money(net)
        return net
    }

    private fun saveRecord() {
        if (basic.text.toString().trim().isEmpty()) {
            basic.error = "Enter your basic salary"
            return
        }
        val net = calculate()
        val gross = amount(basic) + amount(allowance) + amount(overtime)
        val record = JSONObject().apply {
            put("month", month.text.toString().trim().ifEmpty { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()) })
            put("basic", amount(basic))
            put("allowance", amount(allowance))
            put("overtime", amount(overtime))
            put("deductions", amount(deductions))
            put("advance", amount(advance))
            put("gross", gross)
            put("net", net)
            put("savedAt", System.currentTimeMillis())
        }
        val updated = JSONArray()
        updated.put(record)
        for (i in 0 until history.length()) {
            val old = history.optJSONObject(i) ?: continue
            if (old.optString("month") != record.optString("month")) updated.put(old)
        }
        history = updated
        persistHistory()
        val card = if (darkMode) Color.rgb(29, 32, 49) else Color.WHITE
        val primaryText = if (darkMode) Color.rgb(245, 246, 255) else Color.rgb(29, 32, 53)
        val secondary = if (darkMode) Color.rgb(174, 179, 201) else Color.rgb(111, 117, 139)
        renderHistory(card, primaryText, secondary)
        AlertDialog.Builder(this).setTitle("Salary saved").setMessage("Your take-home salary is ${money(net)}.").setPositiveButton("OK", null).show()
    }

    private fun renderHistory(card: Int, primary: Int, secondary: Int) {
        if (!::historyBox.isInitialized) return
        historyBox.removeAllViews()
        if (history.length() == 0) {
            val empty = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(18), dp(24), dp(18), dp(24))
                background = rounded(card, 18)
            }
            empty.addView(text("No saved salaries yet", 16f, primary, true))
            empty.addView(text("Calculate and save your first month.", 13f, secondary, false))
            historyBox.addView(empty)
            return
        }
        for (i in 0 until history.length()) {
            val item = history.optJSONObject(i) ?: continue
            val recordCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
                background = rounded(card, 18)
            }
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val left = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            left.addView(text(item.optString("month"), 16f, primary, true))
            left.addView(text("Gross ${money(item.optDouble("gross"))}", 12f, secondary, false))
            row.addView(left, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(text(money(item.optDouble("net")), 17f, Color.rgb(49, 174, 127), true))
            recordCard.addView(row)
            spacer(recordCard, 8)
            recordCard.addView(text("Basic ${money(item.optDouble("basic"))}  •  Allowances ${money(item.optDouble("allowance"))}", 12f, secondary, false))
            recordCard.addView(text("Overtime ${money(item.optDouble("overtime"))}  •  Deductions ${money(item.optDouble("deductions") + item.optDouble("advance"))}", 12f, secondary, false))
            val remove = TextView(this).apply {
                text = "Delete record"
                textSize = 12f
                setTextColor(Color.rgb(220, 82, 100))
                setPadding(0, dp(8), 0, 0)
                setOnClickListener {
                    val newArray = JSONArray()
                    for (j in 0 until history.length()) if (j != i) newArray.put(history.getJSONObject(j))
                    history = newArray
                    persistHistory()
                    renderHistory(card, primary, secondary)
                }
            }
            recordCard.addView(remove)
            historyBox.addView(recordCard, matchWrap())
            spacer(historyBox, 10)
        }
    }

    private fun field(parent: LinearLayout, label: String, hint: String, numeric: Boolean): EditText {
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(8), 0, dp(8)) }
        column.addView(text(label, 13f, if (darkMode) Color.rgb(202, 207, 226) else Color.rgb(83, 89, 111), true))
        val input = EditText(this).apply {
            textSize = 15f
            setSingleLine(true)
            this.hint = hint
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(if (darkMode) Color.rgb(39, 43, 63) else Color.rgb(246, 247, 251), 12)
            setTextColor(if (darkMode) Color.WHITE else Color.rgb(29, 32, 53))
            setHintTextColor(if (darkMode) Color.rgb(130, 137, 162) else Color.rgb(148, 153, 171))
            if (numeric) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        column.addView(input, matchWrap())
        parent.addView(column, matchWrap())
        return input
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun button(label: String, bg: Int, fg: Int) = Button(this).apply {
        text = label
        textSize = 15f
        isAllCaps = false
        setTextColor(fg)
        background = rounded(bg, 15)
        minHeight = dp(52)
        stateListAnimator = null
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun amount(edit: EditText): Double = edit.text.toString().trim().toDoubleOrNull() ?: 0.0
    private fun money(value: Double): String = "Rs " + NumberFormat.getNumberInstance(Locale.US).format(kotlin.math.round(value).toLong())
    private fun spacer(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }
    private fun matchWrap() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun persistHistory() { prefs.edit().putString("history", history.toString()).apply() }
}
