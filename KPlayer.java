/**
 * Programmer: Kevin Kusi
 * Dedicated to: the client, Brian DePaz
 */

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.*;
import java.util.List;

import java.util.ArrayList;

final public class KPlayer extends Application
{
    final private ArrayList<File> myFileList = new ArrayList<>();
    final private ObservableList<String> playList = FXCollections.observableArrayList();
    final private ArrayList<MediaPlayer> mediaPlayer = new ArrayList<>();
    private BorderPane mainScreen;
    private Stage window;
    private int i = 0;
    private boolean a = true;
    final private ListView<String> listView = new ListView<>();


    protected static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException
    {

        window = primaryStage;

        window.setTitle("KPlayer");

        mainScreen = new BorderPane();

        final Menu fileMenu = new Menu("File");
        final MenuItem fileOpener = new MenuItem("Open");
        final FileChooser openMusicTracks = new FileChooser();
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MP3 file(*.mp3)", " *.mp3"); //forces user to only be able to input MP3 files
        openMusicTracks.getExtensionFilters().add(extFilter);

        fileOpener.setOnAction(e ->
        {
                    a = true;
                    while (a)
                    {
                        try
                        {
                            myFileList.add(openMusicTracks.showOpenDialog(window));
                            for (File currentFile: myFileList) //enhanced for loop used to stop playlist from constantly repeating index 0
                            {
								if(currentFile != null){
                                playList.add(currentFile.getName()); //gets name so that the playlist can be displayed to user later on
                                mediaPlayer.add(Controls.getMediaPlayer(Controls.getMedia(currentFile))); //adds the actual logical file to the mediaPlayer list so that it can be played
                                System.out.println(playList.toString());
                                
								}
								a = false; //stops while loop so that the user can play the music they just opened or open more songs
								break;//breaks out of for loop to stop repeated file names in the playlist
                            }
                            i++;
                        }
                        catch (NullPointerException e1) //catches nullpointers due to the user closing out of the window
                        {
                            Playlist.errorHandling("Critical Failure", e1);
                            myFileList.remove(i); //removal of the index from the 3 lists prevents issues later on as a result of the nullpointer
                            playList.remove(i);
                            mediaPlayer.remove(i);
                            a = false;
                        }
                    }
                }
        );


        final MenuItem makePlaylist = new MenuItem("Create Playlist");
        makePlaylist.setOnAction(e -> Playlist.createPlaylist(myFileList)); //sets the Create Playlist button to the createPlaylist method. Uses lambda notation in order to make code look cleaner.


        final MenuItem exitProgram = new MenuItem("Exit");
        exitProgram.setOnAction(e -> System.exit(0));//sets the exit button for when users want to leave the program.

        fileMenu.getItems().addAll(fileOpener, makePlaylist, exitProgram);  //binds these buttons to a reserved menu


        final MenuBar onlyOne = new MenuBar(fileMenu);
        mainScreen.setTop(onlyOne);
        openMusicTracks.setTitle("Navigate to MP3 File");



        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); // Listview allows user to physically see what is in the observable list created above
        listView.setItems(playList); // listview binded to the observable list


        final VBox Playlist = new VBox();
        Playlist.getChildren().addAll(listView);
        mainScreen.setCenter(Playlist);


        final Button playButton = new Button("Play");
        playButton.setOnAction(e ->//lambda notation makes code much sleeker, used throughout code
                {
                    final int b = listView.getSelectionModel().getSelectedIndex();  //indicates the index of the file being clicked on
                    Controls.playMe(mediaPlayer, b); //sets the playbutton to the playMe method so that the mediaplayer is played
                }
        );

        final Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(e ->
                {
                    if(!mediaPlayer.isEmpty())
                    {
                        final int b = listView.getSelectionModel().getSelectedIndex(); //indicates the index of the file being clicked on
                        Controls.pauseMe(mediaPlayer.get(b)); //sets the pausebutton to the pauseMe method so that the mediaplayer is paused
                    }
                }

        );

        final Button stopButton = new Button("Stop");
        stopButton.setOnAction(e ->
                {
                    if(!mediaPlayer.isEmpty())
                    {
                        final int b = listView.getSelectionModel().getSelectedIndex();  //indicates the index of the file being clicked on
                        Controls.stopMe(mediaPlayer.get(b));
                    }
                }
        );

        final Button removeButton = new Button("Remove");
        removeButton.setOnAction(e ->
                {
                    if(!mediaPlayer.isEmpty())
                    {
                        final int b = listView.getSelectionModel().getSelectedIndex();   //indicates the index of the file being clicked on
                        Controls.removePlaylist(mediaPlayer, playList, myFileList, b);
						i--;
                    }
                }
        );

        final Button skip = new Button("Skip");
        skip.setOnAction(e ->
                {
                    if(!mediaPlayer.isEmpty())
                    {
                        final int b = listView.getSelectionModel().getSelectedIndex();  //indicates the index of the file being clicked on
                        Controls.skipMe(mediaPlayer, b, skip);
                    }
                }
        );

        final Slider volumeSlider = new Slider(0.0, 100.0, 100.0);
        volumeSlider.setPrefWidth(70);
        volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
        volumeSlider.setMinWidth(30);

        volumeSlider.valueProperty().addListener(new InvalidationListener()  //listener makes it so that the code can constantly refresh based on mouse changes
        {
            public void invalidated(Observable ov)
            {
                if (volumeSlider.isValueChanging() && !mediaPlayer.isEmpty())
                {
                    final int b = listView.getSelectionModel().getSelectedIndex();    //indicates the index of the file being clicked on
                    mediaPlayer.get(b).setVolume(volumeSlider.getValue() / 100.0);  //changes volume with respect to the song being clicked on
                }
            }
        }
        );

        final HBox controlButtons = new HBox();
        controlButtons.getChildren().addAll(playButton, pauseButton, stopButton, skip, removeButton, volumeSlider);

    mainScreen.setBottom(controlButtons);

    final Scene scene = new Scene(mainScreen, 600, 250);
    window.setScene(scene);

    window.show();
    }
}


final class Controls { //declared as final as the class has no subclasses
    private static Media media;
    private static MediaPlayer mediaPlayer;

    final protected static Media getMedia(final File F)//method returns media by accepting a file.
    {
        media = new Media(new File(F.getAbsolutePath().replace("\\", "/")).toURI().toString());
        return media;
    }
    final protected static MediaPlayer getMediaPlayer(final Media M)//method returns media by accepting a file.
    {
        mediaPlayer = new MediaPlayer(M);
        return mediaPlayer;
    }

    final protected static void playMe(final ArrayList<MediaPlayer> MEDIAPLAYER, final int A) //plays a mediaplayer by accepting a mediaplayer. 
    {
        if(!(MEDIAPLAYER.isEmpty())) // check to see if the arraylist is empty so that there are no exceptions called
        {
        MEDIAPLAYER.get(A).play();
        }
    }
    final protected static void pauseMe(final MediaPlayer mediaPlayer)//pauses a mediaplayer by accepting a mediaplayer.
    {
        if(!(mediaPlayer == null)) //null check so that there are no exceptions called
        {
            mediaPlayer.pause();
        }
    }
    final protected static void stopMe(final MediaPlayer mediaPlayer)//plays a mediaplayer by accepting a mediaplayer.
    {
        if(!(mediaPlayer == null)) //null check so that there are no exceptions called
        {
            mediaPlayer.stop();
        }
    }
    final protected static void skipMe(final ArrayList<MediaPlayer> mediaPlayer, int a, Button q)//skips the currently playing mediaplayer by accepting a mediaplayer arraylist. 
    {
        if (!(mediaPlayer.isEmpty())) //doesn't allow the method to be called if the arraylist is empty
        {
            try
            {
                mediaPlayer.get(a).stop();
                a++;
                mediaPlayer.get(a).play();
            }
            catch (IndexOutOfBoundsException e) //will catch if the chosen item is the last in the playlist
            {
                Playlist.errorHandling("Skip failed", e, q); //popup menu is called to tell the user what went wrong
            }
        }
    }
    final protected static void removePlaylist(ArrayList<MediaPlayer> mediaPlayer, ObservableList<String> observableList, ArrayList<File> fileArrayList, int g)//removes the element
    {
        mediaPlayer.get(g).stop(); //stops the file so that it does not continue to play after being removed
        mediaPlayer.remove(g);
        observableList.remove(g);
        fileArrayList.remove(g);
    }
}

final class Playlist //class declared as final as there are no subclasses that will inherit from it
{
    final private static Stage window = new Stage();
    private static String s;

    final private static void SaveFile(String content, File file) // declared as private in order to encapsulate and protect it from modification from other classes.
    {
        try
        {
            final FileWriter fileWriter = new FileWriter(file);
            final PrintWriter printWriter = new PrintWriter(fileWriter); //creation of file and printerwriter objects allows for data such as string literals to be written to simple text files

            printWriter.println(content); //this method writes the actual data to the file
            fileWriter.close();
        }
        catch (IOException ex)
        {
            System.exit(1); //IOException is handled by closing the program due to extreme circumstances of the system
        }
    }
    final protected static void createPlaylist(List<File> a) //method uses the list of files to actually write the playlist. declared as protected
    {
        final BorderPane borderPane = new BorderPane();
        final Scene scene = new Scene(borderPane);
        final FileChooser fileChooser = new FileChooser(); //instantiation of 3 objects required for the creation of a new playlist

        window.setScene(scene);

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("KUS file(*.kus)" ," *.kus"); //restricts user to only be able to input MP3 files
        fileChooser.getExtensionFilters().add(extFilter); //binds the extension filter to the actual filechooser window that will open

        final File stragL = fileChooser.showSaveDialog(window.getScene().getWindow()); //allows user to save the .kus file anywhere in their directories
        if(stragL != null)
        {
            for(int i = 0; i < a.size(); i++)  //for loop removes the technical parts of the arraylist such as brackets and comma
            {
                s = a.toString();
                s = s.replace("]", "");
                s = s.replace("[", "");
                s = s.replace(",", "\n"); //replaces commas with an escape sequence in order to make it easier on the client to navigate to that file
            }

            SaveFile(s, stragL); // writes to the .kus file
        }
    }

    final protected static void errorHandling(String title, Exception a) //general error handle message that pops up when an extreme case occurs
    {
        final Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL); //makes it so that the alert box cannot be overriden by clicks to the main screen
        window.setTitle(title);
        window.setMinWidth(100);
        window.setMinHeight(100);

        final Label label = new Label();
        final HBox displayLabel = new HBox(10);
        final Button tryAgain = new Button("OK");

        if(a instanceof NullPointerException) //handles nullpointers by shutting the program down
        {
            label.setText("Please try again");
            tryAgain.setOnAction(e -> window.close());
        }
        else if((a instanceof IndexOutOfBoundsException) && title.equals("Index error")) // handles out of bounds exceptions by telling the user to retry
        {
            label.setText("Please reenter file chosen.");
            tryAgain.setOnAction(e -> window.close());
        }
        else
        {
            label.setText("Error occurred. Try again.");
            tryAgain.setOnAction(e -> window.close());
        }
        displayLabel.getChildren().addAll(label, tryAgain);

        final Scene scene = new Scene(displayLabel);

        window.setScene(scene);
        window.show();
    }
    final protected static void errorHandling(String title, Exception a, Button b)//
    {
        final Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL); //makes it so that the alert box cannot be overriden by clicks to the main screen
        window.setTitle(title);
        window.setMinWidth(100);
        window.setMinHeight(100);

        final Label label = new Label();
        final HBox displayLabel = new HBox(10);
        final Button tryAgain = new Button("OK");
        if((a instanceof IndexOutOfBoundsException) && b.getText().equals("Skip"))
        {
            label.setText("You can't skip, there are no files afterwards!");
            tryAgain.setOnAction(e -> window.close());
        }
        displayLabel.getChildren().addAll(label, tryAgain);

        final Scene scene = new Scene(displayLabel);

        window.setScene(scene);
        window.show();
    }
}

