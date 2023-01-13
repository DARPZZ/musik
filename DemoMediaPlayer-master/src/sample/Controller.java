package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

import javafx.scene.media.*;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.util.Duration;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.net.*;
import java.sql.SQLOutput;
import java.util.*;
import java.util.Timer;

public class Controller implements Initializable
{
    @FXML
    ProgressBar Bar;
    @FXML
    private MediaView mediaV;
    @FXML
    ImageView ImageV;
    @FXML
    Button knapPlay, knapPause, knapStop, knapCreate, knapAdd, knapDelete, knapRemove, knapRename, knapChoose;
    @FXML
    ListView sangeliste, playlistview, playlistsongs;
    @FXML
    TextField searchfield, textfieldInfo, TF_PlaylistName, textfieldPlDuration;
    @FXML
    Slider sliderVolume, sliderPro;
    @FXML
    ToggleButton knapStart_Pause;


    final Timeline timeline = new Timeline();
    boolean running = true;
    private final Timeline TIMELINE = new Timeline();
    private MediaPlayer mp;
    private Media me;
    private String filepath = new File("DemoMediaPlayer-master/src/sample/media/SampleAudio_0.4mb.mp3").getAbsolutePath();
    private Playlist ActivePlaylist = new Playlist(null, 0);
    private String selectedItem;
    private int identifier; // 1 = songlist, 2 = playlist songlist
    private boolean isPlaying = false;
    private String displayInfo;
    private String userDirectoryPath;
    private double duration;
    private Image userImage;
    private int userImageCount;
    private File[] pictureList;


    /**
     * This method is invoked automatically in the beginning. Used for initializing, loading data etc.
     *
     * @param location
     * @param resources
     */

    public void initialize(URL location, ResourceBundle resources)
    {
        sliderVolume.setShowTickMarks(true);
        sliderVolume.setShowTickLabels(true);
        sliderVolume.setMajorTickUnit(25);
        textfieldInfo.setStyle("-fx-background-color: Black; -fx-text-inner-color: white");
        knapStart_Pause.setText("\u23f5/\u23f8");
        knapStop.setText("\u23f9");
        knapPlay.setText("\u23f5");
        knapStart_Pause.setVisible(false);
        // create the list of songs
        Song.createList();
        publishSong();

        // set the selection mode to single, so only one song can be selected at a time
        sangeliste.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Playlist setup
        playlistview.setItems(FXCollections.observableArrayList(Playlist.PlaylistArray()));
        playlistview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        playlistsongs.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        System.out.println(Playlist.PlaylistArray());

        sliderVolume.valueProperty().addListener(new InvalidationListener()
        {
            @Override
            public void invalidated(Observable observable)
            {
                mp.setVolume(sliderVolume.getValue() / 100);
            }
        });

    }

    /**
     * Method updates the playlist view
     */
    public void updatePlaylistView()
    {
        playlistview.setItems(FXCollections.observableArrayList(Playlist.PlaylistArray()));
    }

    public void updatePlaylistSongView(int i)
    {
        playlistsongs.setItems(FXCollections.observableArrayList(ActivePlaylist.getListPlaylist()));
        textfieldPlDuration.setText(Playlist.durationFormat(i));
    }

    public String stringFormat(String inputString)
    {
        StringBuilder sB = new StringBuilder(25);
        sB.insert(0, inputString);
        for (int i = 0; i < 25 - inputString.length(); i++) {
            sB.append(" ");
        }
        return sB.toString();
    }

    @FXML
    /**
     * Handler for the play/pause/stop button
     */
    public void handlerplay()
    {

        String endSearch = selectedItem.substring(selectedItem.indexOf(" ") + 1, selectedItem.indexOf("Artist") - 1);
        System.out.println(endSearch);

        if (userDirectoryPath == null)
        {
            loadBilleder();
        }
        else
        {
            TIMELINE.stop();
            runUserImage();
        }

        findFilePath(endSearch);

        System.out.println("Now playing: " + filepath);
        textfieldInfo.setText(displayInfo);

        me = new Media(new File(filepath).toURI().toString());
        // Create new MediaPlayer and attach the media to be played
        mp = new MediaPlayer(me);

        mediaV.setMediaPlayer(mp);

        mp.play();


        knapPlay.setVisible(false);
        knapStart_Pause.setVisible(true);
        isPlaying = true;
        beginTimer();
        mp.setOnEndOfMedia(new Runnable() {
            @Override
            public void run()
            {
                System.out.println();
                    mp.setStartTime(Duration.ZERO);
                    String endSearch = selectedItem.substring(selectedItem.indexOf(" ") + 1, selectedItem.indexOf("Artist") - 1);
                    int indexCheck = 0;
                    switch (identifier)
                    {
                        case 1:
                        {
                            ArrayList<sample.Song> songlist = new ArrayList<>(Song.getSongList());
                            //Song.getSongList() //array af song objekts
                            for (int i = 0; i < songlist.size(); i++)
                            {
                                System.out.println(songlist.get(i).getSONG_NAME());
                                String meme = songlist.get(i).getSONG_NAME();
                                if (endSearch.equals(meme))
                                {
                                    indexCheck = i + 1;
                                    break;
                                }
                            }
                            for (Song songs : Song.getSongList())
                            {
                                if (songs.getSONG_NAME().equals(songlist.get(indexCheck).getSONG_NAME())) {
                                    filepath = songs.getFILE_PATH();
                                    break;
                                }
                            }
                            me = new Media(new File(filepath).toURI().toString());
                            mp = new MediaPlayer(me);
                            mp.play();
                            break;

                        }
                        case 2://ActivePlaylist.getListPlaylist() string array
                        {
                            ArrayList<String> songlist = new ArrayList<>(ActivePlaylist.getListPlaylist());
                            for (int i = 0; i < songlist.size(); i++) {
                                String Formatted = songlist.get(i).substring(songlist.get(i).indexOf(" ") + 1, songlist.get(i).indexOf("Artist") - 1);
                                if (endSearch.equals(Formatted))
                                {
                                    indexCheck = i + 1;
                                    break;
                                }
                            }
                            for (Song songs : Song.getSongList())
                            {
                                if (songs.getSONG_NAME().equals(songlist.get(indexCheck)))
                                {
                                    filepath = songs.getFILE_PATH();
                                    break;
                                }
                            }
                            me = new Media(new File(filepath).toURI().toString());
                            mp = new MediaPlayer(me);
                            mp.play();
                            break;
                        }
                    }
            }
        });
    }


    public void handlerS_P()
    {
        if (knapStart_Pause.isSelected()) {
            mp.play();
            TIMELINE.play();

        }
        mp.pause();
        TIMELINE.stop();
        TIMELINE.getKeyFrames().clear();
    }


    public void handlerStop()
    {
        mp.stop();
        TIMELINE.stop();
        TIMELINE.getKeyFrames().clear();
        knapPlay.setVisible(true);
        knapStart_Pause.setVisible(false);
        isPlaying = false;


    }

    public void handlerSearch()
    {
        searchfield.setOnKeyPressed(handlerSearch ->
        {
            // Handle the key press event here
            KeyCode code = handlerSearch.getCode();
            if (code == KeyCode.ENTER)
            {
                String search = searchfield.getText();
                Song.searchSong(search);
                publishSong();
                // searchfield.clear();
            }
        });
    }

    public void handleClickView(MouseEvent mouseEvent)
    {
        selectedItem = (String) sangeliste.getSelectionModel().getSelectedItem();
        identifier = 1;
        knapPlay.setVisible(true);
        knapStart_Pause.setVisible(false);
        if (isPlaying == true)
        {
            mp.stop();
            isPlaying= true;
        }

    }

    public void handlerPL_Create()
    {
        String PLname = TF_PlaylistName.getText();
        Playlist ActivePlaylist = new Playlist(PLname, Playlist.createPlaylist(PLname)); // Ugly code, Creates the Playlist in SQL and the instance of the Playlist Class
        ActivePlaylist.playlistSongNameFill();
        System.out.println(Playlist.PlaylistArray());
        updatePlaylistView();


    }

    public void handlerPL_Delete()
    {
        ActivePlaylist.deletePlaylist();
        updatePlaylistView();
    }

    public void handlerPL_Rename()
    {
        System.out.println();
        String selectedPL = TF_PlaylistName.getText();
        ActivePlaylist.renamePlaylist(selectedPL);
        updatePlaylistView();

    }

    public void handlerPL_Select(MouseEvent event)
    {
        try // Java throws an error if you click on a non entry in the table, catch to ignore
        {
            String selectedPL = playlistview.getSelectionModel().getSelectedItem().toString();
            ActivePlaylist.setPlaylistName(selectedPL);
            ActivePlaylist.setPlaylistID(Playlist.getPlaylistID(selectedPL));
            int totalDur = ActivePlaylist.playlistSongNameFill();
            updatePlaylistSongView(totalDur);
        } catch (Exception e) {
            System.out.println();
        }

    }

    public void handlerPLsong_Select()
    {
        System.out.println();
        String selectedPLsong = playlistsongs.getSelectionModel().getSelectedItem().toString();
        selectedPLsong = selectedPLsong.substring(selectedPLsong.indexOf("g") + 3, selectedPLsong.indexOf("Artist") - 1);
        //Super hacky workaround string requirements in play method
        selectedItem = " " + selectedPLsong + " Artist";
        identifier = 2;
        knapPlay.setVisible(true);
        knapStart_Pause.setVisible(false);
        if (isPlaying == true)
        {mp.stop();}

    }

    public void handlerPL_add()
    {
        String endSearch = selectedItem.substring(selectedItem.indexOf(" ") + 1, selectedItem.indexOf("Artist") - 1);
        ActivePlaylist.addSongPlaylist(endSearch);
        int totaldur = ActivePlaylist.playlistSongNameFill();
        updatePlaylistSongView(totaldur);

    }

    public void handlerPL_remove()
    {
        try {
            ActivePlaylist.deleteSongPlaylist(selectedItem);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    /**
     * Allows user to set a folder with the users own images
     */
    public void handleChoose()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("image files", "png", "jpg", "jpeg", "bmp");
        chooser.setFileFilter(filter);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            userDirectoryPath = String.valueOf(chooser.getSelectedFile());
            pictureList = Pictures.listUserPictures(userDirectoryPath);
            System.out.println("User picture folder path: " + userDirectoryPath);
        }
    }

    public void loadBilleder()
    {
        Random random = new Random();
        ArrayList<String> mylist = Pictures.addPictures();
        TIMELINE.getKeyFrames().add(
                new KeyFrame(Duration.seconds(5), event ->
                {
                    final Image image = new Image(mylist.get(random.nextInt(mylist.size())));
                    System.out.println("RONALDO: SUIIIIIIIIIIII");
                    ImageV.setImage(image);
                }));
        TIMELINE.setCycleCount(Animation.INDEFINITE);
        TIMELINE.play();
    }

    public void publishSong()
    {
        ArrayList<String> songName = new ArrayList<>();
        for (Song object : Song.getSongList()) {
            String duration = Playlist.durationFormat( object.getDURATION());
            String navn = "Song: " + object.getSONG_NAME() + " Artist: " + object.getARTIST() + "Duration: " + duration;
            songName.add(navn);
        }
        ObservableList<String> songs = FXCollections.observableArrayList(songName);

        // set the items of the list view
        sangeliste.setItems(songs);
    }

    /**
     * Finds the filepath for the selected song
     * @param endSearch
     */
    public void findFilePath(String endSearch)
    {
        for (Song songs : Song.getSongList()) {
            if (songs.getSONG_NAME().equals(endSearch)) {
                filepath = songs.getFILE_PATH();
                displayInfo = songs.getARTIST() + " - " + songs.getSONG_NAME() + " - " + Playlist.durationFormat(songs.getDURATION()) + " min.";
                duration = songs.getDURATION();
            }
        }
    }

    /**
     * Displays user images and changes picture everytime it's run
     */
    public void runUserImage()
    {
        if (userImageCount == pictureList.length) {
            userImageCount = 0;

        }
        if (pictureList[userImageCount].toString().endsWith(".png") || pictureList[userImageCount].toString().endsWith(".jpg") || pictureList[userImageCount].toString().endsWith(".bmp"))
        {
            userImage = new Image((pictureList[userImageCount++]).toURI().toString());
            ImageV.setImage(userImage);
            System.out.println("Displayed user image: " + userImage);
        }
        else if (pictureList.length > userImageCount + 1)
        {
            userImage = new Image((pictureList[++userImageCount]).toURI().toString());
            userImageCount++;
            ImageV.setImage(userImage);
        }
    }

    public void beginTimer()
    {
        mp.currentTimeProperty().addListener(new ChangeListener<Duration>()
        {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue)
            {
                sliderPro.setValue(newValue.toSeconds());
            }
        });
        sliderPro.setOnMousePressed(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                mp.seek(Duration.seconds(sliderPro.getValue()));
            }
        });
        sliderPro.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                mp.seek(Duration.seconds(sliderPro.getValue()));
            }
        });
        mp.setOnReady(new Runnable()
        {
            @Override
            public void run()
            {
                Duration total = mp.getTotalDuration();
                sliderPro.setMax(total.toSeconds());
            }
        });
    }

}

