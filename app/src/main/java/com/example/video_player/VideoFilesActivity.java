package com.example.video_player;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;


import com.example.video_player.Adapter.VideoFilesAdapter;
import com.example.video_player.models.MediaFiles;

import java.util.ArrayList;

public class VideoFilesActivity extends AppCompatActivity  implements SearchView.OnQueryTextListener{
    public  static  final String MY_PREF = "my pref";

    RecyclerView recyclerView;
    private ArrayList<MediaFiles> videoFilesArryaList = new ArrayList<>();
    static VideoFilesAdapter videoFilesAdapter;
    String folder_name;
    SwipeRefreshLayout swipeRefreshLayout;
    String sortOrder;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_files);
        folder_name = getIntent().getStringExtra("folderName");
        Toolbar toolbar = findViewById(R.id.my_toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(folder_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //  folder_name = getIntent().getStringExtra("folderName");


        recyclerView = findViewById(R.id.videos_rv);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_videos);
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREF, MODE_PRIVATE).edit();
        editor.putString("playlistFOlderName", folder_name);
        editor.apply();
        showVideoFiles();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showVideoFiles();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }
    private void  showVideoFiles(){
        videoFilesArryaList = fetchMedia(folder_name);
        videoFilesAdapter = new VideoFilesAdapter(videoFilesArryaList, this,0);
        recyclerView.setAdapter(videoFilesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        videoFilesAdapter.notifyDataSetChanged();
    }

    private  ArrayList <MediaFiles> fetchMedia(String folderName) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
        String sort_value = preferences.getString("sort","abcd");

        ArrayList<MediaFiles> videoFiles = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        if (sort_value.equals("sortName")) {
            sortOrder = MediaStore.MediaColumns.DISPLAY_NAME+" ASC";
        } else if (sort_value.equals("sortSize")) {
            sortOrder = MediaStore.MediaColumns.SIZE+" DESC";
        } else if (sort_value.equals("sortDate")) {
            sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";
        } else {
            sortOrder = MediaStore.Video.Media.DURATION+" DESC";
        }

        String selection = MediaStore.Video.Media.DATA+" like?";
        String[] selectionArg = new String[]{"%"+folderName+"%"};
        Cursor cursor = getContentResolver().query(uri, null,
                selection, selectionArg, sortOrder);
        if (cursor != null && cursor.moveToNext()) {
            do {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                @SuppressLint("Range") String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                @SuppressLint("Range") String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                @SuppressLint("Range") String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                MediaFiles mediaFiles = new MediaFiles(id, title, displayName, size, duration, path,
                        dateAdded);
                videoFiles.add(mediaFiles);
            }while (cursor.moveToNext());
        }
        return videoFiles;
    }
    public  boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.search_video);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    public  boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        int id = item.getItemId();
        if (id== R.id.refresh_files) {
            finish();
            startActivity(getIntent());
        } else if (id==R.id.sort_by) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Sort By");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    editor.apply();
                    finish();
                    startActivity(getIntent());
                    dialog.dismiss();
                }
            });

            String[] items = {"Name (A to Z)","Size (Big to Small)","Date (New to Old)",
                    "Length (Long to Short)"};
            alertDialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            editor.putString("sort","sortName");
                            break;
                        case 1:
                            editor.putString("sort","sortSize");
                            break;
                        case 2:
                            editor.putString("sort","sortDate");
                            break;
                        case 3:
                            editor.putString("sort","sortLength");
                            break;
                    }
                }
            });
            alertDialog.create().show();
            
        }else  {
            System.out.println("Raj mhato");
        }
        return super.onOptionsItemSelected(item);

    }
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    public boolean onQueryTextChange(String newText) {
        String inputs = newText.toLowerCase();
        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        for (MediaFiles media : videoFilesArryaList) {
            if (media.getTitle().toLowerCase().contains(inputs)) {
                mediaFiles.add(media);
            }
        }
        VideoFilesActivity.videoFilesAdapter.updateVideoFiles(mediaFiles);
        return true;
    }
}