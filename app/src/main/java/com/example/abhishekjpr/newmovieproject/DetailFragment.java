package com.example.abhishekjpr.newmovieproject;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by abhishekjpr on 17/6/16.
 */
public class DetailFragment extends Fragment implements View.OnClickListener {
    TextView originalTitle, releaseDate, description, voteAvg;
    ProgressDialog dialog;
    ImageButton favButton;
    String receiveMovieId, oTitle, rDate, vAvg, pPath, d;
    Bitmap map;
    ImageView mThumb;
    ArrayAdapter<String> adapter;
    ArrayList<String> key;
    ArrayList<String> list;

    ListView trailerListview;
    int trailerCount = 1;
    String trailerString = "Trailer ";
    ViewGroup.LayoutParams listViewParams;
    MovieTrailerInformation[] movieTrailerInformation;

    ViewGroup.LayoutParams reviewlistViewParams;
    ArrayList<HoldMovieReviews> listReview;
    ListView reviewListView;
    CustomAdapter myAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("OnActivityCreated-->", savedInstanceState+"");
        originalTitle = (TextView) getActivity().findViewById(R.id.detailOriginalTitle);
        releaseDate = (TextView) getActivity().findViewById(R.id.detailReleaseDate);
        description = (TextView) getActivity().findViewById(R.id.descriptionDetailTextView);
        voteAvg = (TextView) getActivity().findViewById(R.id.voteAvg);
        mThumb = (ImageView) getActivity().findViewById(R.id.detailImageView);
        favButton = (ImageButton) getActivity().findViewById(R.id.fav_button);
        favButton.setOnClickListener(this);
        key = new ArrayList<>();
        trailerListview = (ListView) getActivity().findViewById(R.id.trailers_List);
        reviewListView = (ListView) getActivity().findViewById(R.id.readListview);
        reviewListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(listReview.get(position).getUrl())));
            }
        });
        /* Important Code to set listview in scroolview*/
        listViewParams = trailerListview.getLayoutParams();
        reviewlistViewParams = reviewListView.getLayoutParams();
        trailerListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String movieKey = key.get(position);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+movieKey)));
            }
        });
        if(savedInstanceState == null && getActivity().findViewById(R.id.fragment_list_activity_main) == null){
            Intent intent = getActivity().getIntent();
            receiveMovieId = intent.getStringExtra(Intent.EXTRA_TEXT);
            new FetchMovieTask().execute(receiveMovieId);
        }
        else if(getActivity().findViewById(R.id.fragment_list_activity_main) != null && savedInstanceState == null){

        }
        else{
            originalTitle.setText(savedInstanceState.getString("otitle"));
            releaseDate.setText(savedInstanceState.getString("rdate"));
            description.setText(savedInstanceState.getString("des"));
            voteAvg.setText(savedInstanceState.getString("vote"));
            map = savedInstanceState.getParcelable("bitmap");
            mThumb.setImageBitmap(map);
            key = savedInstanceState.getStringArrayList("key");
            list = savedInstanceState.getStringArrayList("trailer");
            adapter = new ArrayAdapter<String>(getActivity(), R.layout.trailer_textview, R.id.movie_trailer_textview, list);
            if(list!=null&&adapter!=null)
            {
                trailerListview.setAdapter(adapter);
                listViewParams.height = 70*(trailerListview.getCount()+5);
                trailerListview.requestLayout();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.fav_button) {
            try {
                // Add a new student record
                ContentValues values = new ContentValues();
                values.put(MovieDataProvider.MOVIE_ID, receiveMovieId);
                byte[] arr = DbBitmapUtility.getBytes(map);
                values.put(MovieDataProvider.MOVIE_POSTER, arr);
                Uri uri = getActivity().getContentResolver().insert(
                        MovieDataProvider.CONTENT_URI, values);

                favButton.setImageResource(R.drawable.fav_marked);
                Toast.makeText(getActivity(), "Added to favourite..", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Already in favourites", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("otitle", originalTitle.getText().toString());
        outState.putString("rdate", releaseDate.getText().toString());
        outState.putString("des", description.getText().toString());
        outState.putString("vote", voteAvg.getText().toString());
        outState.putParcelable("bitmap", map);
        outState.putStringArrayList("trailer", list);
        outState.putStringArrayList("key", key);
        Log.d("OnSaveInst-->", outState+"");
    }


    public void receiveMovieId(String id){
        receiveMovieId = id;
        new FetchMovieTask().execute(receiveMovieId);
    }

    public class FetchMovieTask extends AsyncTask<String, Void, Void> {
        private String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle("Loading Information..");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void holdMovieData) {
            super.onPostExecute(holdMovieData);
            originalTitle.setText(oTitle);
            releaseDate.setText(rDate);
            description.setText(d);
            voteAvg.setText(vAvg);
            new DownloadPosterImageTask().execute(pPath);
        }

        @Override
        protected Void doInBackground(String... params) {
            String movieList = "";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL("https://api.themoviedb.org/3/movie/"+params[0]+"?api_key=1613321b441831f386b754347a517ec8");
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
                Log.d(LOG_TAG, e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.d(LOG_TAG, "Error in Closing stream");
                    }
                }
            }
            try {
                getMoviesDataFromJson(movieList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /* Get dara from json this method
        tooks a input and parse it and return back the result to the calling
        method
         */
        private void getMoviesDataFromJson(String moviesData)
                throws JSONException {

            final String OWM_POSTER_PATH = "poster_path";
            final String OWM_OVERVIEW = "overview";
            final String OWM_RELEASE_DATE = "release_date";
            final String OWM_VOTE_AVG = "vote_average";
            final String OWM_ORIGINAL_TITLE = "original_title";

            JSONObject moviesJson = new JSONObject(moviesData);
            oTitle = moviesJson.getString(OWM_ORIGINAL_TITLE);
            rDate = moviesJson.getString(OWM_RELEASE_DATE);
            vAvg = "Vote: " +moviesJson.getString(OWM_VOTE_AVG);
            d = moviesJson.getString(OWM_OVERVIEW);
            pPath = "http://image.tmdb.org/t/p/w500" +moviesJson.getString(OWM_POSTER_PATH);
        }
    }

    public class DownloadPosterImageTask extends AsyncTask<String, Void, Bitmap> {


        public DownloadPosterImageTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                new FetchMovieTrailers().execute(receiveMovieId);
            }catch (Exception e){
                System.out.print(e.getMessage());
            }
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                return mIcon11;
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            map = result;
            mThumb.setImageBitmap(map);
        }
    }

    public class FetchMovieTrailers extends AsyncTask<String, Void, MovieTrailerInformation[]>{
        final String LOG_TAG = MovieFragment.FetchMovieTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(MovieTrailerInformation[] strings) {
            super.onPostExecute(strings);
            try {
                System.out.println("--------------------------------");
                if (strings != null && strings.length != 0) {
                    movieTrailerInformation = strings;
                    int j = 0;
                    String[] trailerName = new String[movieTrailerInformation.length];
                    for (MovieTrailerInformation mti : strings) {
                        key.add(mti.getTrailerKey());
                        trailerName[j] = trailerString + " " + trailerCount + ": " + mti.getTrailerName();
                        j++;
                        trailerCount++;
                    }
                    list = new ArrayList<>(Arrays.asList(trailerName));

                    adapter = new ArrayAdapter<String>(getActivity(), R.layout.trailer_textview, R.id.movie_trailer_textview, list);
                    if (list != null && adapter != null) {
                        trailerListview.setAdapter(adapter);
                        listViewParams.height = 70 * (trailerListview.getCount() + 5);
                        trailerListview.requestLayout();
                    }
                    new FetchReviewTask().execute(receiveMovieId);
                } else {
                    dialog.dismiss();
                }
            }catch (Exception e){
                Log.d("Excep :: ", e.getMessage());
            }
            System.out.println("--------------------------------");
        }

        @Override
        protected MovieTrailerInformation[] doInBackground(String... params) {
            String movieList = "";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieId = params[0];
            try {
                URL url = new URL("http://api.themoviedb.org/3/movie/"+receiveMovieId+"?api_key=1613321b441831f386b754347a517ec8&append_to_response=videos");
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
                if (buffer.length() == 0 && buffer == null) {
                    return null;
                }
                movieList = buffer.toString();
            } catch (Exception e) {
                Log.d(LOG_TAG, e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.d(LOG_TAG, "Error in Closing stream");
                    }
                }
            }
            try{
                return getTrailersFromJson(movieList);
            }
            catch (Exception e){
                Log.d(LOG_TAG, "Error in JSON parsing..");
            }
            return null;
        }

        public MovieTrailerInformation[] getTrailersFromJson(String movieURL)
                throws JSONException{
            Log.d(LOG_TAG, movieURL);
            final String OWM_RESULT = "videos";
            final String OWM_URLS = "results";

            JSONObject moviesJson = new JSONObject(movieURL);
            JSONObject videoObject = moviesJson.getJSONObject(OWM_RESULT);
            JSONArray videoArray = videoObject.getJSONArray(OWM_URLS);

            MovieTrailerInformation[] urlArray = new MovieTrailerInformation[videoArray.length()];

            for(int i = 0; i < urlArray.length; i++){
                urlArray[i] = new MovieTrailerInformation();
                JSONObject movieDay = videoArray.getJSONObject(i);
                String name = movieDay.getString("name");
                String key = movieDay.getString("key");
                String siteName = movieDay.getString("site");
                urlArray[i].setTrailerName(name);
                urlArray[i].setTrailerKey(key);
                urlArray[i].setSiteName(siteName);
                System.out.println(urlArray[i].getTrailerName());
                System.out.println(urlArray[i].getTrailerKey());
                System.out.println(urlArray[i].getSiteName());
            }
            return urlArray;
        }
    }

    public class FetchReviewTask extends AsyncTask<String, Void, HoldMovieReviews[]> {
        final String LOG_TAG = MovieFragment.FetchMovieTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(HoldMovieReviews[] strings) {
            super.onPostExecute(strings);
            if(strings.length!=0 && strings!=null) {
                HoldMovieReviews[] holdData = new HoldMovieReviews[strings.length];
                for (int i = 0; i < holdData.length; i++)
                    holdData[i] = strings[i];
                listReview = new ArrayList<HoldMovieReviews>(Arrays.asList(holdData));
                myAdapter = new CustomAdapter(getActivity(), listReview);
                if(listReview!=null&&myAdapter!=null)
                {
                    reviewListView.setAdapter(myAdapter);
                    reviewlistViewParams.height = 195*(reviewListView.getCount()+5);
                    reviewListView.requestLayout();
                }
            }
            else{
                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("No Reviews Available");
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
            dialog.dismiss();
        }

        @Override
        protected HoldMovieReviews[] doInBackground(String... params) {
            String movieList = "";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieId = params[0];
            try {
                URL url = new URL("http://api.themoviedb.org/3/movie/" + movieId + "/reviews?api_key=1613321b441831f386b754347a517ec8");
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
                Log.d(LOG_TAG, e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.d(LOG_TAG, "Error in Closing stream");
                    }
                }
            }
            try {
                return getTrailersFromJson(movieList);
            } catch (Exception e) {
                Log.d(LOG_TAG, "Error in JSON parsing..");
            }
            return null;
        }

        public HoldMovieReviews[] getTrailersFromJson(String movieURL)
                throws JSONException {
            Log.d(LOG_TAG, movieURL);
            final String OWM_URLS = "results";

            JSONObject moviesJson = new JSONObject(movieURL);
            JSONArray videoArray = moviesJson.getJSONArray("results");

            HoldMovieReviews[] urlArray = new HoldMovieReviews[videoArray.length()];

            for (int i = 0; i < urlArray.length; i++) {

                urlArray[i] = new HoldMovieReviews();
                JSONObject movieDay = videoArray.getJSONObject(i);
                String authorName = "Author Name: "+movieDay.getString("author");
                String reviewURL = movieDay.getString("url");
                urlArray[i].setAuthor(authorName);
                urlArray[i].setUrl(reviewURL);
            }
            return urlArray;
        }
    }
}