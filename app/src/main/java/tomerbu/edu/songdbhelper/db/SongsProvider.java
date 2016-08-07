package tomerbu.edu.songdbhelper.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

public class SongsProvider extends ContentProvider {
    public static final String AUTHORITY = "edu.tomerbu";
    public static final Uri SONGS_URI = Uri.parse("content://" + AUTHORITY + "/Songs");

    private static final int SONGS = 10;
    private static final int SONGS_ID = 11;
    private SongDBHelper helper;

    private UriMatcher matcher;

    @Override
    public boolean onCreate() {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "Songs", SONGS);
        matcher.addURI(AUTHORITY, "Songs/#", SONGS_ID);

        helper = new SongDBHelper(getContext());
        return false;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (matcher.match(uri)) {
            case SONGS:
                long insertedID = helper.getWritableDatabase().insert("Songs", null, values);
                Uri insertedURI = SONGS_URI.buildUpon().appendPath(insertedID + "").build();
                notifyChange(uri);
                return insertedURI;
            default:
                throw new UnsupportedOperationException("No Such Uri");
        }
    }

    private void notifyChange(Uri uri) {
        Context c = getContext();
        assert c != null;
        c.getContentResolver().notifyChange(uri, null);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        switch (matcher.match(uri)) {
            case SONGS:
                int deleteCount = helper.getWritableDatabase().delete("Songs", selection, selectionArgs);
                notifyChange(uri);
                return deleteCount;

            case SONGS_ID:
                selection = getUpdatedSelection(uri, selection);
                int deleteCounts = helper.getWritableDatabase().delete("Songs", selection, selectionArgs);
                notifyChange(uri);
                return deleteCounts;
            default:
                throw new UnsupportedOperationException("No Such Uri");
        }

    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        switch (matcher.match(uri)) {
            case SONGS:
                Cursor cursor = helper.getWritableDatabase().query("Songs", projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case SONGS_ID:
                selection = getUpdatedSelection(uri, selection);
                Cursor c = helper.getWritableDatabase().query("Songs", projection, selection, selectionArgs, null, null, sortOrder);
                c.setNotificationUri(getContext().getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("No Such Uri");
        }
    }


    @Override
    public String getType(Uri uri) {

        // at the given URI.
        switch (matcher.match(uri)) {
            case SONGS:
                return "Songs";
            case SONGS_ID:
                return "Songs_ID";
            default:
                throw new UnsupportedOperationException("Not Implelented");
        }
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        switch (matcher.match(uri)) {
            case SONGS:
                int updatedCount = helper.getWritableDatabase().update("Songs", values, selection, selectionArgs);
                notifyChange(uri);
                return updatedCount;
            case SONGS_ID:
                selection = getUpdatedSelection(uri, selection);
                int count = helper.getWritableDatabase().update("Songs", values, selection, selectionArgs);
                notifyChange(uri);
                return count;
            default:
                throw new UnsupportedOperationException("No Such uri");

        }
    }

    private String getUpdatedSelection(Uri uri, String selection) {
        String id = uri.getLastPathSegment();
        if (!TextUtils.isEmpty(selection)) {
            selection = selection + "  _ID = " + id;
        } else {
            selection = "  _ID = " + id;
        }
        return selection;
    }
}
