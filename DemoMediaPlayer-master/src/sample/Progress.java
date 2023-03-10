package sample;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class Progress
{
    /**
     * Makes so the user can both drag and click on the slider
     * The song wil then move to that location.
     * @param mp mp = mediaplayer
     * @param sliderPro = the slider for the song
     */
    public void beginTimer(MediaPlayer mp , Slider sliderPro)
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

