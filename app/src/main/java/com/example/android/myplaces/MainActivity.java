package com.example.android.myplaces;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.LoaderManager.LoaderCallbacks;

import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;


public class MainActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {

    private final LatLng LOCATION_UNIV= new LatLng(33.783768, -118.114336);
    private final LatLng LOCATION_ECS= new LatLng(33.782777, -118.111868);


    GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());


        if(status!=ConnectionResult.SUCCESS){

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        }else {


            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


            googleMap = fm.getMap();


            googleMap.setMyLocationEnabled(true);


            getSupportLoaderManager().initLoader(0, null, this);
        }

        googleMap.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {



                drawMarker(point);


                ContentValues contentValues = new ContentValues();


                contentValues.put(LocationsDB.FIELD_LAT, point.latitude );


                contentValues.put(LocationsDB.FIELD_LNG, point.longitude);


                contentValues.put(LocationsDB.FIELD_ZOOM, googleMap.getCameraPosition().zoom);


                LocationInsertTask insertTask = new LocationInsertTask();


                insertTask.execute(contentValues);

                Toast.makeText(getBaseContext(), "Marker is added to the Map", Toast.LENGTH_SHORT).show();

            }
        });


        googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {


                googleMap.clear();


                LocationDeleteTask deleteTask = new LocationDeleteTask();


                deleteTask.execute();

                Toast.makeText(getBaseContext(), "All markers are removed", Toast.LENGTH_LONG).show();

            }
        });
    }


    public void onClick_ECS(View v) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 14);
        googleMap.animateCamera(update);
    }


    public void onClick_LongBeach(View v) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_ECS, 16);
        googleMap.animateCamera(update);
    }

    public void onClick_City(View v) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 9);
        googleMap.animateCamera(update);
    }






    private void drawMarker(LatLng point){

        MarkerOptions markerOptions = new MarkerOptions();


        markerOptions.position(point);


        googleMap.addMarker(markerOptions);
    }


    private class LocationInsertTask extends AsyncTask<ContentValues, Void, Void>{
        @Override
        protected Void doInBackground(ContentValues... contentValues) {


            getContentResolver().insert(LocationsContentProvider.CONTENT_URI, contentValues[0]);
            return null;
        }
    }

    private class LocationDeleteTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {


            getContentResolver().delete(LocationsContentProvider.CONTENT_URI, null, null);
            return null;
        }
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        this.closeOptionsMenu();

        return true;
    }








    @Override
    public Loader<Cursor> onCreateLoader(int arg0,
                                         Bundle arg1) {


        Uri uri = LocationsContentProvider.CONTENT_URI;


        return new CursorLoader(this, uri, null, null, null, null);

    }


    @Override
    public void onLoadFinished(Loader<Cursor> arg0,
                               Cursor arg1) {
        int locationCount = 0;
        double lat=0;
        double lng=0;
        float zoom=0;


        if(arg1 != null){
            locationCount = arg1.getCount();
            arg1.moveToFirst();
        }else{
            locationCount=0;
        }



        for(int i=0;i<locationCount;i++){


            lat = arg1.getDouble(arg1.getColumnIndex(LocationsDB.FIELD_LAT));


            lng = arg1.getDouble(arg1.getColumnIndex(LocationsDB.FIELD_LNG));


            zoom = arg1.getFloat(arg1.getColumnIndex(LocationsDB.FIELD_ZOOM));


            LatLng location = new LatLng(lat, lng);


            drawMarker(location);

            arg1.moveToNext();
        }

        if(locationCount>0){

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lng)));


            googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub
    }
}