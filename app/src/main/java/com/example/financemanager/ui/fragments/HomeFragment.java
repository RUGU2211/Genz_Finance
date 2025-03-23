package com.example.financemanager.ui.fragments;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financemanager.R;
import com.example.financemanager.data.dao.TransactionDao;
import com.example.financemanager.data.entities.Transaction;
import com.example.financemanager.data.entities.User;
import com.example.financemanager.ui.adapters.TransactionAdapter;
import com.example.financemanager.ui.viewmodels.TransactionViewModel;
import com.example.financemanager.ui.viewmodels.UserViewModel;
import com.example.financemanager.utils.CurrencyFormatter;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    private TransactionViewModel transactionViewModel;
    private UserViewModel userViewModel;
    private TextView tvUserName, tvTotalBalance, tvIncome, tvExpense;
    private CircleImageView ivProfile;
    private PieChart chartExpenses;
    private RecyclerView rvRecentTransactions;
    private TransactionAdapter transactionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpense = view.findViewById(R.id.tv_expense);

        // Initialize profile image view
        ivProfile = view.findViewById(R.id.iv_profile);

        // Set default profile image immediately
        ivProfile.setImageResource(R.drawable.default_profile);

        // Make sure image view is visible
        ivProfile.setVisibility(View.VISIBLE);

        // Initialize views
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpense = view.findViewById(R.id.tv_expense);
        ivProfile = view.findViewById(R.id.iv_profile);
        chartExpenses = view.findViewById(R.id.chart_expenses);
        rvRecentTransactions = view.findViewById(R.id.rv_recent_transactions);

        // Setup RecyclerView
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionAdapter = new TransactionAdapter();
        rvRecentTransactions.setAdapter(transactionAdapter);

        // Setup ViewModels
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Setup chart
        setupPieChart();

        // Observe data changes
        observeUserData();
        observeTransactions();
        observeExpensesByCategory();

        // Setup click listeners
        view.findViewById(R.id.tv_transactions_see_all).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_transactions);
        });

        transactionAdapter.setOnItemClickListener(transaction -> {
            Bundle bundle = new Bundle();
            bundle.putInt("transactionId", transaction.getId());
            Navigation.findNavController(view).navigate(R.id.action_to_transactionDetailFragment, bundle);
        });

        return view;
    }

    private void observeTransactions() {
        transactionViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                // Display only the most recent transactions (max 5)
                int limit = Math.min(transactions.size(), 5);
                List<Transaction> recentTransactions = transactions.subList(0, limit);
                transactionAdapter.submitList(recentTransactions);
            } else {
                transactionAdapter.submitList(new ArrayList<>());
            }
        });
    }

    private void observeExpensesByCategory() {
        transactionViewModel.getExpensesByCategory().observe(getViewLifecycleOwner(), categoryTotals -> {
            if (categoryTotals != null && !categoryTotals.isEmpty()) {
                updatePieChart(categoryTotals);
            } else {
                chartExpenses.setData(null);
                chartExpenses.invalidate();
            }
        });
    }

    private void setupPieChart() {
        chartExpenses.getDescription().setEnabled(false);
        chartExpenses.setUsePercentValues(true);
        chartExpenses.setDrawHoleEnabled(true);
        chartExpenses.setHoleColor(Color.WHITE);
        chartExpenses.setTransparentCircleRadius(61f);
        chartExpenses.setHoleRadius(58f);
        chartExpenses.setDrawCenterText(true);
        chartExpenses.setCenterText("Expenses");
        chartExpenses.setRotationAngle(0);
        chartExpenses.setRotationEnabled(true);
        chartExpenses.setHighlightPerTapEnabled(true);
        chartExpenses.getLegend().setEnabled(true);
        chartExpenses.setEntryLabelTextSize(12f);
    }

    private void updatePieChart(List<TransactionDao.CategoryTotal> categoryTotals) {
        List<PieEntry> entries = new ArrayList<>();

        for (TransactionDao.CategoryTotal categoryTotal : categoryTotals) {
            entries.add(new PieEntry((float) categoryTotal.total, categoryTotal.category));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(chartExpenses));

        chartExpenses.setData(data);
        chartExpenses.invalidate();
    }

    private void observeUserData() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvUserName.setText(user.getName());
                tvTotalBalance.setText(CurrencyFormatter.format(user.getTotalBalance()));
                tvIncome.setText(CurrencyFormatter.format(user.getTotalIncome()));
                tvExpense.setText(CurrencyFormatter.format(user.getTotalExpense()));

                // Set profile image if available
                if (user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
                    try {
                        Uri imageUri = Uri.parse(user.getProfileImagePath());
                        Log.d("HomeFragment", "Loading image from: " + imageUri);

                        try {
                            if (ContentResolver.SCHEME_CONTENT.equals(imageUri.getScheme())) {
                                // Content URI handling
                                ivProfile.setImageURI(imageUri);
                            } else {
                                // File path handling
                                File imgFile = new File(user.getProfileImagePath());
                                if (imgFile.exists()) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                    ivProfile.setImageBitmap(bitmap);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("HomeFragment", "Error loading profile image: " + e.getMessage());
                            ivProfile.setImageResource(R.drawable.default_profile);
                        }
                    } catch (Exception e) {
                        Log.e("HomeFragment", "Error parsing image URI: " + e.getMessage());
                        ivProfile.setImageResource(R.drawable.default_profile);
                    }
                } else {
                    ivProfile.setImageResource(R.drawable.default_profile);
                }
            } else {
                // Create default user if none exists
                User defaultUser = new User("User", "user@example.com", "", 0, 0, 0);
                userViewModel.insert(defaultUser);
            }
        });
    }
}