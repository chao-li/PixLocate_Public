package clidev.pixlocate.MapSearchFunctions;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import clidev.pixlocate.Fragments.ExploreFragment;
import clidev.pixlocate.R;
import clidev.pixlocate.Utilities.NetworkUtilities;
import clidev.pixlocate.Utilities.Utilities;
import timber.log.Timber;

public class SearchLocationAction {

    private Context mContext;
    private LocationSearchHandler mLocationSearchHandler;


    // interface
    public interface LocationSearchHandler {
        void onSearchFieldEntered();
        void onSearchResultObtained(Address address);
        void onSearchResultFailed(Boolean isFailed);

    }

    // Constructor for activities
    public SearchLocationAction (Context context, LocationSearchHandler locationSearchHandler) {
        mContext = context;

        mLocationSearchHandler = locationSearchHandler;
    }


    // methods
    public void setSearchListener(final EditText searchEditText) {

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_GO ||
                        event.getAction() == KeyEvent.ACTION_DOWN ||
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    if (NetworkUtilities.isNetworkAvailable(mContext)) {
                        // Search have been entered
                        mLocationSearchHandler.onSearchFieldEntered();


                        // hide virtual keyboard
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

                        String text = searchEditText.getText().toString().trim();
                        Timber.d("Entered text is: " + text);
                        searchEditText.setText("", TextView.BufferType.EDITABLE);

                        new SearchClicked(text).execute();


                        return true;
                    } else {
                        NetworkUtilities.alertNetworkNotAvailable(mContext);
                        return false;
                    }
                }
                return false;
            }
        });



    }



    // Search for location in Async task.
    private class SearchClicked extends AsyncTask<Void, Void, Address> {
        private String toSearch;
        private Address address;

        public SearchClicked(String toSearch) {
            this.toSearch = toSearch;
        }

        @Override
        protected Address doInBackground(Void... voids) {

            try {
                Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(toSearch, 1);

                if (results.size() == 0) {
                    return null;
                }

                address = results.get(0);

                return address;

            } catch (Exception e) {
                Log.e("", "Something went wrong: ", e);
                return null;
            }

        }

        @Override
        protected void onPostExecute(Address address) {
            if (address != null) {
                mLocationSearchHandler.onSearchResultObtained(address);
            } else if (address == null) {
                mLocationSearchHandler.onSearchResultFailed(true);
            }
        }
    }

}
