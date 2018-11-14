package com.peerbits.nfccardread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.peerbits.creditCardNfcReader.CardNfcAsyncTask;
import com.peerbits.creditCardNfcReader.utils.CardNfcUtils;

import pl.droidsonroids.gif.GifImageView;


public class NFCCardReading extends Activity implements CardNfcAsyncTask.CardNfcInterface {

    private CardNfcAsyncTask mCardNfcAsyncTask;
    /*private LinearLayout mCardReadyContent;
    private TextView mPutCardContent;
    private TextView mCardNumberText;
    private TextView mExpireDateText;
    private ImageView mCardLogoIcon;*/ //Shahil
    private NfcAdapter mNfcAdapter;
    private AlertDialog mTurnNfcDialog;
    private ProgressDialog mProgressDialog;
    private String mDoNotMoveCardMessage;
    private String mUnknownEmvCardMessage;
    private String mCardWithLockedNfcMessage;
    private String card;
    private String cardType;
    private String expiredDate;
    private boolean mIsScanNow;
    private boolean mIntentFromCreate;
    private CardNfcUtils mCardNfcUtils;
    private ImageView ivBack, imgRead;
    private LinearLayout llText;
    private GifImageView imgRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_cardread);


        imgRight = findViewById(R.id.imgRight);
        ivBack = findViewById(R.id.ivBack);
        imgRead = findViewById(R.id.imgRead);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        llText = findViewById(R.id.llText);

        if (mNfcAdapter == null) {
           /* TextView noNfc = findViewById(android.R.id.candidatesArea);
            noNfc.setVisibility(View.VISIBLE);*/ // Shahil
        } else {
            mCardNfcUtils = new CardNfcUtils(this);
            /* mCardReadyContent = findViewById(R.id.content_cardReady);

           mPutCardContent = findViewById(R.id.content_putCard);
            mCardNumberText = findViewById(android.R.id.text1);
            mExpireDateText =  findViewById(android.R.id.text2);
            mCardLogoIcon = findViewById(android.R.id.icon);*/  // Shahil

            createProgressDialog();
            initNfcMessages();
            mIntentFromCreate = true;
            onNewIntent(getIntent());

            ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIntentFromCreate = false;
        if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
            showTurnOnNfcDialog();
//            mPutCardContent.setVisibility(View.GONE);  Shahil

        } else if (mNfcAdapter != null) {
            if (!mIsScanNow) {
                //  mPutCardContent.setVisibility(View.VISIBLE);
                //mCardReadyContent.setVisibility(View.GONE);
            }
            mCardNfcUtils.enableDispatch();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mCardNfcUtils.disableDispatch();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mCardNfcAsyncTask = new CardNfcAsyncTask.Builder(this, intent, mIntentFromCreate)
                    .build();
        }
    }

    @Override
    public void startNfcReadCard() {
        mIsScanNow = true;
        mProgressDialog.show();
    }

    @Override
    public void cardIsReadyToRead() {

        // mCardReadyContent.setVisibility(View.VISIBLE);
        card = mCardNfcAsyncTask.getCardNumber();
        card = getPrettyCardNumber(card);
        expiredDate = mCardNfcAsyncTask.getCardExpireDate();
        cardType = mCardNfcAsyncTask.getCardType();
        String cardHolderFirstName = mCardNfcAsyncTask.getCardFirstName();
        String cardHolderLastName = mCardNfcAsyncTask.getCardFirstName();
        int CardCvv = mCardNfcAsyncTask.getCardCvv();

        imgRight.setVisibility(View.VISIBLE);
        llText.setVisibility(View.GONE);


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                Intent i = new Intent(NFCCardReading.this, NFCPayActivity.class);
                i.putExtra("card", card);
                i.putExtra("cardType", cardType);
                i.putExtra("expiredDate", expiredDate);
                startActivity(i);
                finish();
            }
        }, 1200);





     /*   mPutCardContent.setVisibility(View.GONE);
        mCardNumberText.setText(card + cardType);
        mExpireDateText.setText(expiredDate);*/ // Shahil

        Log.e("Details", "-->" + card + " -->" + cardType + "-->" + expiredDate + "--> " + cardHolderFirstName + "--> " + cardHolderLastName + " --> " + CardCvv);
        parseCardType(cardType);
    }

    @Override
    public void doNotMoveCardSoFast() {
        showSnackBar(mDoNotMoveCardMessage);
    }

    @Override
    public void unknownEmvCard() {
        showSnackBar(mUnknownEmvCardMessage);
    }

    @Override
    public void cardWithLockedNfc() {
        showSnackBar(mCardWithLockedNfcMessage);
    }

    @Override
    public void finishNfcReadCard() {
        mProgressDialog.dismiss();
        mCardNfcAsyncTask = null;
        mIsScanNow = false;
    }

    private void createProgressDialog() {
        String title = getString(R.string.ad_progressBar_title);
        String mess = getString(R.string.ad_progressBar_mess);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(mess);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }

    private void showSnackBar(String message) {
        Toast.makeText(NFCCardReading.this, message, Toast.LENGTH_LONG).show();
    }

    private void showTurnOnNfcDialog() {
        if (mTurnNfcDialog == null) {
            String title = getString(R.string.ad_nfcTurnOn_title);
            String mess = getString(R.string.ad_nfcTurnOn_message);
            String pos = getString(R.string.ad_nfcTurnOn_pos);
            String neg = getString(R.string.ad_nfcTurnOn_neg);
            mTurnNfcDialog = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(mess)
                    .setPositiveButton(pos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Send the user to the settings page and hope they turn it on
                            if (android.os.Build.VERSION.SDK_INT >= 16) {
                                startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                            } else {
                                startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        }
                    })
                    .setNegativeButton(neg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            onBackPressed();
                        }
                    }).create();
        }
        mTurnNfcDialog.show();
    }

    private void initNfcMessages() {
        mDoNotMoveCardMessage = getString(R.string.snack_doNotMoveCard);
        mCardWithLockedNfcMessage = getString(R.string.snack_lockedNfcCard);
        mUnknownEmvCardMessage = getString(R.string.snack_unknownEmv);
    }

    private void parseCardType(String cardType) {
        if (cardType.equals(CardNfcAsyncTask.CARD_UNKNOWN)) {
            Toast.makeText(NFCCardReading.this, getString(R.string.snack_unknown_bank_card), Toast.LENGTH_LONG).show();
        } else if (cardType.equals(CardNfcAsyncTask.CARD_VISA)) {
            //   mCardLogoIcon.setImageResource(R.mipmap.visa_logo); Shahil
        } else if (cardType.equals(CardNfcAsyncTask.CARD_MASTER_CARD)) {
            //   mCardLogoIcon.setImageResource(R.mipmap.master_logo); Shahil
        }
    }

    private String getPrettyCardNumber(String card) {
        String div = " - ";
        return card.substring(0, 4) + div + card.substring(4, 8) + div + card.substring(8, 12)
                + div + card.substring(12, 16);
    }
}
