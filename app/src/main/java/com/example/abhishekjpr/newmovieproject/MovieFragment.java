package com.example.abhishekjpr.newmovieproject;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by abhishekjpr on 16/6/16.
 */
public class MovieFragment extends Fragment implements AdapterView.OnItemClickListener{
    List<HoldMovieData> listOfData;
    ArrayList<Bitmap> bitmapList;
    HoldMovieData[] myData;
    SharedPreferences prefs;
    ImageAdapter adapter;
    Communicator comm;
    ProgressDialog dialog;
    GridView moviesGridView;
    String spHolder = "";
    boolean check = true;
    DownloadImageTask imageTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings){
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Log.d("Action: ", "On Create View");
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        spHolder = prefs.getString(getString(R.string.pref_movie_key), getString(R.string.pref_movie_default));
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("Action: ", "On Activity Created");
        moviesGridView = (GridView) getActivity().findViewById(R.id.movieGridView);
        moviesGridView.setOnItemClickListener(this);
        bitmapList = new ArrayList<Bitmap>();
        comm = (Communicator) getActivity();
        if(savedInstanceState == null){
            new FetchMovieTask().execute(spHolder);
        }
        else{
            myData = (HoldMovieData[])savedInstanceState.getParcelableArray("moviedata");
            listOfData = new ArrayList<HoldMovieData>(Arrays.asList(myData));
            bitmapList = savedInstanceState.getParcelableArrayList("bitmaparray");
            adapter = new ImageAdapter(getActivity(), bitmapList);
            moviesGridView.setAdapter(adapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("bitmaparray", bitmapList);
        outState.putParcelableArray("moviedata", myData);
        Log.d("-->", "Saved Instance State Created");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        check = false;
        if(spHolder.equals("favourite")){
            if (getActivity().findViewById(R.id.fragment_dettaaill) != null) {

                comm.sendMovieId(myData[position].getId());
            } else {

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, myData[position].getId());
                startActivity(intent);
            }
        }else {
            if (getActivity().findViewById(R.id.fragment_dettaaill) != null) {

                comm.sendMovieId(listOfData.get(position).getId());
            } else {

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, listOfData.get(position).getId());
                startActivity(intent);
            }
        }
    }

    /*******************************************************************************************************/
    public class FetchMovieTask extends AsyncTask<String, Void, HoldMovieData[]> {
        private String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle("Loading Images..");
            dialog.show();
        }

        @Override
        protected HoldMovieData[] doInBackground(String... params) {
            if (params[0].equals("favourite")) {

            } else {
                String movieList = "";
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String preferenceView = params[0];
                try {
                    URL url = new URL("https://api.themoviedb.org/3/movie/" + preferenceView + "?page=1&api_key=1613321b441831f386b754347a517ec8");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    if (buffer.length() == 0) {
                        return null;
                    }
                    movieList = buffer.toString();

                } catch (Exception e) {

                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {

                        }
                    }
                    try {
                        return getMoviesDataFromJson(movieList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        public void getMyFavouriteMoviesList() {
            HoldMovieData[] holdMovieData = null;
            try {
                ContentResolver resolver = getActivity().getContentResolver();
                // Retrieve student records
                String URL = MovieDataProvider.URL;
                Uri movies = Uri.parse(URL);

                Cursor c = resolver.query(movies, null, null, null, null);
                holdMovieData = new HoldMovieData[c.getCount()];
                int i = 0;
                if (c.moveToFirst()) {
                    do {
                        holdMovieData[i] = new HoldMovieData();
                        holdMovieData[i].setId(c.getString(c.getColumnIndex(MovieDataProvider.MOVIE_ID)));
                        holdMovieData[i].setPosterPath("");
                        byte[] arr = c.getBlob(c.getColumnIndex(MovieDataProvider.MOVIE_POSTER));
                        Bitmap map = DbBitmapUtility.getImage(arr);
                        bitmapList.add(map);
                        i++;
                    } while (c.moveToNext());
                }
                myData = holdMovieData;
                adapter = new ImageAdapter(getActivity(), bitmapList);
                moviesGridView.setAdapter(adapter);
            }catch (Exception e){
                Log.d(LOG_TAG, "Error in fetching fav. movies "+e.getMessage());
            }
            dialog.dismiss();
        }

        @Override
        protected void onPostExecute(HoldMovieData[] holdMovieData) {
            super.onPostExecute(holdMovieData);
            if(holdMovieData==null){
                getMyFavouriteMoviesList();
                return;
            }else {
                try {
                    myData = holdMovieData;
                    if (listOfData == null) {
                        listOfData = new ArrayList<>(Arrays.asList(myData));
                        adapter = new ImageAdapter(getActivity(), bitmapList);
                        moviesGridView.setAdapter(adapter);
                        imageTask = new DownloadImageTask(listOfData, adapter);
                        imageTask.execute();
                    } else {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /* Get dara from json this method
        tooks a input and parse it and return back the result to the calling
        method
         */
        private HoldMovieData[] getMoviesDataFromJson(String moviesData)
                throws JSONException {

            final String OWM_RESULT = "results";
            final String OWM_ID = "id";
            final String OWM_POSTER_PATH = "poster_path";

            JSONObject moviesJson = new JSONObject(moviesData);
            JSONArray movieArray = moviesJson.getJSONArray(OWM_RESULT);
            HoldMovieData[] holdData = new HoldMovieData[movieArray.length()];
            for (int i = 0; i < holdData.length; i++) {
                String pPath, dTitle, overview, rDate, vCount, vAvg, oTitle, lang, backPath, id, genreInfo;
                genreInfo = "";
                JSONObject movieDay = movieArray.getJSONObject(i);
                pPath = "http://image.tmdb.org/t/p/w500" + movieDay.getString(OWM_POSTER_PATH);
                id = movieDay.getString(OWM_ID);
                holdData[i] = new HoldMovieData();
                holdData[i].setPosterPath(pPath);
                holdData[i].setId(id);
            }
            return holdData;
        }
    }
    /** Another inner class for downloading the images
     *
     * Class for downloading the images one by one
     ****/
    public class DownloadImageTask extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = DownloadImageTask.class.getSimpleName();

        ArrayList<HoldMovieData> listData;
        ImageAdapter adapter;

        public DownloadImageTask(List<HoldMovieData> listData, ImageAdapter adapter) {
            this.listData = (ArrayList) listData;
            this.adapter = adapter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(Void... urls) {
            outer:
            for (int i = 0; ((i < listData.size()) && (check==true)); i++) {
                String imageUrl = listData.get(i).getPosterPath();
                System.out.println(imageUrl);
                Bitmap mIcon11 = null;
                try {
                    mIcon11 = null;
                    InputStream in = new URL(imageUrl).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {

                }
                if (mIcon11 != null) {
                    bitmapList.add(mIcon11);
                }
                else {
                    Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.error_image);
                    bitmapList.add(largeIcon);
                }
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {

                    break outer;
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);dialog.dismiss();
        }
    }
}