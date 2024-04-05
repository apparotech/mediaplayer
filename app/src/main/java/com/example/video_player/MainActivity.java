package com.example.video_player;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;

import com.example.video_player.Adapter.VideoFoldersAdapter;
import com.example.video_player.models.MediaFiles;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
    private  ArrayList<String> allFolderList = new ArrayList<>();
    RecyclerView recyclerView;
    VideoFoldersAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    @RequiresApi(api = Build.VERSION_CODES.M)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.folders_rv);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_folders);
        showFolders();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showFolders();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
     private  void showFolders(){
        mediaFiles = fetchMedia();
        adapter = new VideoFoldersAdapter(mediaFiles,allFolderList,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL,false));
        adapter.notifyDataSetChanged();

     }
    @SuppressLint("Range")
     public  ArrayList<MediaFiles> fetchMedia(){
        ArrayList<MediaFiles> mediaFilesArrayList = new ArrayList<>();
         Uri uri  = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

         Cursor cursor = getContentResolver().query(uri, null,
                 null,null,null);
         if (cursor !=null && cursor.moveToNext()){
             do{
                 String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                 String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                 String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                 String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                 String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                 String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                 String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                 MediaFiles mediaFiles = new MediaFiles(id, title, displayName, size, duration, path,
                         dateAdded);
                 int index = path.lastIndexOf("/");
                 String subString = path.substring(0,index);
                 if (!allFolderList.contains(subString)) {
                     allFolderList.add(subString);
                 }
                 mediaFilesArrayList.add(mediaFiles);
             } while (cursor.moveToNext());
         }
         return  mediaFilesArrayList;
     }
     public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.folder_menu,menu);
        return super.onCreateOptionsMenu(menu);
     }
     public  boolean pnOptionItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        if (id== R.id.rateus){
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id="
            +getApplicationContext().getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        } else if (id== R.id.refresh_folders) {
            finish();
            startActivity(getIntent());
        } else if (id==R.id.share_app) {
            Intent share_intent = new Intent();
            share_intent.setAction(Intent.ACTION_SEND);
            share_intent.putExtra(Intent.EXTRA_TEXT,"Check this app via\n"+
                    "https://play.google.com/store/apps/details?id="
                    + getApplicationContext().getPackageName());
            share_intent.setType("text/plain");
            startActivity(Intent.createChooser(share_intent,"Share app via"));

        } else {
            System.out.println("Raj mahto");
        }
        return  true;
     }

}