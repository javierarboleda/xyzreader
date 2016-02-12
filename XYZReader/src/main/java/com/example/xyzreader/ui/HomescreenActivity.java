package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class HomescreenActivity extends Activity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TOP_STORY_URL = "topStoryUrl";
    public static final String TOP_STORY_AUTHOR = "topStoryAuthor";
    public static final String TOP_STORY_DATE = "topStoryDate";
    public static final String TOP_STORY_TITLE = "topStoryTitle";

    private RecyclerView mRecyclerView;

    private String mTopStoryDate;
    private String mTopStoryTitle;
    private String mTopStoryAuthor;
    private String mTopStoryUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_homescreen);

        ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout))
                .setTitle("XYZReader");

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
            loadTopStory();
        } else {
            mTopStoryTitle = savedInstanceState.getString(TOP_STORY_TITLE);
            mTopStoryAuthor = savedInstanceState.getString(TOP_STORY_AUTHOR);
            mTopStoryDate = savedInstanceState.getString(TOP_STORY_DATE);
            mTopStoryUrl = savedInstanceState.getString(TOP_STORY_URL);

            loadTopStoryViews(mTopStoryTitle, mTopStoryDate, mTopStoryAuthor, mTopStoryUrl);
        }


    }

    /**
     *  This method loads randomly a story into the top collapsing toolbar
     */
    private void loadTopStory() {

        Cursor cursor = getContentResolver().query(ItemsContract.Items.buildDirUri(),
                null,
                null,
                null,
                null);

        // We want to grab a story that isn't at the top so you don't notice it's a repeated story
        // to do this, going to pick a random story from the second half of total articles
        int position;
        int cursorCount = cursor.getCount();

        if (cursorCount > 1) {
            int half = cursorCount / 2;
            int remainder = cursorCount % 2;
            position = new Random().nextInt(half) + half + remainder;
        } else {
            position = 1;
        }

        cursor.moveToPosition(position);

        mTopStoryTitle = cursor.getString(ArticleLoader.Query.TITLE);
        mTopStoryDate = DateUtils.getRelativeTimeSpanString(
                cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL).toString();

        mTopStoryAuthor = cursor.getString(ArticleLoader.Query.AUTHOR);
        mTopStoryUrl = cursor.getString(ArticleLoader.Query.PHOTO_URL);

        cursor.close();

        loadTopStoryViews(mTopStoryTitle, mTopStoryDate, mTopStoryAuthor, mTopStoryUrl);
    }

    private void loadTopStoryViews(String title, String date, String author, String url) {
        TextView dateTextView = (TextView) findViewById(R.id.date_text_view);
        TextView authorTextView = (TextView) findViewById(R.id.author_text_view);
        TextView titleTextView = (TextView) findViewById(R.id.title_text_view);

        ImageView imageView = (ImageView) findViewById(R.id.top_story_image_view);

        dateTextView.setText(date);
        authorTextView.setText(author);
        titleTextView.setText(title);

        Picasso.with(this)
                .load(url)
                .into(imageView);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(TOP_STORY_TITLE, mTopStoryTitle);
        outState.putString(TOP_STORY_DATE, mTopStoryDate);
        outState.putString(TOP_STORY_AUTHOR, mTopStoryAuthor);
        outState.putString(TOP_STORY_URL, mTopStoryUrl);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // LoaderManager LoaderCallbacks: below methods belong to callbacks for LoaderManager
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }
    // /LoaderManager LoaderCallbacks:

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;

        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))));
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            mCursor.moveToPosition(position);

            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            String author = mCursor.getString(ArticleLoader.Query.AUTHOR);
            String date = DateUtils.getRelativeTimeSpanString(
                    mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString();
            String url = mCursor.getString(ArticleLoader.Query.THUMB_URL);
            ImageLoader imageLoader =
                    ImageLoaderHelper.getInstance(HomescreenActivity.this).getImageLoader();
            float aspectRatio = mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO);

            holder.titleView.setText(title);
            holder.author.setText(author);
            holder.date.setText(date);
            holder.thumbnailView.setImageUrl(url, imageLoader);
            holder.thumbnailView.setAspectRatio(aspectRatio);

            Log.d("HomescreenActivity", title);

        }

        @Override
        public int getItemCount() {
            return mCursor.getCount() - 1;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView author;
        public TextView date;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            author = (TextView) view.findViewById(R.id.article_author);
            date = (TextView) view.findViewById(R.id.article_date);
        }
    }
}
