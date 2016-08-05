package tech.salroid.filmy.Activity;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.StringDef;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import tech.salroid.filmy.Custom.BreathingProgress;
import tech.salroid.filmy.Database.FilmContract;
import tech.salroid.filmy.CustomAdapter.MovieDetailsActivityAdapter;
import tech.salroid.filmy.Fragments.CastFragment;
import tech.salroid.filmy.Fragments.FullReadFragment;
import tech.salroid.filmy.R;
import tech.salroid.filmy.Network.VolleySingleton;

public class MovieDetailsActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    Context context = this;
    String movie_id_final;
    private String movie_id;
    private String trailer = null, movie_desc;

    private RelativeLayout header, main;
    BreathingProgress breathingProgress;
    private RequestQueue requestQueue;


    private static TextView det_title, det_tagline, det_overview,
            det_rating, det_released, det_certification,
            det_language, det_runtime;

    private static ImageView youtube_link, banner;
    private final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();
    private final int MOVIE_DETAILS_LOADER = 2, SAVED_MOVIE_DETAILS_LOADER = 5;
    LinearLayout trailorBackground;
    TextView tvRating;
    FrameLayout trailorView, newMain, headerContainer;
    FullReadFragment fullReadFragment;
    HashMap<String, String> movieMap;
    boolean networkApplicable = false, databaseApplicable = false, savedDatabaseApplicable = false;
    int type;


    private static final String[] GET_MOVIE_COLUMNS = {

            FilmContract.MoviesEntry.MOVIE_TITLE,
            FilmContract.MoviesEntry.MOVIE_BANNER,
            FilmContract.MoviesEntry.MOVIE_DESCRIPTION,
            FilmContract.MoviesEntry.MOVIE_TAGLINE,
            FilmContract.MoviesEntry.MOVIE_TRAILER,
            FilmContract.MoviesEntry.MOVIE_RATING,
            FilmContract.MoviesEntry.MOVIE_LANGUAGE,
            FilmContract.MoviesEntry.MOVIE_RELEASED,
            FilmContract.MoviesEntry.MOVIE_CERTIFICATION,
            FilmContract.MoviesEntry.MOVIE_RUNTIME,
    };


    private static final String[] GET_SAVE_COLUMNS = {

            FilmContract.SaveEntry.SAVE_ID,
            FilmContract.SaveEntry.SAVE_TITLE,
            FilmContract.SaveEntry.SAVE_BANNER,
            FilmContract.SaveEntry.SAVE_DESCRIPTION,
            FilmContract.SaveEntry.SAVE_TAGLINE,
            FilmContract.SaveEntry.SAVE_TRAILER,
            FilmContract.SaveEntry.SAVE_RATING,
            FilmContract.SaveEntry.SAVE_LANGUAGE,
            FilmContract.SaveEntry.SAVE_RELEASED,
            FilmContract.SaveEntry._ID,
            FilmContract.SaveEntry.SAVE_YEAR,
            FilmContract.SaveEntry.SAVE_CERTIFICATION,
            FilmContract.SaveEntry.SAVE_RUNTIME,
            FilmContract.SaveEntry.SAVE_POSTER_LINK,
    };


    private ImageView youtube_play_button;

    private String cast_json = null, movie_title = null, movie_tagline = null, movie_rating = null, show_centre_img_url = null, movie_trailer = null;
    private boolean trailer_boolean = false;
    private FrameLayout main_content;
    private String quality;
    boolean cache=true;
    private String banner_for_full_activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        det_title = (TextView) findViewById(R.id.detail_title);
        det_tagline = (TextView) findViewById(R.id.detail_tagline);
        det_overview = (TextView) findViewById(R.id.detail_overview);
        det_rating = (TextView) findViewById(R.id.detail_rating);
        youtube_link = (ImageView) findViewById(R.id.detail_youtube);
        banner = (ImageView) findViewById(R.id.bannu);
        youtube_play_button = (ImageView) findViewById(R.id.play_button);
        trailorBackground = (LinearLayout) findViewById(R.id.trailorBackground);
        tvRating = (TextView) findViewById(R.id.tvRating);
        det_released = (TextView) findViewById(R.id.detail_released);
        det_certification = (TextView) findViewById(R.id.detail_certification);
        det_runtime = (TextView) findViewById(R.id.detail_runtime);
        det_language = (TextView) findViewById(R.id.detail_language);
        trailorView = (FrameLayout) findViewById(R.id.trailorView);


        breathingProgress = (BreathingProgress) findViewById(R.id.breathingProgress);

        main = (RelativeLayout) findViewById(R.id.main);
        newMain = (FrameLayout) findViewById(R.id.new_main);
        main_content = (FrameLayout) findViewById(R.id.all_details_container);
        header = (RelativeLayout) findViewById(R.id.header);
        headerContainer = (FrameLayout) findViewById(R.id.header_container);


        SharedPreferences prefrence = PreferenceManager.getDefaultSharedPreferences(MovieDetailsActivity.this);
        quality = prefrence.getString("image_quality", "medium");
        cache=prefrence.getBoolean("cache",false);

        headerContainer.setOnClickListener(this);

        newMain.setOnClickListener(this);

        trailorView.setOnClickListener(this);

        Intent intent = getIntent();
        getDataFromIntent(intent);

        //this should be called only when coming from the mainActivity and searchActivity & from
        //characterDetailsActivity
        if (networkApplicable)
            getMovieDetailsFromNetwork();

        if (databaseApplicable)
            getSupportLoaderManager().initLoader(MOVIE_DETAILS_LOADER, null, this);

        if (savedDatabaseApplicable)
            getSupportLoaderManager().initLoader(SAVED_MOVIE_DETAILS_LOADER, null, this);

        if (!databaseApplicable && !savedDatabaseApplicable) {

            main.setVisibility(View.INVISIBLE);
            breathingProgress.setVisibility(View.VISIBLE);

        }


        if (savedInstanceState==null)
            performReveal();


    }

    private void performReveal() {

        final FrameLayout allDetails = (FrameLayout) findViewById(R.id.all_details_container);

        if(allDetails!=null){

            ViewTreeObserver viewTreeObserver = allDetails.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        circularRevealActivity(allDetails);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            allDetails.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            allDetails.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }

        }

    }



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void circularRevealActivity(FrameLayout allDetails) {

        int cx = allDetails.getWidth() / 2;
        int cy = allDetails.getHeight() / 2;

        float finalRadius = Math.max(allDetails.getWidth(), allDetails.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(allDetails, cx, cy, 0, finalRadius);
        circularReveal.setDuration(1000);

        // make the view visible and start the animation
        allDetails.setVisibility(View.VISIBLE);
        circularReveal.start();
    }



    private void getDataFromIntent(Intent intent) {

        if (intent != null) {

            networkApplicable = intent.getBooleanExtra("network_applicable", false);

            databaseApplicable = intent.getBooleanExtra("database_applicable", false);

            savedDatabaseApplicable = intent.getBooleanExtra("saved_database_applicable", false);

            type = intent.getIntExtra("type",0);

            movie_id = intent.getStringExtra("id");

            movie_title = intent.getStringExtra("title");

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void getMovieDetailsFromNetwork() {

        VolleySingleton volleySingleton = VolleySingleton.getInstance();
        requestQueue = volleySingleton.getRequestQueue();

        final String BASE_URL_MOVIE_DETAILS = new String("http://api.themoviedb.org/3/movie/" + movie_id + "?api_key=b640f55eb6ecc47b3433cfe98d0675b1&append_to_response=trailers");
        JsonObjectRequest jsonObjectRequestForMovieDetails = new JsonObjectRequest(Request.Method.GET, BASE_URL_MOVIE_DETAILS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("webi",response.toString());
                        parseMovieDetails(response.toString());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // Log.e("webi", "Volley Error: " + error.getCause());

            }
        }
        );

        requestQueue.add(jsonObjectRequestForMovieDetails);

        showCastFragment();


    }

    private void showCastFragment() {



    }


    void parseMovieDetails(String movieDetails) {


        String title, tagline, overview, banner_profile, runtime, language, released, poster;
        double rating;
        String img_url = null;

        try {


            JSONObject jsonObject = new JSONObject(movieDetails);

            ContentValues contentValues = new ContentValues();

            title = jsonObject.getString("title");
            tagline = jsonObject.getString("tagline");
            overview = jsonObject.getString("overview");
            released = jsonObject.getString("release_date");
            runtime = jsonObject.getString("runtime");
            language = jsonObject.getString("original_language");

            //check the values correcly

            movie_id_final = jsonObject.getString("imdb_id");



            CastFragment castFragment = CastFragment.newInstance(movie_id_final,title);
            getSupportFragmentManager().
                    beginTransaction().
                    replace(R.id.cast_container, castFragment)
                    .commit();




            JSONObject trailorsObject = jsonObject.getJSONObject("trailers");
            JSONArray youTubeArray = trailorsObject.getJSONArray("youtube");

            Log.d("webi"," youtube length "+youTubeArray.length());

            JSONObject singleTrailor = youTubeArray.getJSONObject(0);
            String trailor = singleTrailor.getString("source");

            trailer = "https://www.youtube.com/watch?v="+trailor;

          banner_for_full_activity="http://image.tmdb.org/t/p/500"+jsonObject.getString("backdrop_path");

            banner_profile = "http://image.tmdb.org/t/p/w500"+jsonObject.getString("backdrop_path");
            poster = "http://image.tmdb.org/t/p/w185"+jsonObject.getString("poster_path");


           /* rating = jsonObject.getDouble("rating");
            certification = jsonObject.getString("certification");

            if (certification.equals("null")) {
                certification = "--";
            }

            double roundOff = Math.round(rating * 100.0) / 100.0;

            movie_rating = String.valueOf(roundOff);*/

            String genre="";

            JSONArray genreArray=jsonObject.getJSONArray("genres");

            for(int i=0 ;i<genreArray.length();i++){

                String finalgenre=genreArray.getJSONObject(i).getString("name");

                String punctuation=", ";

                if(i==genre.length())
                    punctuation="";

                genre=genre+punctuation+finalgenre;

            }

            movie_desc = overview;
            movie_title = title;
            movie_tagline = tagline;
            show_centre_img_url = banner_for_full_activity;

            movieMap = new HashMap<String, String>();
            movieMap.put("title", title);
            movieMap.put("tagline", tagline);
            movieMap.put("overview", overview);
          //  movieMap.put("rating", movie_rating);
           movieMap.put("certification", genre);
            movieMap.put("language", language);
            movieMap.put("year", "0");
            movieMap.put("released", released);
            movieMap.put("runtime", runtime);
            movieMap.put("trailer", trailer);
            movieMap.put("banner", banner_profile);
            movieMap.put("poster", poster);
            movieMap.put("id",movie_id_final);

            try {

                if (!(trailer.equals("null"))) {

                    trailer_boolean = true;
                    String videoId = extractYoutubeId(trailer);
                    img_url = "http://img.youtube.com/vi/" + videoId + "/0.jpg";

                    //  movie_trailer=trailer;

                } else {

                    img_url = jsonObject.getJSONObject("images").getJSONObject("poster").getString(quality);

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } finally {

                if (databaseApplicable) {

                    contentValues.put(FilmContract.MoviesEntry.MOVIE_BANNER, banner_profile);
                    contentValues.put(FilmContract.MoviesEntry.MOVIE_TAGLINE, tagline);
                    contentValues.put(FilmContract.MoviesEntry.MOVIE_DESCRIPTION, overview);
                    contentValues.put(FilmContract.MoviesEntry.MOVIE_TRAILER, img_url);
                    contentValues.put(FilmContract.MoviesEntry.MOVIE_CERTIFICATION, genre);
                    contentValues.put(FilmContract.MoviesEntry.MOVIE_LANGUAGE, language);
                    contentValues.put(FilmContract.MoviesEntry.MOVIE_RUNTIME, runtime);
                    contentValues.put(FilmContract.MoviesEntry.MOVIE_RELEASED, released);
                    contentValues.put(FilmContract.MoviesEntry.MOVIE_RATING, movie_rating);
                    //contentValues.put(FilmContract.MoviesEntry.MOVIE_ID,movie_id_final);




                    switch (type){

                        case 0:

                            final String selection =
                                    FilmContract.MoviesEntry.TABLE_NAME +
                                            "." + FilmContract.MoviesEntry.MOVIE_ID + " = ? ";
                            final String[] selectionArgs = {movie_id};

                            long id = context.getContentResolver().update(FilmContract.MoviesEntry.buildMovieByTag(movie_id), contentValues, selection, selectionArgs);

                            if (id != -1) {
                                //  Log.d(LOG_TAG, "Movie row updated with new values.");
                            }

                            break;

                        case 1:

                            final String selection2 =
                                    FilmContract.InTheatersMoviesEntry.TABLE_NAME +
                                            "." + FilmContract.MoviesEntry.MOVIE_ID + " = ? ";
                            final String[] selectionArgs2 = {movie_id};

                            long id2 = context.getContentResolver().update(FilmContract.InTheatersMoviesEntry.buildMovieByTag(movie_id), contentValues, selection2, selectionArgs2);

                            if (id2 != -1) {
                                //  Log.d(LOG_TAG, "Movie row updated with new values.");
                            }
                            break;

                        case 2:


                            final String selection3 =
                                    FilmContract.UpComingMoviesEntry.TABLE_NAME +
                                            "." + FilmContract.MoviesEntry.MOVIE_ID + " = ? ";
                            final String[] selectionArgs3 = {movie_id};

                            long id3 = context.getContentResolver().update(FilmContract.UpComingMoviesEntry.buildMovieByTag(movie_id), contentValues, selection3, selectionArgs3);

                            if (id3 != -1) {
                                //  Log.d(LOG_TAG, "Movie row updated with new values.");
                            }

                            break;

                    }

                    /*if (!movie_id_final.equals(movie_id)){
                        //loader failed show with this hack

                        showParsedContent(title, banner_profile, img_url, tagline, overview, movie_rating, runtime, released, genre, language);

                    }*/


                } else {

                    showParsedContent(title, banner_profile, img_url, tagline, overview, movie_rating, runtime, released, genre, language);

                }


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void showParsedContent(String title, String banner_profile, String img_url, String tagline,
                                   String overview, String rating, String runtime,
                                   String released, String certification, String language) {


        det_title.setText(title);
        det_tagline.setText(tagline);
        det_overview.setText(overview);
        det_rating.setText(rating);

        det_runtime.setText(runtime + " mins");
        det_released.setText(released);
        det_certification.setText(certification);
        det_language.setText(language);


        Glide.with(context)
                .load(banner_profile)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                        banner.setImageBitmap(resource);

                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            public void onGenerated(Palette p) {
                                // Use generated instance
                                Palette.Swatch swatch = p.getVibrantSwatch();
                                Palette.Swatch trailorSwatch = p.getDarkVibrantSwatch();

                                if (swatch != null) {
                                    header.setBackgroundColor(swatch.getRgb());
                                    det_title.setTextColor(swatch.getTitleTextColor());
                                    det_tagline.setTextColor(swatch.getBodyTextColor());
                                    det_overview.setTextColor(swatch.getBodyTextColor());


                                }
                                if (trailorSwatch != null) {
                                    trailorBackground.setBackgroundColor(trailorSwatch.getRgb());
                                    tvRating.setTextColor(trailorSwatch.getTitleTextColor());
                                    det_rating.setTextColor(trailorSwatch.getBodyTextColor());
                                }
                            }
                        });

                    }
                });


        Glide.with(context)
                .load(img_url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                        youtube_link.setImageBitmap(resource);
                        if (trailer_boolean)
                            youtube_play_button.setVisibility(View.VISIBLE);
                    }

                });

        main.setVisibility(View.VISIBLE);
        breathingProgress.setVisibility(View.INVISIBLE);

    }





    public String extractYoutubeId(String url) throws MalformedURLException {
        String query = new URL(url).getQuery();
        String[] param = query.split("&");
        String id = null;
        for (String row : param) {
            String[] param1 = row.split("=");
            if (param1[0].equals("v")) {
                id = param1[1];
            }
        }
        return id;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader cursorloader = null;

        if (id == MOVIE_DETAILS_LOADER) {

            switch (type){

                case 0:

                    cursorloader = new CursorLoader(this, FilmContract.MoviesEntry.buildMovieWithMovieId(movie_id), GET_MOVIE_COLUMNS, null, null, null);
                    break;

                case 1:

                    cursorloader = new CursorLoader(this, FilmContract.InTheatersMoviesEntry.buildMovieWithMovieId(movie_id), GET_MOVIE_COLUMNS, null, null, null);
                    break;

                case 2:

                    cursorloader = new CursorLoader(this, FilmContract.UpComingMoviesEntry.buildMovieWithMovieId(movie_id), GET_MOVIE_COLUMNS, null, null, null);
                    break;

            }

        } else if (id == SAVED_MOVIE_DETAILS_LOADER) {

            final String selection = FilmContract.SaveEntry.TABLE_NAME +
                    "." + FilmContract.SaveEntry.SAVE_ID + " = ? ";
            String[] selectionArgs = {movie_id};

            cursorloader = new CursorLoader(this, FilmContract.SaveEntry.CONTENT_URI, GET_SAVE_COLUMNS, selection, selectionArgs, null);

        }

        return cursorloader;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        int id = loader.getId();

        if (id == MOVIE_DETAILS_LOADER) {

            fetchMovieDetailsFromCursor(data);

        } else if (id == SAVED_MOVIE_DETAILS_LOADER) {

            fetchSavedMovieDetailsFromCursor(data);
        }

    }

    private void fetchSavedMovieDetailsFromCursor(Cursor data) {

        if (data != null && data.moveToFirst()) {

            int title_index = data.getColumnIndex(FilmContract.SaveEntry.SAVE_TITLE);
            int banner_index = data.getColumnIndex(FilmContract.SaveEntry.SAVE_BANNER);
            int tagline_index = data.getColumnIndex(FilmContract.SaveEntry.SAVE_TAGLINE);
            int description_index = data.getColumnIndex(FilmContract.SaveEntry.SAVE_DESCRIPTION);
            int trailer_index = data.getColumnIndex(FilmContract.SaveEntry.SAVE_TRAILER);
            int rating_index = data.getColumnIndex(FilmContract.SaveEntry.SAVE_RATING);
            int released_index = data.getColumnIndex(FilmContract.SaveEntry.SAVE_RATING);
            int runtime_index = data.getColumnIndex(FilmContract.SaveEntry.SAVE_RUNTIME);
            int language_index = data.getColumnIndex(FilmContract.SaveEntry.SAVE_LANGUAGE);
            int certification_index = data.getColumnIndex(FilmContract.SaveEntry.SAVE_CERTIFICATION);

            int poster_link_index = data.getColumnIndex(FilmContract.SaveEntry.SAVE_POSTER_LINK);

            String title = data.getString(title_index);
            String banner_url = data.getString(banner_index);
            String tagline = data.getString(tagline_index);
            String overview = data.getString(description_index);

            //as it will be used to show it on YouTube
            trailer = data.getString(trailer_index);
            String posterLink = data.getString(poster_link_index);

            String rating = data.getString(rating_index);
            String runtime = data.getString(runtime_index);
            String released = data.getString(released_index);
            String certification = data.getString(certification_index);
            String language = data.getString(language_index);


            det_title.setText(title);
            det_tagline.setText(tagline);
            det_overview.setText(overview);
            det_rating.setText(rating);

            det_runtime.setText(runtime + " mins");
            det_released.setText(released);
            det_certification.setText(certification);
            det_language.setText(language);

            movie_desc = overview;
            show_centre_img_url = banner_url;


            Glide.with(context)
                    .load(banner_url)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                            banner.setImageBitmap(resource);

                            Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette p) {
                                    // Use generated instance
                                    Palette.Swatch swatch = p.getMutedSwatch();
                                    Palette.Swatch trailorSwatch = p.getDarkVibrantSwatch();

                                    if (swatch != null) {

                                        header.setBackgroundColor(swatch.getRgb());
                                        det_title.setTextColor(swatch.getTitleTextColor());
                                        det_tagline.setTextColor(swatch.getBodyTextColor());
                                        det_overview.setTextColor(swatch.getBodyTextColor());

                                    }
                                    if (trailorSwatch != null) {
                                        trailorBackground.setBackgroundColor(trailorSwatch.getRgb());
                                        tvRating.setTextColor(trailorSwatch.getTitleTextColor());
                                        det_rating.setTextColor(trailorSwatch.getBodyTextColor());
                                    }
                                }
                            });

                        }
                    });


            String thumbNail = null;

            if (!trailer.equals("null")) {

                trailer_boolean = true;

                try {

                    String videoId = extractYoutubeId(trailer);
                    thumbNail = "http://img.youtube.com/vi/" + videoId + "/0.jpg";

                } catch (Exception e) {

                }

            } else {
                thumbNail = posterLink;
            }


            //    Toast.makeText(this, thumbNail, Toast.LENGTH_LONG).show();


            Glide.with(context)
                    .load(thumbNail)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                            youtube_link.setImageBitmap(resource);
                            if (trailer_boolean)
                                youtube_play_button.setVisibility(View.VISIBLE);
                        }

                    });


        }


        showCastFragment();

    }

    private void fetchMovieDetailsFromCursor(Cursor data) {

        if (data != null && data.moveToFirst()) {

            int title_index = data.getColumnIndex(FilmContract.MoviesEntry.MOVIE_TITLE);
            int banner_index = data.getColumnIndex(FilmContract.MoviesEntry.MOVIE_BANNER);
            int tagline_index = data.getColumnIndex(FilmContract.MoviesEntry.MOVIE_TAGLINE);
            int description_index = data.getColumnIndex(FilmContract.MoviesEntry.MOVIE_DESCRIPTION);
            int trailer_index = data.getColumnIndex(FilmContract.MoviesEntry.MOVIE_TRAILER);
            int rating_index = data.getColumnIndex(FilmContract.MoviesEntry.MOVIE_RATING);
            int released_index = data.getColumnIndex(FilmContract.MoviesEntry.MOVIE_RELEASED);
            int runtime_index = data.getColumnIndex(FilmContract.MoviesEntry.MOVIE_RUNTIME);
            int language_index = data.getColumnIndex(FilmContract.MoviesEntry.MOVIE_LANGUAGE);
            int certification_index = data.getColumnIndex(FilmContract.MoviesEntry.MOVIE_CERTIFICATION);

            String title = data.getString(title_index);
            String banner_url = data.getString(banner_index);
            String tagline = data.getString(tagline_index);
            String overview = data.getString(description_index);
            String trailer = data.getString(trailer_index);
            String rating = data.getString(rating_index);
            String runtime = data.getString(runtime_index);
            String released = data.getString(released_index);
            String certification = data.getString(certification_index);
            String language = data.getString(language_index);


            det_title.setText(title);
            det_tagline.setText(tagline);
            det_overview.setText(overview);
            det_rating.setText(rating);

            det_runtime.setText(runtime + " mins");
            det_released.setText(released);
            det_certification.setText(certification);
            det_language.setText(language);


            Glide.with(context)
                    .load(banner_url)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                            banner.setImageBitmap(resource);

                            Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette p) {
                                    // Use generated instance
                                    Palette.Swatch swatch = p.getMutedSwatch();
                                    Palette.Swatch trailorSwatch = p.getDarkVibrantSwatch();

                                    if (swatch != null) {

                                        header.setBackgroundColor(swatch.getRgb());
                                        det_title.setTextColor(swatch.getTitleTextColor());
                                        det_tagline.setTextColor(swatch.getBodyTextColor());
                                        det_overview.setTextColor(swatch.getBodyTextColor());

                                    }
                                    if (trailorSwatch != null) {
                                        trailorBackground.setBackgroundColor(trailorSwatch.getRgb());
                                        tvRating.setTextColor(trailorSwatch.getTitleTextColor());
                                        det_rating.setTextColor(trailorSwatch.getBodyTextColor());
                                    }
                                }
                            });

                        }
                    });


            Glide.with(context)
                    .load(trailer)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                            youtube_link.setImageBitmap(resource);
                            if (trailer_boolean)
                                youtube_play_button.setVisibility(View.VISIBLE);
                        }

                    });


        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {


            case android.R.id.home:

                finish();
                break;

            case R.id.action_search:

                movie_trailer = "http://www.imdb.com/title/" + movie_id_final;

                if (!(movie_title.equals(null) && movie_rating.equals("null") && movie_id_final.equals("null"))) {
                    Intent myIntent = new Intent(Intent.ACTION_SEND);
                    myIntent.setType("text/plain");
                    myIntent.putExtra(Intent.EXTRA_TEXT, "*" + movie_title + "*\n" + movie_tagline + "\nRating: " + movie_rating + " / 10\n" + movie_trailer + "\n");
                    startActivity(Intent.createChooser(myIntent, "Share with"));
                }

                break;

            case R.id.action_save:

                saveMovie();

                break;

        }

        return super.onOptionsItemSelected(item);
    }


    private void saveMovie() {

        if (movieMap != null && !movieMap.isEmpty()) {


            final ContentValues saveValues = new ContentValues();

            saveValues.put(FilmContract.SaveEntry.SAVE_ID, movie_id_final);
            saveValues.put(FilmContract.SaveEntry.SAVE_TITLE, movieMap.get("title"));
            saveValues.put(FilmContract.SaveEntry.SAVE_TAGLINE, movieMap.get("tagline"));
            saveValues.put(FilmContract.SaveEntry.SAVE_DESCRIPTION, movieMap.get("overview"));
            saveValues.put(FilmContract.SaveEntry.SAVE_BANNER, movieMap.get("banner"));
            saveValues.put(FilmContract.SaveEntry.SAVE_TRAILER, movieMap.get("trailer"));
            saveValues.put(FilmContract.SaveEntry.SAVE_RATING, movieMap.get("rating"));
            saveValues.put(FilmContract.SaveEntry.SAVE_YEAR, movieMap.get("year"));
            saveValues.put(FilmContract.SaveEntry.SAVE_POSTER_LINK, movieMap.get("poster"));
            saveValues.put(FilmContract.SaveEntry.SAVE_RUNTIME, movieMap.get("runtime"));
            saveValues.put(FilmContract.SaveEntry.SAVE_CERTIFICATION, movieMap.get("certification"));
            saveValues.put(FilmContract.SaveEntry.SAVE_LANGUAGE, movieMap.get("language"));
            saveValues.put(FilmContract.SaveEntry.SAVE_RELEASED, movieMap.get("released"));


            final String selection =
                    FilmContract.SaveEntry.TABLE_NAME +
                            "." + FilmContract.SaveEntry.SAVE_ID + " = ? ";
            final String[] selectionArgs = {movie_id};

            //  boolean deletePermission = false;
            Cursor alreadyCursor = context.getContentResolver().query(FilmContract.SaveEntry.CONTENT_URI, null, selection, selectionArgs, null);

            if (alreadyCursor.moveToFirst()) {
                //Already present in databse
                Snackbar.make(main_content, "Already present in database", Snackbar.LENGTH_SHORT).show();

            } else {

                final Cursor returnedCursor = context.getContentResolver().query(FilmContract.SaveEntry.CONTENT_URI, null, null, null, null);


                if (returnedCursor.moveToFirst() && returnedCursor.getCount() == 10) {
                    //No space to fill more. Have to delete oldest entry to save this Agree?

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Remove");
                    alertDialog.setIcon(R.drawable.ic_delete_sweep_black_24dp);

                    final TextView input = new TextView(context);
                    FrameLayout container = new FrameLayout(context);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(96, 48, 96, 48);
                    input.setLayoutParams(params);

                    input.setText("Save Limit reached , want to remove the oldest movie and save this one ?");
                    input.setTextColor(Color.parseColor("#303030"));

                    container.addView(input);


                    alertDialog.setView(container);
                    alertDialog.setPositiveButton("Okay",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    final String deleteSelection = FilmContract.SaveEntry.TABLE_NAME + "." + FilmContract.SaveEntry._ID + " = ? ";

                                    returnedCursor.moveToFirst();

                                    //Log.d(LOG_TAG, "This is the last index value which is going to be deleted "+returnedCursor.getInt(0));

                                    final String[] deletionArgs = {String.valueOf(returnedCursor.getInt(0))};


                                    long deletion_id = context.getContentResolver().delete(FilmContract.SaveEntry.CONTENT_URI, deleteSelection, deletionArgs);

                                    if (deletion_id != -1) {

                                        // Log.d(LOG_TAG, "We deleted this row" + deletion_id);

                                        Uri uri = context.getContentResolver().insert(FilmContract.SaveEntry.CONTENT_URI, saveValues);

                                        long movieRowId = ContentUris.parseId(uri);

                                        if (movieRowId != -1) {
                                            //inserted
                                            Snackbar.make(main_content, "Movie Saved", Snackbar.LENGTH_SHORT).show();

                                        } else {

                                            // Log.d(LOG_TAG, "row not Inserted in database");
                                        }

                                    } else {

                                        //delete was unsuccessful
                                    }

                                    dialog.cancel();
                                }
                            });

                    alertDialog.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Write your code here to execute after dialog
                                    dialog.cancel();
                                }
                            });

                    alertDialog.show();
                } else {

                    Uri uri = context.getContentResolver().insert(FilmContract.SaveEntry.CONTENT_URI, saveValues);

                    long movieRowId = ContentUris.parseId(uri);

                    if (movieRowId != -1) {

                        Snackbar.make(main_content, "Movie Saved", Snackbar.LENGTH_SHORT).show();

                        // Toast.makeText(MovieDetailsActivity.this, "Movie Inserted", Toast.LENGTH_SHORT).show();

                    } else {

                        Snackbar.make(main_content, "Movie Not Saved", Snackbar.LENGTH_SHORT).show();

                    }
                }
            }
        }
    }


    @Override
    public void onBackPressed() {

        FullReadFragment fragment = (FullReadFragment) getSupportFragmentManager().findFragmentByTag("DESC");
        if (fragment != null && fragment.isVisible()) {
            getSupportFragmentManager().beginTransaction().remove(fullReadFragment).commit();
        } else {
            super.onBackPressed();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.movie_detail_menu, menu);

        menu.findItem(R.id.action_save).setVisible(!savedDatabaseApplicable);

        return true;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.header_container:


                if (movie_title != null && movie_desc != null) {

                    fullReadFragment = new FullReadFragment();
                    Bundle args = new Bundle();
                    args.putString("title", movie_title);
                    args.putString("desc", movie_desc);
                    fullReadFragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.all_details_container, fullReadFragment, "DESC").commit();
                }

                break;

            case R.id.new_main:

                if (!(show_centre_img_url == null)) {
                    Intent intent = new Intent(MovieDetailsActivity.this, FullScreenImage.class);
                    intent.putExtra("img_url", show_centre_img_url);
                    startActivity(intent);
                }

                break;

            case R.id.trailorView:

                if ((trailer_boolean))
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailer)));

                break;

        }
    }
}