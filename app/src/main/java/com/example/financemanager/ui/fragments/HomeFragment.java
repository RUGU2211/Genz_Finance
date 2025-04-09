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
import androidx.lifecycle.LiveData;
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
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private TransactionViewModel transactionViewModel;
    private UserViewModel userViewModel;
    private TextView tvUserName, tvTotalBalance, tvIncome, tvExpense;
    private CircleImageView ivProfile;
    private PieChart chartExpenses;
    private RecyclerView rvRecentTransactions;
    private TransactionAdapter transactionAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpense = view.findViewById(R.id.tv_expense);
        ivProfile = view.findViewById(R.id.iv_profile);
        chartExpenses = view.findViewById(R.id.chart_expenses);
        rvRecentTransactions = view.findViewById(R.id.rv_recent_transactions);

        // Set default profile image immediately
        ivProfile.setImageResource(R.drawable.default_profile);
        ivProfile.setVisibility(View.VISIBLE);

        // Setup RecyclerView
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionAdapter = new TransactionAdapter(this);
        rvRecentTransactions.setAdapter(transactionAdapter);

        // Setup chart
        setupPieChart();

        // Setup click listeners
        view.findViewById(R.id.tv_transactions_see_all).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.transactionFragment);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe data changes
        observeUserData();
        setupObservers();
    }

    private void setupObservers() {
        LiveData<Double> totalIncome = transactionViewModel.getTotalIncome();
        if (totalIncome != null) {
            totalIncome.observe(getViewLifecycleOwner(), income -> {
                if (income != null) {
                    tvIncome.setText(CurrencyFormatter.format(income));
                }
            });
        }

        LiveData<Double> totalExpense = transactionViewModel.getTotalExpense();
        if (totalExpense != null) {
            totalExpense.observe(getViewLifecycleOwner(), expense -> {
                if (expense != null) {
                    tvExpense.setText(CurrencyFormatter.format(expense));
                }
            });
        }

        LiveData<List<Transaction>> allTransactions = transactionViewModel.getAllTransactions();
        if (allTransactions != null) {
            allTransactions.observe(getViewLifecycleOwner(), transactions -> {
                if (transactions != null && !transactions.isEmpty()) {
                    updateBalance(transactions);
                    List<Transaction> recentTransactions = transactions.subList(0, Math.min(transactions.size(), 5));
                    transactionAdapter.submitList(recentTransactions);
                }
            });
        }

        LiveData<List<TransactionDao.CategoryTotal>> expensesByCategory = transactionViewModel.getExpensesByCategory();
        if (expensesByCategory != null) {
            expensesByCategory.observe(getViewLifecycleOwner(), this::updatePieChart);
        }
    }

    private void updateBalance(List<Transaction> transactions) {
        double income = 0;
        double expense = 0;
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("income")) {
                income += transaction.getAmount();
            } else {
                expense += transaction.getAmount();
            }
        }
        double balance = income - expense;
        tvTotalBalance.setText(CurrencyFormatter.format(balance));
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
            entries.add(new PieEntry((float) categoryTotal.getTotal(), categoryTotal.getCategory()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
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

                // Set profile image if available
                if (user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
                    try {
                        if (user.getProfileImagePath().startsWith("http")) {
                            // Load image from URL using Glide
                            Glide.with(this)
                                .load(user.getProfileImagePath())
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(ivProfile);
                        } else {
                            Uri imageUri = Uri.parse(user.getProfileImagePath());
                            if (ContentResolver.SCHEME_CONTENT.equals(imageUri.getScheme())) {
                                ivProfile.setImageURI(imageUri);
                            } else {
                                File imgFile = new File(user.getProfileImagePath());
                                if (imgFile.exists()) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                    ivProfile.setImageBitmap(bitmap);
                                } else {
                                    ivProfile.setImageResource(R.drawable.default_profile);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("HomeFragment", "Error loading profile image: " + e.getMessage());
                        ivProfile.setImageResource(R.drawable.default_profile);
                    }
                } else {
                    ivProfile.setImageResource(R.drawable.default_profile);
                }
            } else {
                // Check if user exists before creating default user
                userViewModel.checkUserExists().observe(getViewLifecycleOwner(), exists -> {
                    if (!exists) {
                        // Create default user if none exists
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null) {
                            User defaultUser = new User(
                                firebaseUser.getUid(),
                                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                                firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "",
                                0.0, 0.0, 0.0
                            );
                            if (firebaseUser.getPhotoUrl() != null) {
                                defaultUser.setProfileImagePath(firebaseUser.getPhotoUrl().toString());
                            }
                            userViewModel.insert(defaultUser);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        Bundle bundle = new Bundle();
        bundle.putString("transactionId", transaction.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_homeFragment_to_transactionDetailFragment, bundle);
    }

    @Override
    public void onTransactionLongClick(Transaction transaction) {
        // Show options menu for the transaction
        // This could be implemented to show a popup menu with options like edit, delete, etc.
    }
}