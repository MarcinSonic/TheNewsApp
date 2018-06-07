package pl.marcingorski.thenewsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ArticleAdapter extends ArrayAdapter <Article> {

    /**
     * Constructs a new {@link ArticleAdapter}.
     *
     * @param context  of the app
     * @param articles is the list of articles, which is the data source of the adapter
     */

    public ArticleAdapter(Context context, List <Article> articles) {
        super ( context, 0, articles );
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from ( getContext () ).inflate (
                    R.layout.news_list_item, parent, false );
        }

        //Find the article at the given position in the list of articles
        Article currentArticle = getItem ( position );
        // Find all text Views:
        TextView titleOfArticle = listItemView.findViewById ( R.id.title_of_article );
        titleOfArticle.setText ( currentArticle.getTitleOfArticle () );

        TextView section = listItemView.findViewById ( R.id.section );
        section.setText ( currentArticle.getSection () );

        TextView author = listItemView.findViewById ( R.id.author );
        author.setText ( currentArticle.getAuthor () );

        TextView datePublished = listItemView.findViewById ( R.id.date_of_publish );
        datePublished.setText ( currentArticle.getDatePublished () );

        // Return the list item view that is now showing the appropriate data
        return listItemView;
    }

}
