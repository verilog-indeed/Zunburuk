package dz.lightyearsoftworks.zunburuk.graphics;

import dz.lightyearsoftworks.zunburuk.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;

import static dz.lightyearsoftworks.zunburuk.graphics.GraphicsConstants.*;

interface GraphicsConstants {
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
}

class GraphPlot  {
    private ArrayList<ODEDataPoint> dataSeries;
    private final GraphicsContext rembrandtTheRevered;
    private final boolean lissajousMode;
    //default 20?
    private final int MAX_MAJOR_XAXIS_TICKS_ONSCREEN;
    private final int MAX_MAJOR_YAXIS_TICKS_ONSCREEN;
    //because theres 60 datapoints per second, should probably make that a constant too
    private final int MAX_POINTS_ONSCREEN;

    public void drawGraph(ArrayList<ODEDataPoint> function)   {
        dataSeries = function;
        drawNextFrame(dataSeries.remove(dataSeries.size() - 1));
    }

    public void drawNextFrame(ODEDataPoint dp)  {
        dataSeries.add(dp);
        if (!lissajousMode && dataSeries.size() > MAX_POINTS_ONSCREEN)   {
            dataSeries.remove(0);
        }

        Canvas cnv = rembrandtTheRevered.getCanvas();
        double midX = lissajousMode? cnv.getWidth() * 0.5: 0.0;
        double midY = cnv.getHeight() * 0.5;
        double lesserDimension = Math.min(cnv.getHeight(), cnv.getWidth());
        double xScale = lissajousMode? lesserDimension / 3 :(cnv.getWidth() / MAX_MAJOR_XAXIS_TICKS_ONSCREEN);
        double yScale = lissajousMode? lesserDimension / 3 : (cnv.getHeight()) / MAX_MAJOR_YAXIS_TICKS_ONSCREEN;
        if (!lissajousMode) {
            rembrandtTheRevered.setStroke(Color.GRAY);
            rembrandtTheRevered.setLineWidth(1.0);
            for (int i = 1; i <= MAX_MAJOR_XAXIS_TICKS_ONSCREEN; i++) {
                rembrandtTheRevered.strokeLine(i * xScale, 0.0, i * xScale, cnv.getHeight());
            }
            for (int j = 1; j <= MAX_MAJOR_YAXIS_TICKS_ONSCREEN; j++)   {
                rembrandtTheRevered.strokeLine(0.0, j * yScale, cnv.getWidth(), j * yScale);
            }
            //draws the x-axis line
            rembrandtTheRevered.setStroke(Color.BLACK);
            rembrandtTheRevered.setLineWidth(2.0);
            rembrandtTheRevered.strokeLine(0.0, midY, cnv.getWidth(), midY);
        }
        rembrandtTheRevered.setStroke(Color.RED);
        rembrandtTheRevered.setLineWidth(3.0);
        Iterator<ODEDataPoint> it = dataSeries.iterator();
        ODEDataPoint first = it.next();
        while (it.hasNext()) {
            ODEDataPoint second = it.next();
            drawLineFromDP(first, second, xScale, yScale, midX, midY);
            first = second;
        }
    }

    private void drawLineFromDP(ODEDataPoint first, ODEDataPoint second, double xAxisScale, double yAxisScale, double xOffset, double yOffset) {
        double x1 = xOffset + first.getT() * xAxisScale;
        double y1 = yOffset - first.getY() * yAxisScale;
        double x2 = xOffset + second.getT() * xAxisScale;
        double y2 = yOffset - second.getY() * yAxisScale;
        if (!lissajousMode && dataSeries.size() == MAX_POINTS_ONSCREEN) {
                /*if the datapoints are going offscreen, we subtract the virtual xPosition of the first datapoint
                  in the series, because we keep removing the first datapoint on the next frame this produces a
                  rolling effect*/
            x1 = x1 - dataSeries.get(0).getT() * xAxisScale;
            x2 = x2 - dataSeries.get(0).getT() * xAxisScale;
        }
        rembrandtTheRevered.strokeLine(x1, y1, x2, y2);

    }

    public GraphPlot(GraphicsContext g, boolean isLissajous, int xTicks, int yTicks)   {
        dataSeries = new ArrayList<>();
        rembrandtTheRevered = g;
        lissajousMode = isLissajous;
        MAX_MAJOR_XAXIS_TICKS_ONSCREEN = xTicks;
        MAX_MAJOR_YAXIS_TICKS_ONSCREEN = yTicks;
        MAX_POINTS_ONSCREEN = (int) (MAX_MAJOR_XAXIS_TICKS_ONSCREEN / FRAME_TIME);
    }
}