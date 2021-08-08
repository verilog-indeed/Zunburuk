package dz.lightyearsoftworks.zunburuk.graphics;

import dz.lightyearsoftworks.zunburuk.ODEDataPoint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;

class GraphPlot  {
    private final ArrayList<ODEDataPoint> dataSeries;
    private final GraphicsContext rembrandtTheRevered;
    private final boolean lissajousMode;
    private static final int MAX_MAJOR_TICKS_ONSCREEN = 15;
    //because theres 60 datapoints per second, should probably make that a constant too
    private static final int MAX_POINTS_ONSCREEN = 60 * MAX_MAJOR_TICKS_ONSCREEN;
    public void drawNextFrame(ODEDataPoint dp)  {
        dataSeries.add(dp);
        if (!lissajousMode && dataSeries.size() > MAX_POINTS_ONSCREEN)   {
            dataSeries.remove(0);
        }

        Canvas cnv = rembrandtTheRevered.getCanvas();
        double midX = lissajousMode? cnv.getWidth() * 0.5: 0.0;
        double midY = cnv.getHeight() * 0.5;
        double xScale = lissajousMode? 10.0 :(cnv.getWidth() / MAX_MAJOR_TICKS_ONSCREEN);
        double yScale = lissajousMode? 10.0 : 25.0;
        if (!lissajousMode) {
            rembrandtTheRevered.setStroke(Color.GRAY);
            rembrandtTheRevered.setLineWidth(1.0);
            for (int i = 1; i <= MAX_MAJOR_TICKS_ONSCREEN; i++) {
                rembrandtTheRevered.strokeLine(i * xScale, 0.0, i * xScale, cnv.getHeight());
                //TODO should not be drawing vertical lines like this, need to properly define how many ticks are on the y-axis
                rembrandtTheRevered.strokeLine(0.0, i * yScale, cnv.getWidth(), i * yScale);
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

    public GraphPlot(GraphicsContext g, boolean isLissajous)   {
        dataSeries = new ArrayList<>();
        rembrandtTheRevered = g;
        lissajousMode = isLissajous;
    }
}