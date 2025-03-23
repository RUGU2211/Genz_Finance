package com.example.financemanager.ui.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financemanager.R;
import com.example.financemanager.data.entities.Budget;
import com.example.financemanager.utils.CurrencyFormatter;

public class BudgetAdapter extends ListAdapter<Budget, BudgetAdapter.BudgetViewHolder> {

    private static final DiffUtil.ItemCallback<Budget> DIFF_CALLBACK = new DiffUtil.ItemCallback<Budget>() {
        @Override
        public boolean areItemsTheSame(@NonNull Budget oldItem, @NonNull Budget newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Budget oldItem, @NonNull Budget newItem) {
            return oldItem.getCategory().equals(newItem.getCategory()) &&
                    oldItem.getBudgetAmount() == newItem.getBudgetAmount() &&
                    oldItem.getSpentAmount() == newItem.getSpentAmount() &&
                    oldItem.getPeriod().equals(newItem.getPeriod());
        }
    };
    private OnEditClickListener editListener;
    private OnItemClickListener itemClickListener;

    public BudgetAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget currentBudget = getItem(position);
        holder.bind(currentBudget);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnEditClickListener {
        void onEditClick(Budget budget);
    }

    public interface OnItemClickListener {
        void onItemClick(Budget budget);
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategory;
        private final TextView tvBudgetAmount;
        private final TextView tvSpentAmount;
        private final TextView tvPercentage;
        private final TextView tvEdit;
        private final ProgressBar progressBar;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvBudgetAmount = itemView.findViewById(R.id.tv_budget_amount);
            tvSpentAmount = itemView.findViewById(R.id.tv_spent_amount);
            tvPercentage = itemView.findViewById(R.id.tv_percentage);
            progressBar = itemView.findViewById(R.id.progress_bar);
            tvEdit = itemView.findViewById(R.id.tv_edit);

            // Set click listeners
            tvEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (editListener != null && position != RecyclerView.NO_POSITION) {
                    editListener.onEditClick(getItem(position));
                }
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (itemClickListener != null && position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(getItem(position));
                }
            });
        }

        public void bind(Budget budget) {
            tvCategory.setText(budget.getCategory());
            tvBudgetAmount.setText("Budget: " + CurrencyFormatter.format(budget.getBudgetAmount()));
            tvSpentAmount.setText("Spent: " + CurrencyFormatter.format(budget.getSpentAmount()));

            double percentageUsed = budget.getPercentageSpent();
            int progress = (int) percentageUsed;

            progressBar.setProgress(progress);
            tvPercentage.setText(String.format("%.1f%% used", percentageUsed));

            // Change progress bar color based on usage
            if (percentageUsed > 90) {
                progressBar.setProgressTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), R.color.colorExpense)));
            } else if (percentageUsed > 75) {
                progressBar.setProgressTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark)));
            } else {
                progressBar.setProgressTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary)));
            }

            // Visual indicators for budget status
            if (percentageUsed >= 100) {
                tvSpentAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorExpense));
            } else {
                tvSpentAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorTextSecondary));
            }
        }
    }
}