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
    private static final int MAX_MAJOR_TICKS_ONSCREEN = 5;
    //because theres 60 datapoints per second, should probably make that a constant too
    private static final int MAX_POINTS_ONSCREEN = 60 * MAX_MAJOR_TICKS_ONSCREEN;
    public void drawNextFrame(ODEDataPoint dp)  {
        dataSeries.add(dp);
        if (dataSeries.size() > MAX_POINTS_ONSCREEN)   {
            dataSeries.remove(0);
        }
        Canvas cnv = rembrandtTheRevered.getCanvas();
        double midY = cnv.getHeight() * 0.5;
        double xScale = (cnv.getWidth() / MAX_POINTS_ONSCREEN) * 60.0; double yScale = 10.0;
        rembrandtTheRevered.setStroke(Color.BLACK);
        rembrandtTheRevered.strokeLine(0.0, midY, cnv.getWidth(), midY);

        rembrandtTheRevered.setStroke(Color.RED);
        Iterator<ODEDataPoint> it = dataSeries.iterator();
        ODEDataPoint first = it.next();
        while (it.hasNext()) {
            ODEDataPoint second = it.next();
            double x1 = first.getT() * xScale;
            double y1 = midY - first.getY() * yScale;
            double x2 = second.getT() * xScale;
            double y2 = midY - second.getY() * yScale;
            if (dataSeries.size() == MAX_POINTS_ONSCREEN) {
                /*if the datapoints are going offscreen, we subtract the virtual xPosition of the first datapoint
                  in the series, because we keep removing the first datapoint on the next frame this produces a
                  rolling effect*/
                x1 = x1 - dataSeries.get(0).getT() * xScale;
                x2 = x2 - dataSeries.get(0).getT() * xScale;
            }
            rembrandtTheRevered.strokeLine(x1, y1, x2, y2);
            first = second;
        }
    }
    public GraphPlot(GraphicsContext g)   {
        dataSeries = new ArrayList<>();
        rembrandtTheRevered = g;
    }
}