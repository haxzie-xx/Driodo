package me.haxzie.driodo.Registration.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import me.haxzie.driodo.Home.Activities.HomeActivity;
import me.haxzie.driodo.Registration.Fragments.MobileNumberRequestFragment;
import me.haxzie.driodo.R;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MobileVerifActivity extends AppCompatActivity {

    private String TAG = "driodo";
    private AppCompatDialog compatDialog;
    private Fragment fragment;
    private Class fragmentClass;
    private TextView dialogMsg;
    private String phoneNumber;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verificaiton without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:" + credential.getProvider());
            SharedPreferences pref = getSharedPreferences("USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("PHONE", phoneNumber);
            editor.commit();
            attemptLogin();
//            signInWithPhoneAuthCredential(credential);
        }


        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e);
            compatDialog.dismiss();
            Toasty.error(MobileVerifActivity.this, "Verification Failed").show();

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
            }

            // Show a message and update the UI
            // ...
        }


        @Override
        public void onCodeSent(String verificationId,
                               PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:" + verificationId);
            dialogMsg.setText("Waiting for incoming Ravens");
            // Save verification ID and resending token so we can use them later
//            mVerificationId = verificationId;
//            mResendToken = token;

            // ...
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(String verificationId) {
            compatDialog.dismiss();
            Toasty.info(MobileVerifActivity.this, "Request Timeout").show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_verif);

        compatDialog = new AppCompatDialog(this);
        compatDialog.setContentView(R.layout.progress_dialog);
        compatDialog.getWindow().setBackgroundDrawable(null);
        compatDialog.setCancelable(false);
        dialogMsg = (TextView) compatDialog.findViewById(R.id.text);
        //Setting the default fragment to view in user home
        fragment = new Fragment();
        fragmentClass = MobileNumberRequestFragment.class;

        FragmentManager fragmentManager = getSupportFragmentManager();
        try {
            fragment = (Fragment) fragmentClass.newInstance();

        } catch (Exception e) {
            e.printStackTrace();
        }

        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();

    }

    /**
     * Sends mobile verification sms through firebase auth.
     *
     * @param phoneNumber
     */
    public void sendVerification(String phoneNumber) {

        this.phoneNumber = phoneNumber;

        if (isNetworkAvailable()) {
            compatDialog.show();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,
                    30,
                    TimeUnit.SECONDS,
                    this,
                    mCallbacks
            );
        } else {
            Toasty.error(this, "No internet connection").show();
        }
    }

    /**
     * checks for network connection
     */
    public boolean isNetworkAvailable() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    /**
     * Try to retrieve user info if available from the server,
     * else sign-up using google account
     */
    public void attemptLogin() {

//        dialogMsg.setText("Setting up account");
        startActivity(new Intent(this, HomeActivity.class));

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
