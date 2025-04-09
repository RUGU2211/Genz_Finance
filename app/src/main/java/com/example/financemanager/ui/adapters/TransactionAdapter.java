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
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {
    private final OnTransactionClickListener listener;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);

        void onTransactionLongClick(Transaction transaction);
    }

    public TransactionAdapter(OnTransactionClickListener listener) {
        super(new DiffUtil.ItemCallback<Transaction>() {
            @Override
            public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        holder.bind(transaction, listener);
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView amountText;
        private final TextView descriptionText;
        private final TextView categoryText;
        private final TextView dateText;

        TransactionViewHolder(View itemView) {
            super(itemView);
            amountText = itemView.findViewById(R.id.text_amount);
            descriptionText = itemView.findViewById(R.id.text_description);
            categoryText = itemView.findViewById(R.id.text_category);
            dateText = itemView.findViewById(R.id.text_date);
        }

        void bind(Transaction transaction, OnTransactionClickListener listener) {
            amountText.setText(String.format(Locale.getDefault(), "%.2f", transaction.getAmount()));
            descriptionText.setText(transaction.getDescription());
            categoryText.setText(transaction.getCategory());
            dateText.setText(dateFormat.format(transaction.getDate()));

            itemView.setOnClickListener(v -> listener.onTransactionClick(transaction));
        }
    }
}