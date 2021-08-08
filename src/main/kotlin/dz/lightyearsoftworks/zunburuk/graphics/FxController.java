package dz.lightyearsoftworks.zunburuk.graphics;

import dz.lightyearsoftworks.zunburuk.DifferentialEquationType;
import dz.lightyearsoftworks.zunburuk.DifferentialSolver;
import dz.lightyearsoftworks.zunburuk.EquationParameters;
import dz.lightyearsoftworks.zunburuk.ODEDataPoint;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static java.lang.Math.cos;
import static java.lang.Math.sin;


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
    private GraphicsContext picassoThePainter;
    private Timeline currentAnimation;
    private final ArrayList<TextField> availableInputFields = new ArrayList<>();
    private GraphicsContext rembrandtTheRevered;


    public void onPlayButtonPress(ActionEvent actionEvent) {
        for (TextField t: availableInputFields) {
            if (t.getText().equals("")) {
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
                        0.0, 1.0 / 6000.0);
                graph = new GraphPlot(rembrandtTheRevered, false);
                simulationSteppingHandler = event -> {
                    //ODEDataPoint dp = currentSystem1.nextDataPoint();
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
                        0.0, 1.0 / 6000.0);
                graph = new GraphPlot(rembrandtTheRevered, false);

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
                        0.0, 1.0 / 6000.0);
                graph = new GraphPlot(rembrandtTheRevered, false);
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
                System.out.println("By Dr. Hefner");
                simulationSteppingHandler = null;
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
                double angVel1 = 2.0 * Math.PI * freq1;
                double angVel2 = 2.0 * Math.PI * freq2;
                double amplitude = 10.0;
                currentSystem1 = new DifferentialSolver(DifferentialEquationType.ORDER2_UNDAMPED,
                        new EquationParameters(amplitude,
                                0,
                                angVel1 * angVel1,
                                0.0, 0),
                        0.0, 1.0 / 6000.0);
                currentSystem2 = new DifferentialSolver(DifferentialEquationType.ORDER2_UNDAMPED,
                        new EquationParameters(amplitude * cos(phi),
                                -amplitude * angVel2 * sin(phi),
                                angVel2 * angVel2,
                                0.0, 0),
                        0.0, 1.0 / 6000.0);
                graph = new GraphPlot(picassoThePainter, true);
                simulationSteppingHandler = event -> {
                    ODEDataPoint dp1 = currentSystem1.stepSimByNSteps(100);
                    ODEDataPoint dp2 = currentSystem2.stepSimByNSteps(100);
                    ODEDataPoint dp = new ODEDataPoint(dp1.getY(), dp2.getY());
                    clearCanvas(mainCanvas);
                    graph.drawNextFrame(dp);
                };
                break;
            default:
                simulationSteppingHandler = null;
        }

        if (simulationSteppingHandler != null) {
            currentAnimation = new Timeline(new KeyFrame(Duration.seconds(1 / 60.0), simulationSteppingHandler));
            currentAnimation.setCycleCount(Timeline.INDEFINITE);
            currentAnimation.play();
        }
    }

    private void redrawSpringMass(ODEDataPoint dp, double maxDisplacement) {
        double width = mainCanvas.getWidth(); //double height = mainCanvas.getHeight();
        picassoThePainter.setLineWidth(3.0);
        double baseX = width/2.0; double baseY = 60;
        int springSegs = (int) (maxDisplacement * 4.0);
        //theta is the angle going counter-clockwise from the y-axis to the spring segment
        double theta = (Math.PI / 6.0) * (1 - (dp.getY() / maxDisplacement)) + (Math.PI / 8.0);

        picassoThePainter.strokeLine(baseX, baseY, baseX + 20 * Math.sin(theta), baseY + 20 * cos(theta));
        baseX += 20 * Math.sin(theta);
        baseY += 20 * cos(theta);
        theta = -theta;
        for (int i = 0; i < springSegs; i++)   {
            picassoThePainter.strokeLine(baseX, baseY, baseX + 40 * Math.sin(theta), baseY + 40 * cos(theta));
            baseX += 40 * Math.sin(theta);
            baseY += 40 * cos(theta);
            theta = -theta; //reverse x-direction of the next spring segment
        }
        picassoThePainter.strokeLine(baseX, baseY, baseX + 20 * Math.sin(theta), baseY + 20 * cos(theta));
        baseX += 20 * Math.sin(theta);
        baseY += 20 * cos(theta);

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
                availableInputFields.add(gravityInputField);
                availableInputFields.add(lengthInputField);
                availableInputFields.add(maxAngleInputField);
                canvasesPane.setDividerPosition(0,0.65);
                canvasesPane.setDisable(false);
                imgEqn.setImage(new Image(getClass().getResourceAsStream("resources/ode_pendulum.png")));
                break;
            case ("Mass on a Vertical Spring"):
                availableInputFields.add(springConstInputField);
                availableInputFields.add(massInputField);
                availableInputFields.add(maxDisplacementInputField);
                canvasesPane.setDividerPosition(0,0.65);
                canvasesPane.setDisable(false);
                imgEqn.setImage(new Image(getClass().getResourceAsStream("resources/ode_springmass.png")));
                break;
            case ("Damped Mass on a Vertical Spring"):
                availableInputFields.add(springConstInputField);
                availableInputFields.add(massInputField);
                availableInputFields.add(maxDisplacementInputField);
                availableInputFields.add(dampingFactorInputField);
                canvasesPane.setDividerPosition(0,0.65);
                canvasesPane.setDisable(false);
                imgEqn.setImage(new Image(getClass().getResourceAsStream("resources/ode_springmass_damped.png")));
                break;
            case ("Beats demo"):
                System.out.println("By Dr. Hefner");
                break;
            case ("Lissajous Figures"):
                availableInputFields.add(freq1InputField);
                availableInputFields.add(freq2InputField);
                availableInputFields.add(phaseInputField);
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
        for(TextField t: availableInputFields)  {
            t.setManaged(true);
            t.setVisible(true);
        }
    }

    /**Sets the imagebox, textfields and playbutton to invisible
     * clears the individual textfields and the set of available textfields*/
    private void hideControls() {
        imgEqn.setManaged(false);
        imgEqn.setVisible(false);

        playButton.setManaged(false);
        playButton.setVisible(false);

        for (TextField t: availableInputFields)  {
            t.clear();
            t.setManaged(false);
            t.setVisible(false);
        }
        availableInputFields.clear();
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
