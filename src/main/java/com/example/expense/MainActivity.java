package com.example.expense;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    // Database
    private SQLiteDatabase db;
    private final String TABLE_NAME = "transactions_v2";

    // Colors (Modern Slate & Fintech Neon Palette)
    private final int BG_COLOR = Color.parseColor("#0F172A");        // Deep Slate Dark
    private final int CARD_BG = Color.parseColor("#1E293B");         // Elevated Card
    private final int CARD_BORDER = Color.parseColor("#334155");     // Card Stroke
    private final int TEXT_PRIMARY = Color.parseColor("#F8FAFC");    // Crisp white
    private final int TEXT_MUTED = Color.parseColor("#94A3B8");      // Muted slate
    private final int ACCENT_GREEN = Color.parseColor("#10B981");    // Neon Emerald (Income)
    private final int ACCENT_RED = Color.parseColor("#F43F5E");      // Rose Crimson (Expense)
    private final int ACCENT_BLUE = Color.parseColor("#38BDF8");     // Sky Glow
    private final int ACCENT_PURPLE = Color.parseColor("#818CF8");   // Indigo Violet

    // UI Elements
    private TextView netBalanceTxt, totalIncomeTxt, totalExpenseTxt, budgetPercentTxt;
    private ProgressBar budgetProgressBar;
    private LinearLayout listContainer;
    private EditText searchInput;
    private String activeCategoryFilter = "All";

    // Categories with modern visual emoji identifiers
    private final String[] CATEGORIES = {
        "🍔 Food", "🛍️ Shopping", "🚗 Travel", "💡 Bills", 
        "💼 Salary", "📈 Investment", "💊 Health", "🍿 Fun"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        initDatabase();
        setContentView(buildModernUI());
        loadTransactions("");
    }

    private void initDatabase() {
        SQLiteOpenHelper helper = new SQLiteOpenHelper(this, "modern_fintech_vault.db", null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "amount REAL NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "category TEXT NOT NULL, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
            }
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {}
        };
        db = helper.getWritableDatabase();
    }

    private View buildModernUI() {
        // Main Root Coordinator
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(BG_COLOR);

        // Scrollable Body
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(20), dp(45), dp(20), dp(100)); // bottom padding for floating FAB

        // 1. App Header
        contentLayout.addView(createHeader());

        // 2. High-Tech Balance Hero Card
        contentLayout.addView(createBalanceHeroCard());

        // 3. Search & Filter Bar
        contentLayout.addView(createSearchAndFilterSection());

        // 4. Recent Activity Title Bar
        TextView recentTitle = new TextView(this);
        recentTitle.setText("Transactions Flow");
        recentTitle.setTextColor(TEXT_PRIMARY);
        recentTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        recentTitle.setTypeface(Typeface.DEFAULT_BOLD);
        recentTitle.setPadding(0, dp(15), 0, dp(10));
        contentLayout.addView(recentTitle);

        // 5. Transaction List Stream
        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        contentLayout.addView(listContainer);

        scrollView.addView(contentLayout);
        rootLayout.addView(scrollView);

        // 6. Floating Action Button (+)
        rootLayout.addView(createFloatingActionButton());

        return rootLayout;
    }

    private View createHeader() {
        RelativeLayout header = new RelativeLayout(this);
        header.setPadding(0, 0, 0, dp(20));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);

        TextView greeting = new TextView(this);
        greeting.setText("PORTFOLIO DASHBOARD");
        greeting.setTextColor(ACCENT_BLUE);
        greeting.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        greeting.setTypeface(Typeface.DEFAULT_BOLD);
        greeting.setLetterSpacing(0.15f);

        TextView appTitle = new TextView(this);
        appTitle.setText("Vault Flow");
        appTitle.setTextColor(TEXT_PRIMARY);
        appTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        appTitle.setTypeface(Typeface.DEFAULT_BOLD);

        titleBox.addView(greeting);
        titleBox.addView(appTitle);

        header.addView(titleBox);
        return header;
    }

    private View createBalanceHeroCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(22), dp(22), dp(22), dp(22));

        // Modern Glassmorphism/Neomorphic Border
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(CARD_BG);
        bg.setCornerRadius(dp(24));
        bg.setStroke(dp(1), CARD_BORDER);
        card.setBackground(bg);

        TextView balSub = new TextView(this);
        balSub.setText("NET LIQUID BALANCE");
        balSub.setTextColor(TEXT_MUTED);
        balSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        balSub.setTypeface(Typeface.DEFAULT_BOLD);
        balSub.setLetterSpacing(0.1f);
        card.addView(balSub);

        netBalanceTxt = new TextView(this);
        netBalanceTxt.setText("₹0.00");
        netBalanceTxt.setTextColor(TEXT_PRIMARY);
        netBalanceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
        netBalanceTxt.setTypeface(Typeface.DEFAULT_BOLD);
        netBalanceTxt.setPadding(0, dp(4), 0, dp(15));
        card.addView(netBalanceTxt);

        // Income & Expense Dual Stat Pill
        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setWeightSum(2);

        // Income Stat
        LinearLayout incBox = new LinearLayout(this);
        incBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        incBox.setLayoutParams(p1);

        TextView incLbl = new TextView(this);
        incLbl.setText("▲ Income");
        incLbl.setTextColor(ACCENT_GREEN);
        incLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        incLbl.setTypeface(Typeface.DEFAULT_BOLD);

        totalIncomeTxt = new TextView(this);
        totalIncomeTxt.setText("₹0.00");
        totalIncomeTxt.setTextColor(TEXT_PRIMARY);
        totalIncomeTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        totalIncomeTxt.setTypeface(Typeface.DEFAULT_BOLD);

        incBox.addView(incLbl);
        incBox.addView(totalIncomeTxt);

        // Expense Stat
        LinearLayout expBox = new LinearLayout(this);
        expBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        expBox.setLayoutParams(p2);

        TextView expLbl = new TextView(this);
        expLbl.setText("▼ Expenses");
        expLbl.setTextColor(ACCENT_RED);
        expLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        expLbl.setTypeface(Typeface.DEFAULT_BOLD);

        totalExpenseTxt = new TextView(this);
        totalExpenseTxt.setText("₹0.00");
        totalExpenseTxt.setTextColor(TEXT_PRIMARY);
        totalExpenseTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        totalExpenseTxt.setTypeface(Typeface.DEFAULT_BOLD);

        expBox.addView(expLbl);
        expBox.addView(totalExpenseTxt);

        statsRow.addView(incBox);
        statsRow.addView(expBox);
        card.addView(statsRow);

        // Mini Spending Ratio Bar
        LinearLayout progressHeader = new LinearLayout(this);
        progressHeader.setOrientation(LinearLayout.HORIZONTAL);
        progressHeader.setPadding(0, dp(16), 0, dp(6));

        TextView burnRate = new TextView(this);
        burnRate.setText("Burn Ratio");
        burnRate.setTextColor(TEXT_MUTED);
        burnRate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        budgetPercentTxt = new TextView(this);
        budgetPercentTxt.setText("0%");
        budgetPercentTxt.setTextColor(TEXT_MUTED);
        budgetPercentTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        budgetPercentTxt.setTypeface(Typeface.DEFAULT_BOLD);

        LinearLayout.LayoutParams lpLeft = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        progressHeader.addView(burnRate, lpLeft);
        progressHeader.addView(budgetPercentTxt);
        card.addView(progressHeader);

        budgetProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        budgetProgressBar.setProgress(0);
        budgetProgressBar.setMax(100);
        card.addView(budgetProgressBar);

        return card;
    }

    private View createSearchAndFilterSection() {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(0, dp(20), 0, dp(10));

        // Search Input
        searchInput = new EditText(this);
        searchInput.setHint("🔍 Search by note or category...");
        searchInput.setHintTextColor(TEXT_MUTED);
        searchInput.setTextColor(TEXT_PRIMARY);
        searchInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        searchInput.setPadding(dp(16), dp(12), dp(16), dp(12));

        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(CARD_BG);
        searchBg.setCornerRadius(dp(14));
        searchBg.setStroke(dp(1), CARD_BORDER);
        searchInput.setBackground(searchBg);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadTransactions(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        section.addView(searchInput);

        // Horizontal Category Filter Pills
        HorizontalScrollView hScroll = new HorizontalScrollView(this);
        hScroll.setHorizontalScrollBarEnabled(false);
        hScroll.setPadding(0, dp(12), 0, 0);

        LinearLayout pillGroup = new LinearLayout(this);
        pillGroup.setOrientation(LinearLayout.HORIZONTAL);

        addFilterPill(pillGroup, "All");
        for (String cat : CATEGORIES) {
            addFilterPill(pillGroup, cat);
        }

        hScroll.addView(pillGroup);
        section.addView(hScroll);

        return section;
    }

    private void addFilterPill(LinearLayout parent, final String category) {
        TextView pill = new TextView(this);
        pill.setText(category);
        pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        pill.setPadding(dp(14), dp(7), dp(14), dp(7));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, dp(8), 0);
        pill.setLayoutParams(params);

        updatePillStyle(pill, category.equals(activeCategoryFilter));

        pill.setOnClickListener(v -> {
            activeCategoryFilter = category;
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    updatePillStyle(tv, tv.getText().toString().equals(activeCategoryFilter));
                }
            }
            loadTransactions(searchInput.getText().toString());
        });

        parent.addView(pill);
    }

    private void updatePillStyle(TextView pill, boolean isSelected) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dp(20));
        if (isSelected) {
            d.setColor(ACCENT_BLUE);
            pill.setTextColor(Color.parseColor("#0A0F1D"));
            pill.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            d.setColor(CARD_BG);
            d.setStroke(dp(1), CARD_BORDER);
            pill.setTextColor(TEXT_MUTED);
            pill.setTypeface(Typeface.DEFAULT);
        }
        pill.setBackground(d);
    }

    private View createFloatingActionButton() {
        FrameLayout.LayoutParams fabParams = new FrameLayout.LayoutParams(dp(62), dp(62));
        fabParams.gravity = Gravity.BOTTOM | Gravity.END;
        fabParams.setMargins(0, 0, dp(24), dp(30));

        Button fab = new Button(this);
        fab.setText("+");
        fab.setTextColor(Color.WHITE);
        fab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        fab.setTypeface(Typeface.DEFAULT_BOLD);
        fab.setLayoutParams(fabParams);
        fab.setGravity(Gravity.CENTER);

        GradientDrawable fabBg = new GradientDrawable();
        fabBg.setShape(GradientDrawable.OVAL);
        fabBg.setColors(new int[]{Color.parseColor("#38BDF8"), Color.parseColor("#6366F1")}); // Modern Gradient
        fabBg.setOrientation(GradientDrawable.Orientation.TL_BR);
        fab.setBackground(fabBg);
        fab.setElevation(dp(8));

        fab.setOnClickListener(v -> showModernAddDialog());
        return fab;
    }

    // Modern Form Dialog for adding income/expenses
    private void showModernAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout dialogView = new LinearLayout(this);
        dialogView.setOrientation(LinearLayout.VERTICAL);
        dialogView.setBackgroundColor(CARD_BG);
        dialogView.setPadding(dp(24), dp(24), dp(24), dp(24));

        TextView title = new TextView(this);
        title.setText("New Transaction");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(TEXT_PRIMARY);
        title.setPadding(0, 0, 0, dp(15));
        dialogView.addView(title);

        // Type Selector (Segmented buttons)
        final String[] transType = {"Expense"};
        LinearLayout toggleBox = new LinearLayout(this);
        toggleBox.setOrientation(LinearLayout.HORIZONTAL);
        toggleBox.setWeightSum(2);

        Button btnExp = new Button(this);
        btnExp.setText("Expense");
        Button btnInc = new Button(this);
        btnInc.setText("Income");

        styleToggleButtons(btnExp, btnInc, true);

        LinearLayout.LayoutParams toggleParam1 = new LinearLayout.LayoutParams(0, dp(42), 1);
        toggleParam1.setMargins(0, 0, dp(4), 0);
        LinearLayout.LayoutParams toggleParam2 = new LinearLayout.LayoutParams(0, dp(42), 1);
        toggleParam2.setMargins(dp(4), 0, 0, 0);

        btnExp.setLayoutParams(toggleParam1);
        btnInc.setLayoutParams(toggleParam2);

        btnExp.setOnClickListener(v -> {
            transType[0] = "Expense";
            styleToggleButtons(btnExp, btnInc, true);
        });
        btnInc.setOnClickListener(v -> {
            transType[0] = "Income";
            styleToggleButtons(btnExp, btnInc, false);
        });

        toggleBox.addView(btnExp);
        toggleBox.addView(btnInc);
        dialogView.addView(toggleBox);

        // Amount input
        EditText amtInput = new EditText(this);
        amtInput.setHint("Amount (₹)");
        amtInput.setHintTextColor(TEXT_MUTED);
        amtInput.setTextColor(TEXT_PRIMARY);
        amtInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        amtInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amtInput.setPadding(dp(14), dp(12), dp(14), dp(12));
        amtInput.setElevation(0);

        GradientDrawable fieldBg = new GradientDrawable();
        fieldBg.setColor(BG_COLOR);
        fieldBg.setCornerRadius(dp(12));
        fieldBg.setStroke(dp(1), CARD_BORDER);
        amtInput.setBackground(fieldBg);

        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fieldParams.setMargins(0, dp(15), 0, dp(10));
        amtInput.setLayoutParams(fieldParams);
        dialogView.addView(amtInput);

        // Note input
        EditText noteInput = new EditText(this);
        noteInput.setHint("Description (e.g. Starbucks, Freelance)");
        noteInput.setHintTextColor(TEXT_MUTED);
        noteInput.setTextColor(TEXT_PRIMARY);
        noteInput.setPadding(dp(14), dp(12), dp(14), dp(12));
        noteInput.setBackground(fieldBg);
        noteInput.setLayoutParams(fieldParams);
        dialogView.addView(noteInput);

        // Category Spinner
        Spinner catSpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CATEGORIES);
        catSpinner.setAdapter(adapter);
        catSpinner.setBackground(fieldBg);
        catSpinner.setPadding(dp(10), dp(10), dp(10), dp(10));
        dialogView.addView(catSpinner);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Save Action Button
        Button saveBtn = new Button(this);
        saveBtn.setText("Add Record");
        saveBtn.setTextColor(Color.WHITE);
        saveBtn.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setCornerRadius(dp(12));
        saveBg.setColor(ACCENT_BLUE);
        saveBtn.setBackground(saveBg);

        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        saveParams.setMargins(0, dp(20), 0, 0);
        saveBtn.setLayoutParams(saveParams);

        saveBtn.setOnClickListener(v -> {
            String amtStr = amtInput.getText().toString().trim();
            if (amtStr.isEmpty()) return;

            double amt = Double.parseDouble(amtStr);
            String note = noteInput.getText().toString().trim();
            if (note.isEmpty()) note = "General Transaction";
            String cat = catSpinner.getSelectedItem().toString();

            ContentValues cv = new ContentValues();
            cv.put("title", note);
            cv.put("amount", amt);
            cv.put("type", transType[0]);
            cv.put("category", cat);
            db.insert(TABLE_NAME, null, cv);

            dialog.dismiss();
            loadTransactions(searchInput.getText().toString());
        });

        dialogView.addView(saveBtn);
        dialog.show();
    }

    private void styleToggleButtons(Button exp, Button inc, boolean isExp) {
        GradientDrawable expBg = new GradientDrawable();
        expBg.setCornerRadius(dp(10));
        GradientDrawable incBg = new GradientDrawable();
        incBg.setCornerRadius(dp(10));

        if (isExp) {
            expBg.setColor(ACCENT_RED);
            exp.setTextColor(Color.WHITE);
            incBg.setColor(BG_COLOR);
            inc.setTextColor(TEXT_MUTED);
        } else {
            incBg.setColor(ACCENT_GREEN);
            inc.setTextColor(Color.WHITE);
            expBg.setColor(BG_COLOR);
            exp.setTextColor(TEXT_MUTED);
        }
        exp.setBackground(expBg);
        inc.setBackground(incBg);
    }

    private void loadTransactions(String query) {
        listContainer.removeAllViews();

        StringBuilder sql = new StringBuilder("SELECT id, title, amount, type, category, timestamp FROM " + TABLE_NAME + " WHERE 1=1 ");
        List<String> argsList = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (title LIKE ? OR category LIKE ?) ");
            argsList.add("%" + query.trim() + "%");
            argsList.add("%" + query.trim() + "%");
        }

        if (!activeCategoryFilter.equals("All")) {
            sql.append("AND category = ? ");
            argsList.add(activeCategoryFilter);
        }

        sql.append("ORDER BY id DESC");

        Cursor cursor = db.rawQuery(sql.toString(), argsList.toArray(new String[0]));

        double totalInc = 0;
        double totalExp = 0;
        int count = 0;

        NumberFormat currFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        while (cursor.moveToNext()) {
            final int id = cursor.getInt(0);
            final String title = cursor.getString(1);
            final double amt = cursor.getDouble(2);
            final String type = cursor.getString(3);
            final String category = cursor.getString(4);
            final String time = cursor.getString(5);

            if (type.equals("Income")) totalInc += amt;
            else totalExp += amt;

            listContainer.addView(createTransactionRow(id, title, amt, type, category, time));
            count++;
        }
        cursor.close();

        // Empty state
        if (count == 0) {
            TextView emptyTxt = new TextView(this);
            emptyTxt.setText("No transactions recorded yet.\nTap '+' below to start.");
            emptyTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            emptyTxt.setTextColor(TEXT_MUTED);
            emptyTxt.setGravity(Gravity.CENTER);
            emptyTxt.setPadding(0, dp(40), 0, 0);
            listContainer.addView(emptyTxt);
        }

        // Calculation Updates
        double balance = totalInc - totalExp;
        netBalanceTxt.setText(currFormat.format(balance).replace("₹", "₹ "));
        totalIncomeTxt.setText(currFormat.format(totalInc).replace("₹", "₹ "));
        totalExpenseTxt.setText(currFormat.format(totalExp).replace("₹", "₹ "));

        int burnPct = (totalInc > 0) ? (int) Math.min(100, (totalExp / totalInc) * 100) : (totalExp > 0 ? 100 : 0);
        budgetProgressBar.setProgress(burnPct);
        budgetPercentTxt.setText(burnPct + "% of Income Spent");
        if (burnPct > 85) budgetPercentTxt.setTextColor(ACCENT_RED);
        else budgetPercentTxt.setTextColor(TEXT_MUTED);
    }

    private View createTransactionRow(final int id, String title, double amount, String type, String category, String time) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));

        GradientDrawable rowBg = new GradientDrawable();
        rowBg.setColor(CARD_BG);
        rowBg.setCornerRadius(dp(16));
        rowBg.setStroke(dp(1), CARD_BORDER);
        row.setBackground(rowBg);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(rowParams);

        // Icon Avatar Bubble
        TextView avatar = new TextView(this);
        String emoji = category.split(" ")[0];
        avatar.setText(emoji);
        avatar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        avatar.setGravity(Gravity.CENTER);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(BG_COLOR);
        avatar.setBackground(avatarBg);

        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        avatarParams.setMargins(0, 0, dp(12), 0);
        avatar.setLayoutParams(avatarParams);
        row.addView(avatar);

        // Details (Title & Meta)
        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        details.setLayoutParams(detailParams);

        TextView titleTv = new TextView(this);
        titleTv.setText(title);
        titleTv.setTextColor(TEXT_PRIMARY);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

        TextView metaTv = new TextView(this);
        metaTv.setText(category + " • " + formatTime(time));
        metaTv.setTextColor(TEXT_MUTED);
        metaTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        details.addView(titleTv);
        details.addView(metaTv);
        row.addView(details);

        // Amount Badge
        TextView amtTv = new TextView(this);
        boolean isInc = type.equals("Income");
        amtTv.setText((isInc ? "+ " : "- ") + "₹" + String.format(Locale.US, "%,.2f", amount));
        amtTv.setTextColor(isInc ? ACCENT_GREEN : ACCENT_RED);
        amtTv.setTypeface(Typeface.DEFAULT_BOLD);
        amtTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        row.addView(amtTv);

        // Long Press to Delete Entry
        row.setOnLongClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete Entry")
                    .setMessage("Remove this transaction record permanently?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
                        loadTransactions(searchInput.getText().toString());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        return row;
    }

    private String formatTime(String raw) {
        if (raw == null) return "Recent";
        try {
            return raw.substring(0, 10);
        } catch (Exception e) {
            return "Recent";
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
