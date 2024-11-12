
package com.jmsoft.utility.UtilityTools;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.jmsoft.basic.UtilityTools.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationHelper {
    private final int REQUEST_CODE_ASK_PERMISSIONS = 11090;
    private final int REQUEST_CODE_CHECK_ACCURACY = 2201;
    private final String[] REQUIRED_SDK_PERMISSIONS = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    Activity activity;
    LocationHelperCallback locationHelperCallback;
    private LocationCallback locationCallback;

    public LocationHelper(Activity activity, LocationHelperCallback locationHelperCallback) {
        this.activity = activity;
        this.locationHelperCallback = locationHelperCallback;
    }


    public int getLocationMode() {
        try {
            return Settings.Secure.getInt(activity.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * This methods checks for the permission status
     * if all the permissions are granted, the result will be pushed to {{@link #onRequestLocationPermissionsResult(int, int[])}
     * if not, it will request the permissions mentioned in {{@link #REQUIRED_SDK_PERMISSIONS}} with request code {@link #REQUEST_CODE_ASK_PERMISSIONS}
     */
    public void getCurrentLocation() {
        final List<String> missingPermissions = new ArrayList<>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(activity, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            activity.onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS, grantResults);
        }
    }

    /**
     * This method shows a non dismissible popup if the permission is not granted
     * On the Button Action, the user is redirected to the app's setting page
     */
    private void displayNeverAskAgainDialog() {
        locationHelperCallback.locationPermissionDenied();
    }

    public void onRequestLocationPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            boolean Flag = false;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Flag = true;
                    break;
                }
            }
            if (Flag) {
                displayNeverAskAgainDialog();
            } else {
                if (getLocationMode() != 3) {
                    activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_CHECK_ACCURACY);
                    return;
                }
                if (!canGetLocation()) {
                    enableLocationSettings();
                } else {
                    GetDataBasedOnCL();
                }
            }
        }
    }

    /**
     * Here the Current Location is fetched which is then passed on for the Geocode in order to fetch
     */
    @SuppressLint("MissingPermission")
    void GetDataBasedOnCL() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        if (locationCallback == null) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            fusedLocationClient.removeLocationUpdates(locationCallback);
                            locationHelperCallback.locationFetched(location);
                        } else {
                            locationHelperCallback.locationFetchFailure();
                        }
                    }
                }
            };
        }

        LocationRequest locationRequest = createLocationRequest();
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest.Builder(1500)// The fastest interval between updates
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY) // Use high accuracy for real-time updates
                .build();
    }

    public void onLocationActivityResult(int requestCode, int resultCode) {
        if (REQUEST_CODE_CHECK_ACCURACY == requestCode) {
            Utils.E("REQUEST_CODE_CHECK_ACCURACY :: " + resultCode + " Can get location :: " + canGetLocation());
            if (resultCode == RESULT_OK) {
                getCurrentLocation();
            } else {
                locationHelperCallback.locationPermissionDenied();
            }
        }
    }

    /**
     * @return true if the location can be fetched, i.e, the GPS is enabled && network is enabled
     */
    public boolean canGetLocation() {
        boolean gpsEnabled;
        boolean networkEnabled;
        LocationManager lm = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gpsEnabled && networkEnabled;
    }

    /**
     * Show a popup to enable GPS if disabled, also checks for the GPS Status
     */
    protected void enableLocationSettings() {
        LocationRequest locationRequest = new LocationRequest.Builder(1000).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build()).addOnSuccessListener(activity, (LocationSettingsResponse response) -> GetDataBasedOnCL()).addOnFailureListener(activity, ex -> {
            if (ex instanceof ResolvableApiException) {
                // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) ex;
                    resolvable.startResolutionForResult(activity, REQUEST_CODE_CHECK_ACCURACY);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    public void openSettings(ActivityResultLauncher<Intent> mStartForResult) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        mStartForResult.launch(intent);
    }

   }

