package com.example.financemanager.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financemanager.R;
import com.example.financemanager.data.entities.Transaction;
import com.example.financemanager.utils.CurrencyFormatter;
import com.example.financemanager.utils.DateUtils;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK = new DiffUtil.ItemCallback<Transaction>() {
        @Override
        public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getAmount() == newItem.getAmount() &&
                    oldItem.getCategory().equals(newItem.getCategory()) &&
                    oldItem.getDate().equals(newItem.getDate()) &&
                    oldItem.getType().equals(newItem.getType());
        }
    };
    private OnItemClickListener listener;

    public TransactionAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction currentTransaction = getItem(position);
        holder.bind(currentTransaction);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategory;
        private final TextView tvTitle;
        private final TextView tvDate;
        private final TextView tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        public void bind(Transaction transaction) {
            tvCategory.setText(transaction.getCategory());
            tvTitle.setText(transaction.getTitle());
            tvDate.setText(DateUtils.formatDate(transaction.getDate()));

            String amountText = CurrencyFormatter.format(transaction.getAmount());
            if (transaction.getType().equals("EXPENSE")) {
                tvAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.colorExpense));
                tvAmount.setText("- " + amountText);
            } else {
                tvAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.colorIncome));
                tvAmount.setText("+ " + amountText);
            }
        }
    }
}