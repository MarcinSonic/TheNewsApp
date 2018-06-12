package pl.marcingorski.thenewsapp;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements LoaderCallbacks <List <Article>> {

    public static final String LOG_TAG = MainActivity.class.getName ();

    /**
     * URL for news data from Guardian API
     */
    private static final String GUARDIAN_REQUEST_URL =
            "https://content.guardianapis.com/search?";

    // Constant value for article loader ID.

    private static final int ARTICLE_LOADER_ID = 1;

    /**
     * Adapter for the list of articles
     */
    private ArticleAdapter mAdapter;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate ( saveInstanceState );
        setContentView ( R.layout.activity_main );

        // Find a reference to the {@link ListView} in the layout
        ListView articleListView = findViewById ( R.id.list );

        mEmptyStateTextView = findViewById ( R.id.empty_view );
        articleListView.setEmptyView ( mEmptyStateTextView );

        // Create a new adapter that takes an empty list of articles as input
        mAdapter = new ArticleAdapter ( this, new ArrayList <Article> () );

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        articleListView.setAdapter ( mAdapter );

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected article.
        articleListView.setOnItemClickListener ( new AdapterView.OnItemClickListener () {
            @Override
            public void onItemClick(AdapterView <?> adapterView, View view, int position, long l) {
                Article currentArticle = mAdapter.getItem ( position );

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri articleUri = Uri.parse ( currentArticle.getmUrl () );
                // Create a new intent to view the article URI
                Intent websiteIntent = new Intent ( Intent.ACTION_VIEW, articleUri );

                // Send the intent to launch a new activity
                startActivity ( websiteIntent );
            }
        } );
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService ( Context.CONNECTIVITY_SERVICE );

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo ();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected ()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager ();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader ( ARTICLE_LOADER_ID, null, this );
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById ( R.id.loading_indicator );
            loadingIndicator.setVisibility ( View.GONE );

            // Update empty state with no connection error message
            mEmptyStateTextView.setText ( R.string.no_internet_connection );
        }
    }

    @Override
    public Loader <List <Article>> onCreateLoader(int i, Bundle args) {
        // Create url builder

        String TAG = "URI";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences ( this );

        Boolean business = sharedPreferences.getBoolean ( "business", false );
        Boolean fashion = sharedPreferences.getBoolean ( "fashion", false );
        Boolean lifeandstyle = sharedPreferences.getBoolean ( "lifeandstyle", false );
        Boolean science = sharedPreferences.getBoolean ( "science", false );
        Boolean technology = sharedPreferences.getBoolean ( "technology", false );

        Uri baseUri = Uri.parse ( GUARDIAN_REQUEST_URL );

        Uri.Builder uriBuilder = baseUri.buildUpon ();

        String sections = "";
        if (business) {
            sections += "business|";
        }
        if (fashion) {
            sections += "fashion|";
        }
        if (lifeandstyle) {
            sections += "lifeandstyle|";
        }
        if (science) {
            sections += "science|";
        }
        if (technology) {
            sections += "technology|";
        }
        if (sections.endsWith ( "|" )) {
            sections = sections.substring ( 0, sections.length () - 1 ) + "";
        }
        if (!sections.isEmpty ()) {
            uriBuilder.appendQueryParameter ( "section", sections );
        }
        uriBuilder.appendQueryParameter ( "show-tags", "contributor" );
        uriBuilder.appendQueryParameter ( "api-key", "test" );
        Log.d ( TAG, "uriBuilder: " + uriBuilder.toString () );
        return new ArticleLoader ( this, uriBuilder.toString () );
    }

    @Override
    public void onLoadFinished(Loader <List <Article>> loader, List <Article> articles) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById ( R.id.loading_indicator );
        loadingIndicator.setVisibility ( View.GONE );

        // Set empty state text to display "No articles found."
        mEmptyStateTextView.setText ( R.string.no_articles_found );

        // Clear the adapter of previous article data
        mAdapter.clear ();

        // If there is a valid list of {@link Arlicle}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (articles != null && !articles.isEmpty ()) {
            mAdapter.addAll ( articles );
        }
    }

    @Override
    public void onLoaderReset(Loader <List <Article>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear ();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater ().inflate ( R.menu.main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId ();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent ( this, SettingsActivity.class );
            startActivity ( settingsIntent );
            return true;
        }
        return super.onOptionsItemSelected ( item );
    }
}