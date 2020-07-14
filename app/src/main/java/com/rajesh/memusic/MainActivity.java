package com.rajesh.memusic;

import android.content.res.AssetFileDescriptor;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements  Runnable{

    ImageView imgBtn_stop;
    ImageView imgBtn_play_pause;
    ImageView imgBtn_next;
    ImageView imgBtn_prev;

    ListView listView_songs;
    TextView tv_number_of_songs;
    TextView tv_now_playing;


    private int play = 1;
    private int positionOfPlayingSong = 0;
    private int number_of_songs = -1;
    private String nowSong;

    private ArrayList<String> song_list = new ArrayList<>();
    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ArrayList<File> songFiles_list = new ArrayList<>();



    boolean notPlayedAnyMusic = true;

    private SeekBar seekBar;
    private TextView seekBarHint;

    private  Thread playMusicThread;

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

        seekBar = findViewById(R.id.seekbar);
        seekBarHint = findViewById(R.id.seekBarHint);

        boolean permit = isStoragePermissionGranted();
        //Checking RunTime Permission
        if (permit) {

            // Toast.makeText(this,"Executed",Toast.LENGTH_SHORT).show();

            // Listing all the songs list in the external directory


            File root = Environment.getExternalStorageDirectory();
            songFiles_list = findSongs(root);   // ArrayList of music files


            Collections.sort(songFiles_list, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });


            number_of_songs = songFiles_list.size();
            String countSongs = number_of_songs + " songs";
            tv_number_of_songs.setText(countSongs);


            for (File file : songFiles_list) {
                song_list.add(file.getName());   // song_list contains the music names
            }

//            Collections.sort(song_list);

            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, song_list);
            listView_songs.setAdapter(adapter);

            /*final ArrayAdapter<File> adapter = new ArrayAdapter<File>(this, android.R.layout.simple_list_item_1, songFiles_list);
            listView_songs.setAdapter(adapter); */


            listView_songs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                  //  Toast.makeText(getApplicationContext(), adapter.getItem(position) + "", Toast.LENGTH_SHORT).show();

                    positionOfPlayingSong = position;

                    playPositionMusic(positionOfPlayingSong, "");

                }
            });


            // stop button in the music player

            imgBtn_stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    mediaPlayer = new MediaPlayer();
                    imgBtn_play_pause.setImageResource(R.drawable.play_icon);
                    play = 0;

                    nowSong = "Stopped  \n ";
                    tv_now_playing.setText(nowSong);
                    notPlayedAnyMusic = true;
                }
            });


            // pause and play button in the music player

            imgBtn_play_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (play == 1) {
                        if (notPlayedAnyMusic) {   // at the starting case
                            //playNext();

                            positionOfPlayingSong = (positionOfPlayingSong + 1) % number_of_songs;
                            playPositionMusic(positionOfPlayingSong, "The");


                        } else {
                            imgBtn_play_pause.setImageResource(R.drawable.play_icon);

                            // seekbar --


                            mediaPlayer.pause();
                            nowSong = tv_now_playing.getText().toString();
                            nowSong = nowSong.replaceAll("Playing", "Paused");
                            tv_now_playing.setText(nowSong);
                            play = 0;
                        }

                    } else if (mediaPlayer != null) {
                        if (notPlayedAnyMusic) {    // for stop case
                            //playNext();

                            positionOfPlayingSong = (positionOfPlayingSong + 1) % number_of_songs;
                            playPositionMusic(positionOfPlayingSong, "");

                        } else {

//                            imgBtn_play_pause.setImageResource(R.drawable.ic_pause);
                            imgBtn_play_pause.setImageResource(R.drawable.pause);

                            mediaPlayer.start();

                            nowSong = tv_now_playing.getText().toString();
                            nowSong = nowSong.replaceAll("Paused", "Playing");
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

                    positionOfPlayingSong = (positionOfPlayingSong - 1) % number_of_songs;
                    if (positionOfPlayingSong == -1) {
                        positionOfPlayingSong = number_of_songs - 1;
                    }
                    playPositionMusic(positionOfPlayingSong, "Previous");

                }
            });


            // playing the next song in the list
            imgBtn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getApplicationContext(),"Playing next song",Toast.LENGTH_SHORT).show();
                    //playNext();

                    positionOfPlayingSong = (positionOfPlayingSong + 1) % number_of_songs;
                    playPositionMusic(positionOfPlayingSong, "Next");

                }
            });



            // ---------------- SeekBar Functionalities -----------------------

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                    seekBarHint.setVisibility(View.VISIBLE);
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                    seekBarHint.setVisibility(View.VISIBLE);
                    int x = (int) Math.ceil(progress / 1000f);

                    Log.i("Duration : " , x + " ");

                    if (x < 10)
                        seekBarHint.setText("0:0" + x);
                    else if(x < 59)
                        seekBarHint.setText("0:" + x);
                    else{
                        int min = x/60;
                        int sec = x%60;

                        String temp;

                        if(min < 10 ){
                            temp = "0:"+min;
                        }else{
                            temp = min+"";
                        }

                        if(sec < 10){
                            temp += ":0" +  sec;
                        }else{
                            temp += ":" + sec;
                        }

                        seekBarHint.setText(temp);
                    }

                    double percent = progress / (double) seekBar.getMax();
                    int offset = seekBar.getThumbOffset();
                    int seekWidth = seekBar.getWidth();
                    int val = (int) Math.round(percent * (seekWidth - 2 * offset));
                    int labelWidth = seekBarHint.getWidth();

                    //seekBarHint.setX(offset + seekBar.getX() + val - Math.round(percent * offset)- Math.round(percent * labelWidth / 2));

                    if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                       // clearMediaPlayer();
                        //fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));

                        if (play == 1)
                            MainActivity.this.seekBar.setProgress(0);
                    }


                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {


                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.seekTo(seekBar.getProgress());
                    }
                }
            });


        }
    }


    @Override
    public void run() {

        int currentPosition = mediaPlayer.getCurrentPosition();
        int total = mediaPlayer.getDuration();


        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {


            try {
                Thread.sleep(1000);
                currentPosition = mediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }

            seekBar.setProgress(currentPosition);
        }

    }


//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        clearMediaPlayer();
//    }
//
//    private void clearMediaPlayer() {
//        mediaPlayer.stop();
//        mediaPlayer.release();
//        mediaPlayer = null;
//    }


    // play music at a specific location

    public void playPositionMusic(int position, String pos)  {


//        if(mediaPlayer != null)
        mediaPlayer.release();    // after stoping it has null value so
        mediaPlayer = null;
        mediaPlayer = new MediaPlayer();

        notPlayedAnyMusic = false;


        // now music started playing
        imgBtn_play_pause.setImageResource(R.drawable.pause);
        play = 1;   // to pause the music in next click of the pause/play button


//        Toast.makeText(getApplicationContext(), "Playing " + pos + " song : \n" + song_list.get(position), Toast.LENGTH_SHORT).show();


        nowSong = "Playing:   ";
        nowSong += song_list.get(position);
        tv_now_playing.setText(nowSong);


        // Playing the music  after clicking the song in the list
        Uri uri = Uri.parse(songFiles_list.get(position).toString());
        try {

            // seekbar ---

//            AssetFileDescriptor descriptor = getAssets().openFd(song_list.get(position));
//            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
//            descriptor.close();

            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }


            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();

//            mediaPlayer.setVolume(0.5f, 0.5f);
            mediaPlayer.setLooping(false);
            seekBar.setMax(mediaPlayer.getDuration());

            mediaPlayer.start();


            String countSongs = (position+1) + "/"  + number_of_songs;
            tv_number_of_songs.setText(countSongs);


            // exprementing with the thread concept

            playMusicThread = new Thread(this);
            playMusicThread.setName("playMusicThread");
            playMusicThread.start();
//            new Thread(this).start();



        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
                mediaPlayer = null;
                //mediaPlayer = new MediaPlayer();
//                imgBtn_play_pause.setImageResource(R.drawable.ic_play);
                imgBtn_play_pause.setImageResource(R.drawable.play_icon);
                play = 0;

                //playNext();  // to play the next song
                positionOfPlayingSong = (positionOfPlayingSong + 1) % number_of_songs;
                playPositionMusic(positionOfPlayingSong, "Next");

            }
        });

    }




    // find songs

    public ArrayList<File> findSongs(File root) {

        ArrayList<File> al_songs = new ArrayList<>();

        File[] files = root.listFiles();

        for (File singleFile : files) {

            if (singleFile.isDirectory() && !singleFile.getName().equals("MIUI")) {
                al_songs.addAll(findSongs(singleFile));
            } else {
                if (singleFile.getName().endsWith(".mp3")) {
                    al_songs.add(singleFile);
                }
            }
        }

        return al_songs;
    }


    // RunTime Permission


    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            ) {

                Log.v("Permission Tag : ", "Permission is granted");
                return true;
            } else {

                Log.v("Permission Tag", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Permission Tag : ", "Permission is granted");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("Permission Tag : ", "Permission: " + permissions[0] + "was " + grantResults[0]);
            Log.v("Permission Tag : ", "Permission: " + permissions[1] + "was " + grantResults[1]);

            //resume tasks needing this permission
        }
    }

}


