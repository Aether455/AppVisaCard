package com.example.appvisacard;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appvisacard.model.CardModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    Button logoutBtn, btnScanCard;
    ListView cardListView;
    CardDatabaseHelper cardDbHelper;
    UserDatabaseHelper userDbHelper;
    ArrayAdapter<String> adapter;
    List<CardModel> cardList;
    List<String> displayList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        logoutBtn = findViewById(R.id.logoutBtn);
        btnScanCard = findViewById(R.id.btnScanCard);
        cardListView = findViewById(R.id.cardListView);

        cardDbHelper = new CardDatabaseHelper(this);
        userDbHelper = new UserDatabaseHelper(this);

        int currentUserId = userDbHelper.getCurrentUserId();
        if (currentUserId == -1) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

//        // Nếu bạn chỉ muốn thêm thẻ test 1 lần duy nhất, kiểm tra trước:
//        if (cardDbHelper.getCardsByUserId(currentUserId).isEmpty()) {
//            cardDbHelper.insertCard("1234 5678 9876 5432", "12/30", "Nguyễn Văn A", currentUserId);
//        }

        loadCards(currentUserId);

        cardListView.setOnItemLongClickListener((parent, view, position, id) -> {
            CardModel card = cardList.get(position);

            String[] options = {"Dùng thẻ này", "Xóa thẻ"};
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Tùy chọn cho thẻ")
                    .setIcon(android.R.drawable.ic_menu_manage)
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            showToast("Đã chọn thẻ:\n" + card.getCardNumber());
                        } else if (which == 1) {
                            confirmDeleteCard(card, currentUserId);
                        }
                    })
                    .show();
            return true;
        });

        btnScanCard.setOnClickListener(v -> {
            startActivity(new Intent(this, CardReaderActivity.class));
        });

        logoutBtn.setOnClickListener(v -> logout());
    }

    private void loadCards(int userId) {
        cardList = cardDbHelper.getCardsByUserId(userId);
        displayList = new ArrayList<>();
        for (CardModel c : cardList) {
            displayList.add("Chủ thẻ: " + c.getCardHolder()
                    + "\nSố thẻ: " + c.getCardNumber()
                    + "\nHSD: " + c.getExpireDate());
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        cardListView.setAdapter(adapter);
    }

    private void confirmDeleteCard(CardModel card, int userId) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá thẻ\n" + card.getCardNumber() + " không?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    cardDbHelper.deleteCard(card.getId());
                    showToast("Đã xoá thẻ!");
                    loadCards(userId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logout() {
        userDbHelper.clearCurrentUser();
        showToast("Đăng xuất thành công!");
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
