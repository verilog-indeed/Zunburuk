package dz.lightyearsoftworks.zunburuk.graphics

import dz.lightyearsoftworks.zunburuk.ODEDataPoint
import dz.lightyearsoftworks.zunburuk.graphics.GraphicsConstants.*
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

abstract class GraphPlot(val canvas: Canvas) {
    lateinit var dataSeries: ArrayList<ODEDataPoint>
    val rembrandtTheRevered = canvas.graphicsContext2D
    abstract fun drawNextFrame(dp: ODEDataPoint)
}

class FunctionalGraphPlot(canvas: Canvas, xTicks: Int, yTicks: Int): GraphPlot(canvas) {
    private val MAX_MAJOR_XAXIS_TICKS_ONSCREEN : Int
    private val MAX_MAJOR_YAXIS_TICKS_ONSCREEN  : Int
    private val MAX_POINTS_ONSCREEN : Int
    init {
        dataSeries = ArrayList()
        MAX_MAJOR_XAXIS_TICKS_ONSCREEN = xTicks
        MAX_MAJOR_YAXIS_TICKS_ONSCREEN = yTicks
        MAX_POINTS_ONSCREEN = (MAX_MAJOR_XAXIS_TICKS_ONSCREEN / FRAME_TIME).toInt()
    }
    override fun drawNextFrame(dp: ODEDataPoint) {
        dataSeries.add(dp)
        if (dataSeries.size > MAX_POINTS_ONSCREEN)  {
            dataSeries.removeAt(0)
        }

        val midY: Double = canvas.height * 0.5
        val xScale = canvas.width / MAX_MAJOR_XAXIS_TICKS_ONSCREEN
        val yScale = canvas.height / MAX_MAJOR_YAXIS_TICKS_ONSCREEN

        drawGridLines(xScale, yScale,0.0, midY)
        drawFunction(xScale, yScale, 0.0, midY, dataSeries.iterator())
    }

    private fun drawGridLines(xAxisScale: Double, yAxisScale: Double, xOffset: Double, yOffset: Double) {
        val oldPaint = rembrandtTheRevered.stroke
        val oldLineWidth = rembrandtTheRevered.lineWidth

        rembrandtTheRevered.stroke = Color.GRAY
        rembrandtTheRevered.lineWidth = 1.0
        //draws vertical gridlines
        for (i in 1..MAX_MAJOR_XAXIS_TICKS_ONSCREEN) {
            rembrandtTheRevered.strokeLine(i * xAxisScale, xOffset, i * xAxisScale, canvas.height)
        }
        //draws gray horizontal gridlines
        for (j in 1..MAX_MAJOR_YAXIS_TICKS_ONSCREEN) {
            rembrandtTheRevered.strokeLine(xOffset, j * yAxisScale, canvas.width, j * yAxisScale)
        }
        //draws the x-axis line
        rembrandtTheRevered.stroke = Color.BLACK
        rembrandtTheRevered.lineWidth = 2.0
        rembrandtTheRevered.strokeLine(xOffset, yOffset, canvas.width, yOffset)

        rembrandtTheRevered.stroke = oldPaint
        rembrandtTheRevered.lineWidth = oldLineWidth
    }

    private fun drawFunction(xAxisScale: Double, yAxisScale: Double, xOffset: Double, yOffset: Double, dataIterator: Iterator<ODEDataPoint>)  {
        val oldPaint = rembrandtTheRevered.stroke
        val oldLineWidth = rembrandtTheRevered.lineWidth
        rembrandtTheRevered.stroke = FUNCTION_GRAPH_COLOR
        rembrandtTheRevered.lineWidth = 3.0

        var firstPoint: ODEDataPoint = dataIterator.next()
        while (dataIterator.hasNext()) {
           val secondPoint = dataIterator.next()
           drawLineFromDP(firstPoint, secondPoint, xAxisScale, yAxisScale, xOffset, yOffset)
           firstPoint = secondPoint
        }

        rembrandtTheRevered.stroke = oldPaint
        rembrandtTheRevered.lineWidth = oldLineWidth
    }

    private fun drawLineFromDP(firstPoint: ODEDataPoint, secondPoint: ODEDataPoint, xAxisScale: Double, yAxisScale: Double, xOffset: Double, yOffset: Double) {
        var x1: Double = xOffset + firstPoint.t * xAxisScale
        val y1: Double = yOffset - firstPoint.y * yAxisScale
        var x2: Double = xOffset + secondPoint.t * xAxisScale
        val y2: Double = yOffset - secondPoint.y * yAxisScale
        if (dataSeries.size == MAX_POINTS_ONSCREEN) {
            /*if the datapoints are going offscreen, we subtract the virtual xPosition of the first datapoint
              in the series, because we keep removing the first datapoint on the next frame this produces a
              rolling effect*/
            x1 = x1 - dataSeries[0].t * xAxisScale
            x2 = x2 - dataSeries[0].t * xAxisScale
        }
        rembrandtTheRevered.strokeLine(x1, y1, x2, y2)
    }
}

class LissajousGraphPlot(canvas: Canvas): GraphPlot(canvas) {
    fun drawGraph(func: ArrayList<ODEDataPoint>) {
        dataSeries = func
        drawNextFrame(dataSeries.removeAt(dataSeries.size - 1))
    }

    override fun drawNextFrame(dp: ODEDataPoint) {
        dataSeries.add(dp)
        val midX = canvas.width * 0.5
        val midY = canvas.height * 0.5
        val lesserDimension = Math.min(canvas.height, canvas.width)
        //same scale used for x and y axis to preserve 1:1 aspect ratio
        val uniformScale = lesserDimension / 3.0
        drawLissajousCurve(uniformScale, midX, midY, dataSeries.iterator())
    }

    //TODO: this is pretty much a copy of FunctionalGraphPlot.drawFunction(), must be a better way of organizing this...
    private fun drawLissajousCurve(uniformScale: Double, midX: Double, midY: Double, dataIterator: Iterator<ODEDataPoint>) {
        val oldPaint = rembrandtTheRevered.stroke
        val oldLineWidth = rembrandtTheRevered.lineWidth
        rembrandtTheRevered.stroke = LISSAJOUS_GRAPH_COLOR
        rembrandtTheRevered.lineWidth = 3.0

        var firstPoint: ODEDataPoint = dataIterator.next()
        while (dataIterator.hasNext()) {
            val secondPoint = dataIterator.next()
            drawLineFromDP(firstPoint, secondPoint, uniformScale, midX, midY)
            firstPoint = secondPoint
        }

        rembrandtTheRevered.stroke = oldPaint
        rembrandtTheRevered.lineWidth = oldLineWidth
    }

    private fun drawLineFromDP(firstPoint: ODEDataPoint, secondPoint: ODEDataPoint, uniformScale: Double, xOffset: Double, yOffset: Double) {
        val x1: Double = xOffset + firstPoint.t * uniformScale
        val y1: Double = yOffset - firstPoint.y * uniformScale
        val x2: Double = xOffset + secondPoint.t * uniformScale
        val y2: Double = yOffset - secondPoint.y * uniformScale
        rembrandtTheRevered.strokeLine(x1, y1, x2, y2)
    }
}