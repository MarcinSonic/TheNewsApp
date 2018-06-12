package pl.marcingorski.thenewsapp;


import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Helper methods related to requesting and receiving news data from Guardian.
 */
public final class QueryUtils {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName ();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the Guardian dataset and return a list of {@link Article} objects.
     */
    public static List <Article> fetchNewsData(String requestUrl) {

        try {
            Thread.sleep ( 2000 );
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }

        // Create URL object
        URL url = createUrl ( requestUrl );

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest ( url );
        } catch (IOException e) {
            Log.e ( LOG_TAG, "Problem making the HTTP request.", e );
        }

        // Extract relevant fields from the JSON response and create a list of {@link Article}s
        List <Article> articles = extractFeatureFromJson ( jsonResponse );

        // Return the list of {@link Article}s
        return articles;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL ( stringUrl );
        } catch (MalformedURLException e) {
            Log.e ( LOG_TAG, "Problem building the URL ", e );
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection ();
            urlConnection.setReadTimeout ( 10000 /* milliseconds */ );
            urlConnection.setConnectTimeout ( 15000 /* milliseconds */ );
            urlConnection.setRequestMethod ( "GET" );
            urlConnection.connect ();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode () == 200) {
                inputStream = urlConnection.getInputStream ();
                jsonResponse = readFromStream ( inputStream );
            } else {
                Log.e ( LOG_TAG, "Error response code: " + urlConnection.getResponseCode () );
            }
        } catch (IOException e) {
            Log.e ( LOG_TAG, "Problem retrieving the articles JSON results.", e );
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect ();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close ();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder ();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader ( inputStream, Charset.forName ( "UTF-8" ) );
            BufferedReader reader = new BufferedReader ( inputStreamReader );
            String line = reader.readLine ();
            while (line != null) {
                output.append ( line );
                line = reader.readLine ();
            }
        }
        return output.toString ();
    }

    /**
     * Return a list of {@link Article} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List <Article> extractFeatureFromJson(String newsJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty ( newsJSON )) {
            return null;
        }

        // Create an empty ArrayList that we can start adding articles to
        List <Article> articles = new ArrayList <> ();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject ( newsJSON );

            JSONObject response = baseJsonResponse.getJSONObject ( "response" );
            JSONArray articleArray = response.getJSONArray ( "results" );

            for (int i = 0; i < articleArray.length (); i++) {

                // Get a single article at position i within the list of articles
                JSONObject currentArticle = articleArray.getJSONObject ( i );


                // Extract the value for the key called "webTitle"
                String titleOfArticle = currentArticle.getString ( "webTitle" );

                // Extract the value for the key called "section"
                String section = currentArticle.getString ( "sectionName" );

                // Extract the value for the key called "webPublicationDate"
                String datePublished = currentArticle.optString ( "webPublicationDate" );

                String dateFormated = "";
                try {
                    DateFormat dateFromWeb = new SimpleDateFormat ( "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault () );
                    Date date = dateFromWeb.parse ( datePublished );
                    DateFormat readableDate = new SimpleDateFormat ( "HH:mm    dd.MM.yyyy", Locale.getDefault () );
                    dateFormated = readableDate.format ( date );
                } catch (ParseException e) {
                    Log.e ( LOG_TAG, "date formating problem" );
                }

                // Extract the value for the key called "webUrl"
                String url = currentArticle.getString ( "webUrl" );

                // Extract Author Array - tags

                JSONArray newsAuthorArray = currentArticle.getJSONArray ( "tags" );

                String author = "Author Unavailable";

                if (newsAuthorArray.length () == 1) {
                    JSONObject newsAuthorObject = newsAuthorArray.getJSONObject ( 0 );
                    author = newsAuthorObject.getString ( "webTitle" );
                }

                // Create a new {@link Article} object from Json response
                Article article = new Article ( titleOfArticle, section, author, dateFormated, url );

                // Add the new {@link Article} to the list of articles.
                articles.add ( article );
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e ( "QueryUtils", "Problem parsing the articles JSON results", e );
        }

        // Return the list of articles
        return articles;
    }
}
