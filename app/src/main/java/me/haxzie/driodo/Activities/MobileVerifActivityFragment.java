package me.haxzie.driodo.Activities;


import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import me.haxzie.driodo.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MobileVerifActivityFragment extends Fragment {

    LinearLayout btnVerify;
    EditText etMobile;
    Spinner spCountry;
    List<String> codes;
    Context context;
    JSONArray jsonCodes;
    String selectedCode = "+91";
    private FirebaseAuth mAuth;
    String TAG = "driodo";


    public MobileVerifActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mobile_verif, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        context = getContext();
        btnVerify = view.findViewById(R.id.btn_verify);
        etMobile = view.findViewById(R.id.et_mobile_number);
        spCountry = view.findViewById(R.id.spnr_country_chooser);

        //Retrieving country codes from assets
        try {
            jsonCodes = new JSONArray(readJSONFromAsset());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context,"Something went wrong !",Toast.LENGTH_SHORT).show();
        }


        //if no error in reading json file, parse it to the string array
        if (jsonCodes != null){
            try {
                parseJSONCodes();
                ArrayAdapter<String> ccAdapter = new ArrayAdapter<String>(context,R.layout.support_simple_spinner_dropdown_item,codes);
                ccAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spCountry.setAdapter(ccAdapter);
                setupListners();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobileNumber = etMobile.getText().toString();
                if (mobileNumber.length() == 10 && mobileNumber.matches("[0-9]+")){
                   showConfirmationDialogue(selectedCode + mobileNumber);
                }else{
                    etMobile.setError("Invalid Number");
                }
            }
        });
    }

    /**
     * Show dialog to confirm mobile number before sending message
     * @param mobileNumber
     */
    private void showConfirmationDialogue(final String mobileNumber){

        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Confirm Mobile Number");
        dialog.setMessage("Is your number "+mobileNumber+" correct?");
        dialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((MobileVerifActivity)getContext()).sendVerification(mobileNumber);
            }
        }).setNegativeButton("Edit", null).show();
    }

    /**
     * Reads and returns the json data of dial codes saved in assets
     */
    public String readJSONFromAsset() {
        String json;
        try {
            InputStream is = getContext().getAssets().open("countrycodes.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * Parses the JSON data to arrayList
     */
    private void parseJSONCodes() throws JSONException {

        codes = new ArrayList<>();

        for (int i = 0; i < jsonCodes.length(); i++){
            JSONObject obj = jsonCodes.getJSONObject(i);
            codes.add(obj.getString("code") + " "+ obj.getString("dial_code")+" "+obj.getString("name"));
        }

    }

    /**
     * to setup the listeners for dial code selection spinner
     */
    private void setupListners(){
        spCountry.setSelection(87);
        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    selectedCode = jsonCodes.getJSONObject(i).getString("dial_code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
