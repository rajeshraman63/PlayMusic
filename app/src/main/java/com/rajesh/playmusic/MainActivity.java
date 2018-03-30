package com.rajesh.playmusic;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ImageButton imgBtn_stop;
    ImageButton imgBtn_play_pause;
    ImageButton imgBtn_next;
    ImageButton imgBtn_prev;

    ListView listView_songs;
    TextView tv_number_of_songs;
    TextView tv_now_playing;


    private int play=1;
    private int positionOfPlayingSong = 0;
    private int number_of_songs = -1;
    private String nowSong;

    private ArrayList<String> song_list = new ArrayList<>();
    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ArrayList<File> songFiles_list = new ArrayList<>();


    boolean notPlayedAnyMusic=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imgBtn_stop = findViewById(R.id.imgBtn_stop);
        imgBtn_play_pause = findViewById(R.id.imgBtn_play_pause);
        imgBtn_prev = findViewById(R.id.imgBtn_prev);
        imgBtn_next = findViewById(R.id.imgBtn_next);
        listView_songs = findViewById(R.id.listView_songs);
        tv_number_of_songs = findViewById(R.id.tv_number_of_songs);
        tv_now_playing = findViewById(R.id.tv_now_playing);



        boolean permit = isStoragePermissionGranted();
        //Checking RunTime Permission
        if (permit) {

           // Toast.makeText(this,"Executed",Toast.LENGTH_SHORT).show();

            // Listing all the songs list in the external directory


            File root = Environment.getExternalStorageDirectory();
            songFiles_list = findSongs(root);   // ArrayList of music files




            number_of_songs = songFiles_list.size();
            String countSongs = number_of_songs +" songs";
            tv_number_of_songs.setText(countSongs);


            for (File file : songFiles_list) {
                song_list.add(file.getName());   // song_list contains the music names
            }


            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, song_list);
            listView_songs.setAdapter(adapter);

            /*final ArrayAdapter<File> adapter = new ArrayAdapter<File>(this, android.R.layout.simple_list_item_1, songFiles_list);
            listView_songs.setAdapter(adapter); */


            listView_songs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Toast.makeText(getApplicationContext(),adapter.getItem(position)+"",Toast.LENGTH_SHORT).show();

                    positionOfPlayingSong = position;

                    playPositionMusic(positionOfPlayingSong,"");

                }
            });





            // stop button in the music player

            imgBtn_stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mediaPlayer.release();
                    mediaPlayer=null;
                    mediaPlayer = new MediaPlayer();
                    imgBtn_play_pause.setImageResource(R.drawable.ic_play);
                    play=0;

                    nowSong = "Music Stopped  \n ";
                    tv_now_playing.setText(nowSong);
                    notPlayedAnyMusic=true;
                }
            });



            // pause and play button in the music player

            imgBtn_play_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(play==1) {
                        if (notPlayedAnyMusic) {   // at the starting case
                            //playNext();

                            positionOfPlayingSong = (positionOfPlayingSong+1)%number_of_songs;
                            playPositionMusic(positionOfPlayingSong,"The");


                        } else {
                            imgBtn_play_pause.setImageResource(R.drawable.ic_play);
                            mediaPlayer.pause();
                            nowSong = tv_now_playing.getText().toString();
                            nowSong=nowSong.replaceAll("Now Playing","Music Paused");
                            tv_now_playing.setText(nowSong);
                            play=0;
                        }

                    }else if(mediaPlayer!=null){
                        if(notPlayedAnyMusic){    // for stop case
                            //playNext();

                            positionOfPlayingSong = (positionOfPlayingSong+1)%number_of_songs;
                            playPositionMusic(positionOfPlayingSong,"");

                        }else {

                            imgBtn_play_pause.setImageResource(R.drawable.ic_pause);
                            mediaPlayer.start();

                            nowSong = tv_now_playing.getText().toString();
                            nowSong = nowSong.replaceAll("Music Paused", "Now Playing");
                            tv_now_playing.setText(nowSong);

                            play = 1;
                        }
                    }
                }
            });




            // playing the previous  song in the list


            imgBtn_prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //playPrev();

                    positionOfPlayingSong = (positionOfPlayingSong-1)%number_of_songs;
                    if(positionOfPlayingSong == -1){
                        positionOfPlayingSong = number_of_songs-1;
                    }
                    playPositionMusic(positionOfPlayingSong,"Previous");

                }
            });


            // playing the next song in the list
            imgBtn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getApplicationContext(),"Playing next song",Toast.LENGTH_SHORT).show();
                    //playNext();

                    positionOfPlayingSong = (positionOfPlayingSong+1)%number_of_songs;
                    playPositionMusic(positionOfPlayingSong,"Next");

                }
            });


        }
    }





    // play music at a specific location

    public void playPositionMusic(int position,String pos){


        mediaPlayer.release();    // after stoping it has null value so
        mediaPlayer = null;
        mediaPlayer = new MediaPlayer();

        notPlayedAnyMusic=false;

        // now music started playing
        imgBtn_play_pause.setImageResource(R.drawable.ic_pause);
        play=1;   // to pause the music in next click of the pause/play button


        Toast.makeText(getApplicationContext(),"Playing " + pos + " song : \n" + song_list.get(position),Toast.LENGTH_SHORT).show();


        nowSong = "Now Playing -:-  \n ";
        nowSong += song_list.get(position);
        tv_now_playing.setText(nowSong);


        // Playing the music  after clicking the song in the list
        Uri uri = Uri.parse(songFiles_list.get(position).toString());
        try {
            mediaPlayer.setDataSource(getApplicationContext(),uri);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
                mediaPlayer = null;
                //mediaPlayer = new MediaPlayer();
                imgBtn_play_pause.setImageResource(R.drawable.ic_play);
                play=0;

                //playNext();  // to play the next song
                positionOfPlayingSong = (positionOfPlayingSong+1)%number_of_songs;
                playPositionMusic(positionOfPlayingSong,"Next");

            }
        });

    }







    // find songs

    public ArrayList<File> findSongs(File root){

        ArrayList<File> al_songs = new ArrayList<>();

        File[] files = root.listFiles();

        for(File singleFile : files){

            if(singleFile.isDirectory()   && !singleFile.getName().equals("MIUI")) {
                al_songs.addAll(findSongs(singleFile));
            }else{
                if(singleFile.getName().endsWith(".mp3")){
                    al_songs.add(singleFile);
                }
            }
        }

        return al_songs;
    }




    // RunTime Permission


    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    ) {

                Log.v("Permission Tag : ","Permission is granted");
                return true;
            } else {

                Log.v("Permission Tag","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Permission Tag : ","Permission is granted");
            return true;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("Permission Tag : ","Permission: "+permissions[0]+ "was "+grantResults[0]);
            Log.v("Permission Tag : ","Permission: "+permissions[1]+ "was "+grantResults[1]);

            //resume tasks needing this permission
        }
    }


}
