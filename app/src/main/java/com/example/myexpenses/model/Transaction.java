package com.example.myexpenses.model;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.myexpenses.R;
import com.example.myexpenses.customAdapter.ItemAdapter;

import java.util.Date;

public class Transaction implements Comparable<Transaction>, Item {

    private int id;
    private int type;
    private String name;
    private double amount;
    private String category;
    private Date date;

    public Transaction() {
    }

    public Transaction(int type, String name, float amount, String category, Date date) {
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int compareTo(Transaction transaction) {
        return this.date.compareTo(transaction.date);
    }

    @Override
    public int getViewType() {
        return ItemAdapter.RowType.LIST_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View view) {
        ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.transaction_row, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        double transactionAmountAsDouble = this.amount;
        if (transactionAmountAsDouble >= 0) {
            viewHolder.transactionAmount.setText(String.format("+%.2f", transactionAmountAsDouble));
            viewHolder.transactionAmount.setTextColor(ContextCompat.getColor(view.getContext(), R.color.ColorPrimary));
        } else {
            viewHolder.transactionAmount.setText(String.format("%.2f", transactionAmountAsDouble));
            viewHolder.transactionAmount.setTextColor(ContextCompat.getColor(view.getContext(), R.color.bacgroundColorPopup));
        }

        int lengthOfTransactionName = this.name.length();
        if (lengthOfTransactionName >= 24) {
            int lengthOfTransactionAmount = Double.toString(transactionAmountAsDouble).length();
            if (transactionAmountAsDouble > 0) {
                lengthOfTransactionAmount = lengthOfTransactionAmount + 2; //situation when '+' is added
            }
            int additionalSpaceForText;
            if (lengthOfTransactionAmount <= 4) {
                additionalSpaceForText = lengthOfTransactionAmount;
            } else {
                additionalSpaceForText = 7 - lengthOfTransactionAmount;
            }
            viewHolder.transactionName.setText(this.name.substring(0, 20 + additionalSpaceForText) + "...");
        } else if (lengthOfTransactionName == 0) { //if name of transaction is empty we set it as category name
            viewHolder.transactionName.setText(this.category);
        } else {
            viewHolder.transactionName.setText(this.name);
        }

        String categoryName = this.category.toLowerCase().replace(" ", "_"); //prepare R.drawable.name: toLowerCase() because Android restrict Drawable filenames to not use Capital letters in their names, and also simple replace
        int res = view.getContext().getResources().getIdentifier(categoryName, "drawable", view.getContext().getPackageName());
        viewHolder.transactionImage.setImageResource(res);

        return view;
    }

    private class ViewHolder {
        TextView transactionAmount;
        TextView transactionName;
        ImageView transactionImage;

        ViewHolder(View convertView) {
            transactionAmount = (TextView) convertView.findViewById(R.id.amountTransaction);
            transactionName = (TextView) convertView.findViewById(R.id.nameTransaction);
            transactionImage = (ImageView) convertView.findViewById(R.id.imageTransaction);
        }
    }
}