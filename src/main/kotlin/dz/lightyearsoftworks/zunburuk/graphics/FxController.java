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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static dz.lightyearsoftworks.zunburuk.graphics.GraphicsConstants.*;
import static java.lang.Math.*;
import static java.lang.System.currentTimeMillis;


public class FxController implements Initializable {
    public ComboBox<String> oscillationTypeComboBox;
    public TextField gravityInputField;
    public TextField lengthInputField;
    public TextField springConstInputField;
    public TextField massInputField;
    public TextField maxDisplacementInputField;
    public Canvas mainCanvas;
    public ImageView imgEqn;
    public Button playButton;
    public TextField dampingFactorInputField;
    public TextField freq1InputField;
    public TextField freq2InputField;
    public Canvas secondaryCanvas;
    public SplitPane canvasesPane;
    public Slider freq1Slider;
    public Slider freq2Slider;
    public Slider phaseSlider;
    public Label freq1Label;
    public Label freq2Label;
    public Label phaseLabel;
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
                    maxAngle = phaseSlider.getValue() * (Math.PI / 180.0);
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
                    double width = mainCanvas.getWidth();
                    double height = mainCanvas.getHeight();

                    ODEDataPoint dp = currentSystem1.stepSimByNSteps(100);
                    clearCanvas(mainCanvas);
                    clearCanvas(secondaryCanvas);
                    redrawPendulum(dp, tetherLength, (width + BASE_WIDTH) / 2.0 - BASE_WIDTH / 2.0, height / 3.0 - BASE_HEIGHT / 4.0);
                    redrawBase(width / 2.0, height / 3.0);
                    graph.drawNextFrame(dp);
                };
                playButton.setText("Update");
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
                    double width = mainCanvas.getWidth();
                    double height = mainCanvas.getHeight();

                    ODEDataPoint dp = currentSystem1.stepSimByNSteps(100);
                    clearCanvas(mainCanvas);
                    clearCanvas(secondaryCanvas);
                    redrawSpringMass(dp, maxDisplacement, width / 2.0, height / 10.0 + BASE_HEIGHT / 2.0);
                    redrawBase(width / 2.0, height / 10.0);
                    graph.drawNextFrame(dp);
                };
                playButton.setText("Update");
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
                    double width = mainCanvas.getWidth();
                    double height = mainCanvas.getHeight();

                    ODEDataPoint dp = currentSystem1.stepSimByNSteps(100);
                    clearCanvas(mainCanvas);
                    clearCanvas(secondaryCanvas);
                    redrawSpringMass(dp, maxDisplacement, width / 2.0, height / 10.0 + BASE_HEIGHT / 2.0);
                    redrawBase(width / 2.0, height / 10.0);
                    graph.drawNextFrame(dp);
                };
                playButton.setText("Update");
                break;
            case ("Audio beats (sinewave superposition)"):
                try {
                    freq1 = Double.parseDouble(freq1InputField.getText());
                    freq2 = Double.parseDouble(freq2InputField.getText());
                    phi = 0.0;
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

                graph = new GraphPlot(picassoThePainter, false, 4, 8);
                AtomicReference<Double> finalXi = new AtomicReference<>(0.0);
                simulationSteppingHandler = event -> {
                    ODEDataPoint dp = new ODEDataPoint(finalXi.get(), cos(angFreq1 * finalXi.get()) + cos(angFreq2 * finalXi.get() + phi));
                    finalXi.updateAndGet(v -> (double) (v + FRAME_TIME));
                    clearCanvas(mainCanvas);
                    graph.drawNextFrame(dp);
                };
                playButton.setText("Update");
                break;

            case ("Lissajous Figures"):
                ArrayList<ODEDataPoint> func = new ArrayList<>();
                int graphSamples = (int)(100.0 / (FRAME_TIME));
                graph = new GraphPlot(picassoThePainter, true, 20, 8);
                AtomicReference<Double> lissaFreq1 = new AtomicReference<>((double) 0);
                AtomicReference<Double> lissaFreq2 = new AtomicReference<>((double) 0);
                AtomicReference<Double> lissaPhi = new AtomicReference<>((double) 0);

                simulationSteppingHandler = event -> {
                    //double deltaT = System.nanoTime();
                    if (lissaPhi.get() != (int) phaseSlider.getValue() * PI / 180 ||
                            lissaFreq1.get() != (int) freq1Slider.getValue() ||
                                lissaFreq2.get() != (int) freq2Slider.getValue())   {
                        //only executes if any slider value has actually changed (only care for integer slider values)
                        lissaFreq1.set((double)((int) freq1Slider.getValue()));
                        lissaFreq2.set((double)((int) freq2Slider.getValue()));
                        lissaPhi.set((int) phaseSlider.getValue() * PI / 180); //already a double

                        func.clear();

                        double angVel1 = 1;
                        double angVel2 = lissaFreq2.get() / lissaFreq1.get();


                        DifferentialSolver lissajous1 = new DifferentialSolver(DifferentialEquationType.ORDER2_UNDAMPED,
                                new EquationParameters(1.0,
                                        0,
                                        angVel1 * angVel1,
                                        0.0, 0),
                                0.0, FRAME_TIME);
                        DifferentialSolver lissajous2 = new DifferentialSolver(DifferentialEquationType.ORDER2_UNDAMPED,
                                new EquationParameters(cos(lissaPhi.get()),
                                        -angVel2 * sin(lissaPhi.get()),
                                        angVel2 * angVel2,
                                        0.0, 0),
                                0.0, FRAME_TIME / 5.0);
                        for (int i = 0; i < graphSamples; i++) {
                            ODEDataPoint dp1 = lissajous1.nextDataPoint();
                            ODEDataPoint dp2 = lissajous2.stepSimByNSteps(5);
                            ODEDataPoint dp = new ODEDataPoint(dp1.getY(), dp2.getY());
                            func.add(dp);
                        }
                    }
                    clearCanvas(mainCanvas);
                    graph.drawGraph(func);
                    //deltaT = System.nanoTime() - deltaT;
                    //System.out.println(deltaT / 1000.0);
                };
                playButton.setVisible(false);
                playButton.setManaged(false);
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

    private void redrawSpringMass(ODEDataPoint dp, double maxDisplacement, double xOffset, double yOffset) {
        picassoThePainter.setLineWidth(3.0);
        double baseX = xOffset; double baseY = yOffset;
        int springSegs = (int) (maxDisplacement * SPRING_SEGMENT_MULTIPLIER);
        //theta is the angle going counter-clockwise from the y-axis to the spring segment
        double theta = SPRING_SEGMENT_ANGLE_BIAS * (1 - (dp.getY() / maxDisplacement)) + SPRING_SEGMENT_MIN_ANGLE;
        //draw the small segment connecting the base and the spring independently, it is half as long as the normal segments
        picassoThePainter.strokeLine(baseX, baseY, baseX + 0.5 * SPRING_SEGMENT_LENGTH * sin(theta), baseY + 0.5 * SPRING_SEGMENT_LENGTH * cos(theta));
        baseX += 0.5 * SPRING_SEGMENT_LENGTH * Math.sin(theta);
        baseY += 0.5 * SPRING_SEGMENT_LENGTH * cos(theta);
        theta = -theta;
        //the rest of the spring segments
        for (int i = 0; i < springSegs; i++)   {
            picassoThePainter.strokeLine(baseX, baseY, baseX + SPRING_SEGMENT_LENGTH * sin(theta), baseY + SPRING_SEGMENT_LENGTH * cos(theta));
            baseX += SPRING_SEGMENT_LENGTH * Math.sin(theta);
            baseY += SPRING_SEGMENT_LENGTH * cos(theta);
            theta = -theta; //reverse x-direction of the next spring segment
        }
        //draw the small segment connecting the spring and the block independently, it is half as long as the normal segments
        picassoThePainter.strokeLine(baseX, baseY, baseX + 0.5 * SPRING_SEGMENT_LENGTH * sin(theta), baseY + 0.5 * SPRING_SEGMENT_LENGTH * cos(theta));
        baseX += 0.5 * SPRING_SEGMENT_LENGTH * Math.sin(theta);
        baseY += 0.5 * SPRING_SEGMENT_LENGTH * cos(theta);
        //draw the mass block
        picassoThePainter.setLineWidth(5.0);
        picassoThePainter.setFill(Color.DARKCYAN);
        picassoThePainter.fillRect(baseX - 0.5 * SPRING_MASS_BOX_LENGTH, baseY, SPRING_MASS_BOX_LENGTH, SPRING_MASS_BOX_LENGTH);
        picassoThePainter.strokeRect(baseX - 0.5 * SPRING_MASS_BOX_LENGTH, baseY, SPRING_MASS_BOX_LENGTH, SPRING_MASS_BOX_LENGTH);
    }

    private void redrawPendulum(ODEDataPoint dp, double wireLength, double xOffset, double yOffset) {
        picassoThePainter.setLineWidth(3.0);
        //datapoints give the value of the angle going counterclockwise from the y-axis to the tether
        double currentX = xOffset + PENDULUM_TETHER_SCALE * wireLength * sin(dp.getY());
        double currentY = yOffset + PENDULUM_TETHER_SCALE * wireLength * cos(dp.getY());
        picassoThePainter.strokeLine(xOffset,
                yOffset,
                currentX,
                currentY);
        //drawing the pendulum bob
        picassoThePainter.setFill(Color.INDIANRED);
        picassoThePainter.fillOval(currentX - PENDULUM_BOB_BOUNDINGBOX_LENGTH / 2.0,currentY - PENDULUM_BOB_BOUNDINGBOX_LENGTH / 2.0, PENDULUM_BOB_BOUNDINGBOX_LENGTH, PENDULUM_BOB_BOUNDINGBOX_LENGTH);
        picassoThePainter.strokeOval(currentX - PENDULUM_BOB_BOUNDINGBOX_LENGTH / 2.0,currentY - PENDULUM_BOB_BOUNDINGBOX_LENGTH / 2.0,PENDULUM_BOB_BOUNDINGBOX_LENGTH, PENDULUM_BOB_BOUNDINGBOX_LENGTH);
        picassoThePainter.setLineWidth(1.0);
    }

    private void redrawBase(double xOffset, double yOffset) {
        //draws a nice lil rectangular base
        picassoThePainter.setFill(Color.GRAY);
        picassoThePainter.fillRect(xOffset - BASE_WIDTH / 2.0, yOffset - BASE_HEIGHT / 2.0, BASE_WIDTH, BASE_HEIGHT);
        picassoThePainter.setLineWidth(2.0);
        picassoThePainter.setStroke(Color.BLACK);
        picassoThePainter.strokeRect(xOffset - BASE_WIDTH / 2.0, yOffset - BASE_HEIGHT / 2.0, BASE_WIDTH, BASE_HEIGHT);
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
        oscillationTypeComboBox.getItems().add("Audio beats (sinewave superposition)");
        oscillationTypeComboBox.getItems().add("Lissajous Figures");

    }

    public void onSimTypeSelection(ActionEvent actionEvent) {
        hideControls();
        switch (oscillationTypeComboBox.getValue()) {
            case ("Simple Pendulum"):
                phaseLabel.setText("Initial angular displacement in degrees:");

                availableControls.add(gravityInputField);
                availableControls.add(lengthInputField);
                availableControls.add(phaseLabel);
                availableControls.add(phaseSlider);

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
            case ("Audio beats (sinewave superposition)"):
                availableControls.add(freq1InputField);
                availableControls.add(freq2InputField);
                canvasesPane.setDividerPosition(0,1.0);
                canvasesPane.setDisable(true);
                imgEqn.setImage(new Image(getClass().getResourceAsStream("resources/beats_param.png")));
                break;
            case ("Lissajous Figures"):
                phaseLabel.setText("Phase shift in degrees:");

                availableControls.add(freq1Label);
                availableControls.add(freq1Slider);
                availableControls.add(freq2Label);
                availableControls.add(freq2Slider);
                availableControls.add(phaseLabel);
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
        playButton.setText("Play!");

        for (Control t: availableControls)  {
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
