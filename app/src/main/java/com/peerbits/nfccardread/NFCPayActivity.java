package com.peerbits.nfccardread;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.peerbits.creditCardNfcReader.CardNfcAsyncTask;

public class NFCPayActivity extends Activity implements View.OnClickListener {

    public static final String TAG = NFCPayActivity.class.getSimpleName();

    private TextView tvCardNumber;
    private TextView tvEXPDate;
    private ImageView mCardLogoIcon, ivBack;
    private String card = "", cardType = "", expiredDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_pay);
        initViews();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            card = extras.getString("card");
            cardType = extras.getString("cardType");
            expiredDate = extras.getString("expiredDate");

            tvCardNumber.setText("" + card);
            tvEXPDate.setText("" + expiredDate);

            parseCardType(cardType);
        }
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void parseCardType(String cardType) {

        Log.e("CardTyoe", " " + cardType);
        if (cardType.equals(CardNfcAsyncTask.CARD_UNKNOWN)) {
            Toast.makeText(NFCPayActivity.this, getString(R.string.snack_unknown_bank_card), Toast.LENGTH_LONG).show();
        } else if (cardType.equals(CardNfcAsyncTask.CARD_VISA)) {
            mCardLogoIcon.setImageResource(R.mipmap.visa_logo);
        } else if (cardType.equals(CardNfcAsyncTask.CARD_NAB_VISA)) {
            mCardLogoIcon.setImageResource(R.mipmap.visa_logo);
        }
    }

    private void initViews() {


        tvCardNumber = findViewById(R.id.tvCardNumber);
        tvEXPDate = findViewById(R.id.tvEXPDate);
        ivBack = findViewById(R.id.ivBack);
        mCardLogoIcon = findViewById(R.id.ivCardIcon);
    }


    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.rlReadNFCTAG:
                intent = new Intent(this, NFCRead.class);
                this.startActivity(intent);
                break;

            case R.id.rlWriteWithNFC:
                intent = new Intent(this, NFCWrite.class);
                this.startActivity(intent);
                break;

            case R.id.rlCreditCard:
                intent = new Intent(this, NFCCardReading.class);
                this.startActivity(intent);
                break;
        }
    }


}
