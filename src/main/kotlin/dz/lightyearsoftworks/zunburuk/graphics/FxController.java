package dz.lightyearsoftworks.zunburuk.graphics;

import dz.lightyearsoftworks.zunburuk.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static dz.lightyearsoftworks.zunburuk.graphics.GraphicsConstants.*;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;


public class FxController implements Initializable {
    public ComboBox<String> oscillationTypeComboBox;
    public TextField gravityInputField;
    public TextField lengthInputField;
    public TextField maxAngleInputField;
    public TextField springConstInputField;
    public TextField massInputField;
    public TextField maxDisplacementInputField;
    public Canvas mainCanvas;
    public ImageView imgEqn;
    public Button playButton;
    public TextField dampingFactorInputField;
    public TextField freq1InputField;
    public TextField freq2InputField;
    public TextField phaseInputField;
    public Canvas secondaryCanvas;
    public SplitPane canvasesPane;
    public Slider freq1Slider;
    public Slider freq2Slider;
    public Slider phaseSlider;
    private GraphicsContext picassoThePainter;
    private Timeline currentAnimation;
    private final ArrayList<Control> availableControls = new ArrayList<>();
    private GraphicsContext rembrandtTheRevered;
    private final float samplingRate = 44100.0F;
    private final int numberOfSamples = 10 * (int)samplingRate;
    private Clip systemAudioClip;


    public void onPlayButtonPress(ActionEvent actionEvent) {
        for (Control t: availableControls) {
            if (t instanceof TextField && ((TextField) t).getText().equals("")) {
                return;
            }
        }

        DifferentialSolver currentSystem1, currentSystem2;
        GraphPlot graph;
        EventHandler<ActionEvent> simulationSteppingHandler;
        double maxAngle, gravity, tetherLength, maxDisplacement, springConst, mass, dampingFactor, freq1, freq2, phi;

        if (currentAnimation != null)   {
            currentAnimation.stop();
        }
        if (systemAudioClip != null)    {
            systemAudioClip.stop();
        }

        switch (oscillationTypeComboBox.getValue()) {
            case ("Simple Pendulum"):
                try {
                    maxAngle = Double.parseDouble(maxAngleInputField.getText()) * (Math.PI / 180.0);
                    gravity = Double.parseDouble(gravityInputField.getText());
                    tetherLength = Double.parseDouble(lengthInputField.getText());
                } catch (NumberFormatException e)   {
                    throw new RuntimeException("fix your regex, 7mar");
                }

                if (tetherLength == 0.0)  {
                    lengthInputField.clear();
                    lengthInputField.setPromptText("Tether length cannot be zero.");
                    return;
                }

                currentSystem1 = new DifferentialSolver(DifferentialEquationType.ORDER2_PENDULUM,
                        new EquationParameters(maxAngle,
                                0,
                                gravity / tetherLength,
                                0, 0),
                        0.0, FRAME_TIME / 100);
                graph = new GraphPlot(rembrandtTheRevered, false, 20, 8);
                simulationSteppingHandler = event -> {
                    //skip through 100 datapoints to sync up animation timestep and simulation timestep
                    //anim timestep is 1/60, sim timestep is 1/6000
                    ODEDataPoint dp = currentSystem1.stepSimByNSteps(100);
                    clearCanvas(mainCanvas);
                    clearCanvas(secondaryCanvas);
                    redrawPendulum(dp, tetherLength);
                    redrawBase();
                    graph.drawNextFrame(dp);
                };
                break;
            case ("Mass on a Vertical Spring"):
                try {
                    maxDisplacement = Double.parseDouble(maxDisplacementInputField.getText());
                    springConst = Double.parseDouble(springConstInputField.getText());
                    mass = Double.parseDouble(massInputField.getText());
                } catch (NumberFormatException e)   {
                    throw new RuntimeException("fix your regex, 7mar");
                }

                if (mass == 0.0)  {
                    massInputField.clear();
                    massInputField.setPromptText("Mass cannot be zero.");
                    return;
                }

                currentSystem1 = new DifferentialSolver(DifferentialEquationType.ORDER2_UNDAMPED,
                        new EquationParameters(maxDisplacement,
                                0,
                                springConst / mass,
                                0, 0),
                        0.0, FRAME_TIME / 100);
                graph = new GraphPlot(rembrandtTheRevered, false, 20, 8);

                simulationSteppingHandler = event -> {
                    ODEDataPoint dp = currentSystem1.stepSimByNSteps(100);
                    clearCanvas(mainCanvas);
                    clearCanvas(secondaryCanvas);
                    redrawSpringMass(dp, maxDisplacement);
                    redrawBase();
                    graph.drawNextFrame(dp);
                };
                break;
            case ("Damped Mass on a Vertical Spring"):
                try {
                    maxDisplacement = Double.parseDouble(maxDisplacementInputField.getText());
                    springConst = Double.parseDouble(springConstInputField.getText());
                    mass = Double.parseDouble(massInputField.getText());
                    dampingFactor = Double.parseDouble(dampingFactorInputField.getText());
                } catch (NumberFormatException e)   {
                    throw new RuntimeException("fix your regex, 7mar");
                }

                if (mass == 0.0)  {
                    massInputField.clear();
                    massInputField.setPromptText("Mass cannot be zero.");
                    return;
                }

                currentSystem1 = new DifferentialSolver(DifferentialEquationType.ORDER2_DAMPED,
                        new EquationParameters(maxDisplacement,
                                0,
                                springConst / mass,
                                dampingFactor / mass, 0),
                        0.0, FRAME_TIME / 100);
                graph = new GraphPlot(rembrandtTheRevered, false, 20, 8);
                simulationSteppingHandler = event -> {
                    ODEDataPoint dp = currentSystem1.stepSimByNSteps(100);
                    clearCanvas(mainCanvas);
                    clearCanvas(secondaryCanvas);
                    redrawSpringMass(dp, maxDisplacement);
                    redrawBase();
                    graph.drawNextFrame(dp);
                };
            break;
            case ("Beats demo"):
                try {
                    freq1 = Double.parseDouble(freq1InputField.getText());
                    freq2 = Double.parseDouble(freq2InputField.getText());
                    phi = Double.parseDouble(phaseInputField.getText()) * (Math.PI / 180.0);
                } catch (NumberFormatException e)   {
                    throw new RuntimeException("fix your regex, 7mar");
                }
                double angFreq1 = 2.0 * Math.PI * freq1;
                double angFreq2 = 2.0 * Math.PI * freq2;
                double maxAudioAmplitude = 5.0;

                ArrayList<ODEDataPoint> function = new ArrayList<>();
                AtomicReference<Double> Xi = new AtomicReference<>(0.0);
                for (int i = 0; i < numberOfSamples; i++) {
                    ODEDataPoint dp = new ODEDataPoint(Xi.get(), cos(angFreq1 * Xi.get()) + cos(angFreq2 * Xi.get() + phi));
                    Xi.updateAndGet(v -> (double) (v + 1.0 / samplingRate));
                    function.add(dp);
                }
                try {
                    systemAudioClip = playAudio(function, 2 * maxAudioAmplitude);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //TODO might be worth having each oscillator with an independent amplitude?
                graph = new GraphPlot(picassoThePainter, false, 4, 8);
                AtomicReference<Double> finalXi = new AtomicReference<>(0.0);
                simulationSteppingHandler = event -> {
                    ODEDataPoint dp = new ODEDataPoint(finalXi.get(), cos(angFreq1 * finalXi.get()) + cos(angFreq2 * finalXi.get() + phi));
                    finalXi.updateAndGet(v -> (double) (v + FRAME_TIME));
                    clearCanvas(mainCanvas);
                    graph.drawNextFrame(dp);
                };
                break;

            //TODO frequencies higher than 10Hz produce nonsense figures, change to frequency slider?
            case ("Lissajous Figures"):
                try {
                    freq1 = Double.parseDouble(freq1InputField.getText());
                    freq2 = Double.parseDouble(freq2InputField.getText());
                    phi = Double.parseDouble(phaseInputField.getText()) * (Math.PI / 180.0);
                } catch (NumberFormatException e)   {
                    throw new RuntimeException("fix your regex, 7mar");
                }
                double angVel1 = 1;
                double angVel2 = 1 * freq2 / freq1;

                double amplitude = 15.0;

                currentSystem1 = new DifferentialSolver(DifferentialEquationType.ORDER2_UNDAMPED,
                        new EquationParameters(amplitude,
                                0,
                                angVel1 * angVel1,
                                0.0, 0),
                        0.0, FRAME_TIME);
                currentSystem2 = new DifferentialSolver(DifferentialEquationType.ORDER2_UNDAMPED,
                        new EquationParameters(amplitude * cos(phi),
                                -amplitude * angVel2 * sin(phi),
                                angVel2 * angVel2,
                                0.0, 0),
                        0.0, FRAME_TIME / 10);

                ArrayList<ODEDataPoint> func = new ArrayList<>();
                int graphSamples = (int)(100.0 / (angVel1 * FRAME_TIME));
                //double Zi = 0.0;
                for (int i = 0; i < graphSamples; i++) {
                    ODEDataPoint dp1 = currentSystem1.nextDataPoint();
                    ODEDataPoint dp2 = currentSystem2.stepSimByNSteps(10);
                    ODEDataPoint dp = new ODEDataPoint(dp1.getY(), dp2.getY());


                    //ODEDataPoint dp = new ODEDataPoint(amplitude * cos(angVel1 * Zi), amplitude * cos(angVel2 * Zi + phi));
                    func.add(dp);
                    //Zi += FRAME_TIME;
                }

                graph = new GraphPlot(picassoThePainter, true, 20, 8);
                simulationSteppingHandler = event -> {
                    clearCanvas(mainCanvas);
                    graph.drawGraph(func);
                };
                break;
            default:
                simulationSteppingHandler = null;
        }

        if (simulationSteppingHandler != null) {
            currentAnimation = new Timeline(new KeyFrame(Duration.seconds(FRAME_TIME), simulationSteppingHandler));
            currentAnimation.setCycleCount(Timeline.INDEFINITE);
            currentAnimation.play();
        }
    }

    public void sliderTest(MouseEvent actionEvent) {
        System.out.println(currentTimeMillis());
    }

    private Clip playAudio(ArrayList<ODEDataPoint> function, double maxVolume) throws LineUnavailableException, IOException {
        AudioInputStream asmr = new AudioInputStream(new FunctionalInputStream(function, maxVolume),
                new AudioFormat(samplingRate, 16, 1, true, false),
                numberOfSamples);
        Clip clang = AudioSystem.getClip();
        try {
            clang.open(asmr);
            clang.loop(Clip.LOOP_CONTINUOUSLY);
            clang.start();
        } catch (LineUnavailableException ignored)    {
            //might be a problem if you spam the play button with an autoclicker? didnt break for me ¯\_(ツ)_/¯
        }
        return clang;
    }

    private void redrawSpringMass(ODEDataPoint dp, double maxDisplacement) {
        double width = mainCanvas.getWidth(); //double height = mainCanvas.getHeight();
        picassoThePainter.setLineWidth(3.0);
        double baseX = width/2.0; double baseY = 60;
        int springSegs = (int) (maxDisplacement * 4.0);
        //theta is the angle going counter-clockwise from the y-axis to the spring segment
        double theta = (Math.PI / 6.0) * (1 - (dp.getY() / maxDisplacement)) + (Math.PI / 8.0);
        //draw the small segment connecting the base and the spring independently
        picassoThePainter.strokeLine(baseX, baseY, baseX + 20 * Math.sin(theta), baseY + 20 * cos(theta));
        baseX += 20 * Math.sin(theta);
        baseY += 20 * cos(theta);
        theta = -theta;
        //the rest of the spring segments
        for (int i = 0; i < springSegs; i++)   {
            picassoThePainter.strokeLine(baseX, baseY, baseX + 40 * Math.sin(theta), baseY + 40 * cos(theta));
            baseX += 40 * Math.sin(theta);
            baseY += 40 * cos(theta);
            theta = -theta; //reverse x-direction of the next spring segment
        }
        //draw the small segment connecting the spring and the block independently
        picassoThePainter.strokeLine(baseX, baseY, baseX + 20 * Math.sin(theta), baseY + 20 * cos(theta));
        baseX += 20 * Math.sin(theta);
        baseY += 20 * cos(theta);
        //draw the mass block
        picassoThePainter.setLineWidth(5.0);
        picassoThePainter.setFill(Color.DARKCYAN);
        picassoThePainter.fillRect(baseX - 20, baseY, 40, 40);
        picassoThePainter.strokeRect(baseX - 20, baseY, 40, 40);
    }

    private void redrawPendulum(ODEDataPoint dp, double wireLength) {
        double width = mainCanvas.getWidth();
        picassoThePainter.setLineWidth(3.0);
        //datapoints give the value of the angle going counterclockwise from the y-axis to the tether
        double currentX = width/2.0 + 100 * wireLength * Math.sin(dp.getY());
        double currentY = 60 + 100 * wireLength * cos(dp.getY());
        picassoThePainter.strokeLine(width/2.0,
                60,
                currentX,
                currentY);
        //drawing the pendulum bob
        picassoThePainter.setFill(Color.INDIANRED);
        picassoThePainter.fillOval(currentX - 15.0,currentY - 15.0,30.0,30.0);
        picassoThePainter.strokeOval(currentX - 15.0,currentY - 15.0,30.0,30.0);
        picassoThePainter.setLineWidth(1.0);
    }

    private void redrawBase() {
        //draws a nice lil rectangular base
        double width = mainCanvas.getWidth(); double height = mainCanvas.getHeight();
        picassoThePainter.setFill(Color.GRAY);
        picassoThePainter.fillRect(width/2.0 - 50.0, 40.0, 100.0, 20.0);
        picassoThePainter.setLineWidth(2.0);
        picassoThePainter.setStroke(Color.BLACK);
        picassoThePainter.strokeRect(width/2.0 - 50.0, 40.0, 100.0, 20.0);
        picassoThePainter.setLineWidth(1.0);
    }

    private void clearCanvas(Canvas c) {
        double width = c.getWidth(); double height = c.getHeight();
        c.getGraphicsContext2D().clearRect(0, 0, width, height);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        canvasesPane.setDividerPosition(0,1.0);
        mainCanvas.widthProperty().bind(((Pane)mainCanvas.getParent()).widthProperty());
        mainCanvas.heightProperty().bind(((Pane)mainCanvas.getParent()).heightProperty());
        secondaryCanvas.widthProperty().bind(((Pane)secondaryCanvas.getParent()).widthProperty());
        secondaryCanvas.heightProperty().bind(((Pane)secondaryCanvas.getParent()).heightProperty());
        picassoThePainter = mainCanvas.getGraphicsContext2D();
        rembrandtTheRevered = secondaryCanvas.getGraphicsContext2D();
        oscillationTypeComboBox.getItems().add("Simple Pendulum");
        oscillationTypeComboBox.getItems().add("Mass on a Vertical Spring");
        oscillationTypeComboBox.getItems().add("Damped Mass on a Vertical Spring");
        oscillationTypeComboBox.getItems().add("Beats demo");
        oscillationTypeComboBox.getItems().add("Lissajous Figures");
    }

    public void onSimTypeSelection(ActionEvent actionEvent) {
        hideControls();
        switch (oscillationTypeComboBox.getValue()) {
            case ("Simple Pendulum"):
                availableControls.add(gravityInputField);
                availableControls.add(lengthInputField);
                availableControls.add(maxAngleInputField);
                canvasesPane.setDividerPosition(0,0.65);
                canvasesPane.setDisable(false);
                imgEqn.setImage(new Image(getClass().getResourceAsStream("resources/ode_pendulum.png")));
                break;
            case ("Mass on a Vertical Spring"):
                availableControls.add(springConstInputField);
                availableControls.add(massInputField);
                availableControls.add(maxDisplacementInputField);
                canvasesPane.setDividerPosition(0,0.65);
                canvasesPane.setDisable(false);
                imgEqn.setImage(new Image(getClass().getResourceAsStream("resources/ode_springmass.png")));
                break;
            case ("Damped Mass on a Vertical Spring"):
                availableControls.add(springConstInputField);
                availableControls.add(massInputField);
                availableControls.add(maxDisplacementInputField);
                availableControls.add(dampingFactorInputField);
                canvasesPane.setDividerPosition(0,0.65);
                canvasesPane.setDisable(false);
                imgEqn.setImage(new Image(getClass().getResourceAsStream("resources/ode_springmass_damped.png")));
                break;
            case ("Beats demo"):
                availableControls.add(freq1InputField);
                availableControls.add(freq2InputField);
                availableControls.add(phaseInputField);
                canvasesPane.setDividerPosition(0,1.0);
                canvasesPane.setDisable(true);
                imgEqn.setImage(new Image(getClass().getResourceAsStream("resources/beats_param.png")));
                break;
            case ("Lissajous Figures"):

                availableControls.add(freq1InputField);
                availableControls.add(freq2InputField);
                availableControls.add(phaseInputField);

                availableControls.add(freq1Slider);
                availableControls.add(freq2Slider);
                availableControls.add(phaseSlider);
                canvasesPane.setDividerPosition(0,1.0);
                canvasesPane.setDisable(true);
                imgEqn.setImage(new Image(getClass().getResourceAsStream("resources/lissajous_param.png")));
                break;
            default:
        }
        showControls();
    }

    /**Sets the imagebox, textfields and playbutton back to visible
     * These controls must be setup properly first before calling this method*/
    private void showControls() {

        imgEqn.setManaged(true);
        imgEqn.setVisible(true);

        playButton.setManaged(true);
        playButton.setVisible(true);
        for(Control t: availableControls)  {
            t.setManaged(true);
            t.setVisible(true);
        }
    }

    /**Sets the imagebox, textfields and playbutton to invisible
     * clears the individual textfields and the set of available textfields*/
    private void hideControls() {
        if (systemAudioClip != null)    {
            systemAudioClip.stop();
        }
        if (currentAnimation != null)   {
            currentAnimation.stop();
        }
        clearCanvas(mainCanvas);
        clearCanvas(secondaryCanvas);
        imgEqn.setManaged(false);
        imgEqn.setVisible(false);

        playButton.setManaged(false);
        playButton.setVisible(false);

        for (Control t: availableControls)  {
            //t.clear();
            t.setManaged(false);
            t.setVisible(false);
        }
        availableControls.clear();
    }

    /**Make sure the pattern matches a decimal number, if not it just clears the text field
     *everytime the user types an invalid pattern
     * */
    public void onInputFieldChanged(KeyEvent actionEvent) {
        TextField source = (TextField) actionEvent.getSource();
        if (!source.getText().matches("((\\d+)(\\.)?(\\d+)?){1}")) {
            source.clear();
        }
    }
}
