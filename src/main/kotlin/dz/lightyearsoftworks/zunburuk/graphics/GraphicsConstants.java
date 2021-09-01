package dz.lightyearsoftworks.zunburuk.graphics;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public interface GraphicsConstants {
    double FRAME_TIME = 1.0 / 60.0;
    double PENDULUM_TETHER_SCALE = 100.0;
    double BASE_HEIGHT = 10.0;
    double BASE_WIDTH = 35.0;
    double PENDULUM_BOB_BOUNDINGBOX_LENGTH = 35.0;
    double SPRING_SEGMENT_MULTIPLIER = 4.0;
    double SPRING_SEGMENT_ANGLE_BIAS = (Math.PI / 6.0);
    double SPRING_SEGMENT_MIN_ANGLE = (Math.PI / 8.0);
    double SPRING_SEGMENT_LENGTH = 60.0;
    double SPRING_MASS_BOX_LENGTH = 55.0;
    int FUNC_GRAPH_XTICKS = 20;
    int FUNC_GRAPH_YTICKS = 8;
    Paint FUNCTION_GRAPH_COLOR = Color.RED;
    Paint LISSAJOUS_GRAPH_COLOR = Color.ROYALBLUE;
}