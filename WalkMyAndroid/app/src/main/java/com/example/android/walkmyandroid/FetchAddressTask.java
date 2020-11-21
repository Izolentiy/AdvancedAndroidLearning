package com.example.android.walkmyandroid;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchAddressTask extends AsyncTask<Location, Void, String> {
    interface OnTaskCompleted {
        void onTaskCompleted(String result);
    }

    private static final String TAG = "FetchAddressTaskTAG";

    private final WeakReference<Context> mContext;
    private final OnTaskCompleted mListener;

    public FetchAddressTask(Context applicationContext, OnTaskCompleted listener) {
        mContext = new WeakReference<>(applicationContext);
        mListener = listener;
    }

    @Override
    protected String doInBackground(Location... locations) {
        Context context = mContext.get();
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        Location location = locations[0];

        List<Address> addresses = null;
        String resultMessage = "";

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ioException) {
            // Catch network of other I/O problems
            resultMessage = context.getString(R.string.service_not_available);
            Log.d(TAG, resultMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid longitude or latitude values
            resultMessage = context.getString(R.string.ivalid_args);
            Log.d(TAG, resultMessage, illegalArgumentException);
        }

        if (addresses == null || addresses.size() == 0) {
            if (resultMessage.isEmpty()) {
                resultMessage = context.getString(R.string.no_address_found);
                Log.d(TAG, resultMessage);
            }
        } else {
            // If address is found, read it into result message
            Address address = addresses.get(0);
            ArrayList<String> addressParts = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them and send them to the main thread
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressParts.add(address.getAddressLine(i));
            }

            resultMessage = TextUtils.join("\n", addressParts);
        }

        return resultMessage;
    }

    @Override
    protected void onPostExecute(String address) {
        mListener.onTaskCompleted(address);
        super.onPostExecute(address);
    }
}
