package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
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
import com.example.xyzreader.data.ItemsDatabase;
import com.example.xyzreader.data.ItemsProvider;
import com.example.xyzreader.data.UpdaterService;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class HomescreenActivity extends Activity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;

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
        }

        final ViewHolder vh = new ViewHolder(findViewById(R.id.top_story_frame_layout));

        Cursor cursor = getContentResolver().query(ItemsContract.Items.buildDirUri(),
                null,
                null,
                null,
                null);

        int position = new Random().nextInt(cursor.getCount()) + 1;

        cursor.moveToPosition(position);

        String title = cursor.getString(ArticleLoader.Query.TITLE);
        String date = DateUtils.getRelativeTimeSpanString(
                cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL).toString();

        String author = cursor.getString(ArticleLoader.Query.AUTHOR);
        String url = cursor.getString(ArticleLoader.Query.PHOTO_URL);

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
        // okay, need to get a cursor, use new "Uri buildDirUri(int _id)" from ItemsContract
        // to get cursor with all results, then want to get id from first result, and use that
        // to get results without one of the items

//        Cursor c = getContentResolver().query(ItemsContract.Items.buildDirUri(),
//                null,
//                null,
//                null,
//                null);
//
//        c.moveToPosition(0);
//        int a = c.getInt(ArticleLoader.Query._ID);
//
//        c.moveToPosition(1);
//        int b = c.getInt(ArticleLoader.Query._ID);
//
//        c.moveToPosition(2);
//        int d = c.getInt(ArticleLoader.Query._ID);
//
//        String s = c.getString(2);
//
//        int id = c.getInt(ArticleLoader.Query._ID);
//        long l = 2345;
//        return ArticleLoader.newInstanceForMainList(this, s);
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
