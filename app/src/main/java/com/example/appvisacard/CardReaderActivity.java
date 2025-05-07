package com.example.appvisacard;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.IProvider;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CardReaderActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    TextView statusText;
    PendingIntent pendingIntent;
    IntentFilter[] intentFiltersArray;
    String[][] techListsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_reader);

        statusText = findViewById(R.id.statusText);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ NFC", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Chuẩn bị PendingIntent để nhận NFC Intent
        pendingIntent = PendingIntent.getActivity(
                this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );


        // Bắt cả TECH_DISCOVERED và TAG_DISCOVERED
        IntentFilter techDiscovered = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
//        IntentFilter tagDiscovered = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
//        IntentFilter ndefDiscovered = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        intentFiltersArray = new IntentFilter[]{techDiscovered
//                tagDiscovered, ndefDiscovered
        };

        techListsArray = new String[][]{
                new String[]{android.nfc.tech.IsoDep.class.getName()},
//                new String[]{android.nfc.tech.NfcA.class.getName()},
//                new String[]{android.nfc.tech.NfcB.class.getName()},
//                new String[]{android.nfc.tech.NfcF.class.getName()},
//                new String[]{android.nfc.tech.NfcV.class.getName()}
        };


        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("NFC_LIFECYCLE", "App ready, gọi enableForegroundDispatch...");
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            nfcAdapter.enableForegroundDispatch(
                    this,
                    pendingIntent,
                    intentFiltersArray,
                    techListsArray
            );
            Toast.makeText(this, "Đã sẵn sàng quét NFC", Toast.LENGTH_SHORT).show();
            Log.d("NFC_LIFECYCLE", "Foreground dispatch enabled");
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        String action = intent.getAction();
        if (intent == null) {
            Log.e("NFC_DEBUG", "Intent bị null");
        } else {
            Log.d("NFC_DEBUG", "Intent action: " + intent.getAction());
            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    Log.d("NFC_EXTRA", key + ": " + value);
                }
            } else {
                Log.d("NFC_DEBUG", "Extras null luôn");
            }
        }

        Log.d("DEBUG_INTENT", intent.toString());
        //Toast.makeText(this, "onNewIntent: " + intent, Toast.LENGTH_SHORT).show();
        Log.d("NFC_ACTION", "Nhận được intent action: " + action);
        //Toast.makeText(this, "Nhận được intent action: " + action, Toast.LENGTH_SHORT).show();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
//                NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
//                NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
        ) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            System.out.println("tag: " + tag);
            if (tag != null) {
                String[] techList = tag.getTechList();
                Log.d("NFC_TECHS", "Techs supported:");
                for (String tech : techList) {
                    Log.d("NFC_TECHS", tech);
                }

                IsoDep isoDep = IsoDep.get(tag);
                if (isoDep != null) {
                    readCardWithExecutor(isoDep);
                } else {
                    String techs = Arrays.toString(tag.getTechList());
                    Log.e("NFC_UNSUPPORTED", "Thẻ không hỗ trợ IsoDep: " + techs);
                    Toast.makeText(this, "Không hỗ trợ loại thẻ này", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Không phát hiện được thẻ", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Intent không phải hành động NFC hợp lệ: " + action, Toast.LENGTH_SHORT).show();
            Log.d("NFC_ACTION", "Intent không phải hành động NFC hợp lệ: " + action);
        }
    }

    private void readCardWithExecutor(IsoDep isoDep) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                isoDep.connect();

                IProvider provider = new ProviderImpl(isoDep);

                EmvTemplate.Config config = EmvTemplate.Config()
                        .setContactLess(true)
                        .setReadAllAids(true)
                        .setReadTransactions(true);

                EmvTemplate parser = EmvTemplate.Builder()
                        .setProvider(provider)
                        .setConfig(config)
                        .build();

                EmvCard card = parser.readEmvCard();

                runOnUiThread(() -> {
                    if (card != null && card.getCardNumber() != null) {
                        Log.d("CARD_INFO", card.toString());
                        String cardNumber = card.getCardNumber();
                        Date expireDate = card.getExpireDate();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        String formattedDate = dateFormat.format(expireDate);
                        String holderName = (card.getHolderFirstname() != null ? card.getHolderFirstname() : "") + " " +
                                (card.getHolderLastname() != null ? card.getHolderLastname() : "");


                        // KHÚC NÀY BỔ SUNG QUAN TRỌNG
                        UserDatabaseHelper userDbHelper = new UserDatabaseHelper(CardReaderActivity.this);
                        int currentUserId = userDbHelper.getCurrentUserId();

                        if (currentUserId == -1) {
                            Toast.makeText(CardReaderActivity.this, "Không xác định được người dùng!", Toast.LENGTH_SHORT).show();
                            statusText.setText("Không xác định được user đăng nhập.");
                            return;
                        }

                        CardDatabaseHelper cardDbHelper = new CardDatabaseHelper(CardReaderActivity.this);

                        if (!cardDbHelper.isCardExists(cardNumber)) {
                            boolean inserted = cardDbHelper.insertCard(cardNumber, formattedDate,holderName,currentUserId);
                            Log.d("CARD_INSERT", "Kết quả lưu thẻ: " + inserted);
                            if (inserted) {
                                Toast.makeText(CardReaderActivity.this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CardReaderActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                statusText.setText("Lỗi khi lưu thẻ.");
                            }
                        } else {
                            Toast.makeText(CardReaderActivity.this, "Thẻ đã tồn tại", Toast.LENGTH_SHORT).show();
                            statusText.setText("Thẻ đã tồn tại: " + cardNumber);
                        }
                    } else {
                        statusText.setText("Không thể đọc được thẻ hoặc không có dữ liệu.");
                        Toast.makeText(CardReaderActivity.this, "Thẻ không hợp lệ hoặc không đọc được", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Log.e("EMV_ERROR", e.toString());
                runOnUiThread(() -> {
                    statusText.setText("Lỗi khi đọc thẻ: " + e.getMessage());
                    Toast.makeText(CardReaderActivity.this, "Đọc thẻ thất bại!", Toast.LENGTH_LONG).show();
                });
            }
        });
    }


}
