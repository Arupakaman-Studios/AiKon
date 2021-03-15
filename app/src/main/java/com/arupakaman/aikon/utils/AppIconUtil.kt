package com.arupakaman.aikon.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.arupakaman.aikon.R
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object AppIconUtil {

    fun getAdaptiveIcon(src: Bitmap, bgSrc: Bitmap, padding: Int): Bitmap {
        val bgWidth = src.width + padding
        val bgHeight = src.height + padding

        Log.d("AppIconUtil", "getAdaptiveIcon -> $padding")

        val backgroundBitmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888)
        Canvas(backgroundBitmap).drawBitmap(bgSrc.copy(Bitmap.Config.ARGB_8888, true), Matrix(), null)

        val bitmapToDrawInTheCenter = src.copy(Bitmap.Config.ARGB_8888, true)

        val resultBitmap = Bitmap.createBitmap(bgWidth, bgHeight, backgroundBitmap.config)
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(backgroundBitmap, Matrix(), null)
        canvas.drawBitmap(
            bitmapToDrawInTheCenter,
            ((backgroundBitmap.width - bitmapToDrawInTheCenter.width) / 2).toFloat(),
            ((backgroundBitmap.height - bitmapToDrawInTheCenter.height) / 2).toFloat(),
            Paint()
        )

        return resultBitmap
    }

    fun getColorToBitmap(color: Int, padding: Int): Bitmap{
        val backgroundBitmap = Bitmap.createBitmap(512 + padding, 512 + padding, Bitmap.Config.ARGB_8888)
        Canvas(backgroundBitmap).drawColor(color)
        return backgroundBitmap
    }

    fun vectorDrawableToBitmap(context: Context, drawableId: Int, size: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        return bmp
    }

    fun uriToBitmap(mContext: Context, uri: Uri?, size: Int = 512): Bitmap? {
        uri?.runCatching {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(mContext.contentResolver, uri)
            }
            return Bitmap.createScaledBitmap(bitmap, size, size, false)
        }?.onFailure {
            mContext.toast(AikonRes.getString(R.string.err_msg_general))
            Log.e("AppIconUtil", "uriToBitmap Exc -> $it")
        }
        return null
    }

    const val PATH_CIRCLE = 0
    const val PATH_SQUIRCLE = 1
    const val PATH_ROUNDED_SQUARE = 2
    const val PATH_SQUARE = 3
    const val PATH_TEARDROP = 4

    fun resizePath(path: Path, width: Float, height: Float): Path {
        val bounds = RectF(0f, 0f, width, height)
        val resizedPath = Path(path)
        val src = RectF()
        resizedPath.computeBounds(src, true)
        val resizeMatrix = Matrix()
        resizeMatrix.setRectToRect(src, bounds, Matrix.ScaleToFit.CENTER)
        resizedPath.transform(resizeMatrix)
        Log.d("AppIconUtil", "resizePath Return")
        return resizedPath
    }

    fun getMaskedBitmap(src: Bitmap, path: Path, resizePathToMatchBitmap: Boolean = true): Bitmap {
        val pathToUse = if (resizePathToMatchBitmap) resizePath(
            path,
            src.width.toFloat(),
            src.height.toFloat()
        ) else path
        Log.d("AppIconUtil", "getMaskedBitmap 1")
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = 0XFF000000.toInt()
        canvas.drawPath(pathToUse, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        Log.d("AppIconUtil", "getMaskedBitmap 2")
        canvas.drawBitmap(output, 0f, 0f, paint)
        return output
    }

    fun getMaskedBitmap(drawable: Drawable, path: Path, resizePathToMatchBitmap: Boolean = true): Bitmap = getMaskedBitmap(
        drawable.toBitmap(),
        path,
        resizePathToMatchBitmap
    )

    fun getPath(pathType: Int): Path {
        val path = Path()
        val pathSize = Rect(0, 0, 50, 50)
        when (pathType) {
            PATH_CIRCLE -> {
                path.arcTo(RectF(pathSize), 0f, 359f)
                path.close()
            }
            PATH_SQUIRCLE -> path.set(PathUtils.createPathFromPathData("M 50,0 C 10,0 0,10 0,50 C 0,90 10,100 50,100 C 90,100 100,90 100,50 C 100,10 90,0 50,0 Z"))
            PATH_ROUNDED_SQUARE -> path.set(PathUtils.createPathFromPathData("M 50,0 L 70,0 A 30,30,0,0 1 100,30 L 100,70 A 30,30,0,0 1 70,100 L 30,100 A 30,30,0,0 1 0,70 L 0,30 A 30,30,0,0 1 30,0 z"))
            PATH_SQUARE -> {
                path.lineTo(0f, 50f)
                path.lineTo(50f, 50f)
                path.lineTo(50f, 0f)
                path.lineTo(0f, 0f)
                path.close()
            }
            PATH_TEARDROP -> path.set(PathUtils.createPathFromPathData("M 50,0 A 50,50,0,0 1 100,50 L 100,85 A 15,15,0,0 1 85,100 L 50,100 A 50,50,0,0 1 50,0 z"))
        }
        return path
    }

}

object PathUtils {

    /**
     * @param pathData The string representing a path, the same as "d" string in svg file.
     * @return the generated Path object.
     */
    fun createPathFromPathData(pathData: String): Path {
        val path = Path()
        val nodes = createNodesFromPathData(pathData)
        PathDataNode.nodesToPath(nodes, path)
        return path
    }

    /**
     * @param pathData The string representing a path, the same as "d" string in svg file.
     * @return an array of the PathDataNode.
     */
    fun createNodesFromPathData(pathData: String): Array<PathDataNode> {
        var start = 0
        var end = 1
        val list = ArrayList<PathDataNode>()
        while (end < pathData.length) {
            end = nextStart(pathData, end)
            val s = pathData.substring(start, end)
            val value = getFloats(s)
            addNode(list, s[0], value)
            start = end
            end++
        }
        if (end - start == 1 && start < pathData.length) {
            addNode(list, pathData[start], FloatArray(0))
        }
        return list.toTypedArray()
    }

    private fun nextStart(s: String, inputEnd: Int): Int {
        var end = inputEnd
        var c: Char
        while (end < s.length) {
            c = s[end]
            if ((c - 'A') * (c - 'Z') <= 0 || (c - 'a') * (c - 'z') <= 0) return end
            end++
        }
        return end
    }

    private fun addNode(list: ArrayList<PathDataNode>, cmd: Char, `val`: FloatArray) {
        list.add(PathDataNode(cmd, `val`))
    }

    /**
     * Parse the floats in the string.
     * This is an optimized version of parseFloat(s.split(",|\\s"));
     *
     * @param s the string containing a command and list of floats
     * @return array of floats
     */
    @Throws(NumberFormatException::class)
    private fun getFloats(s: String): FloatArray {
        if (s[0] == 'z' || s[0] == 'Z')
            return FloatArray(0)
        val tmp = FloatArray(s.length)
        var count = 0
        var pos = 1
        var end: Int
        while (extract(s, pos).also { end = it } >= 0) {
            if (pos < end) tmp[count++] = s.substring(pos, end).toFloat()
            pos = end + 1
        }
        // handle the final float if there is one
        if (pos < s.length) tmp[count++] = s.substring(pos).toFloat()
        return tmp.copyOf(count)
    }

    /**
     * Calculate the position of the next comma or space
     *
     * @param s     the string to search
     * @param start the position to start searching
     * @return the position of the next comma or space or -1 if none found
     */
    private fun extract(s: String, start: Int): Int {
        val space = s.indexOf(' ', start)
        val comma = s.indexOf(',', start)
        if (space == -1) return comma
        return if (comma == -1) space else min(comma, space)
    }

    class PathDataNode(private val type: Char, private var params: FloatArray) {
        @Suppress("unused")
        constructor(n: PathDataNode) : this(n.type, n.params.copyOf(n.params.size))

        companion object {
            fun nodesToPath(node: Array<PathDataNode>, path: Path) {
                val current = FloatArray(4)
                var previousCommand = 'm'
                for (pathDataNode in node) {
                    addCommand(
                        path,
                        current,
                        previousCommand,
                        pathDataNode.type,
                        pathDataNode.params
                    )
                    previousCommand = pathDataNode.type
                }
            }

            private fun addCommand(
                path: Path,
                current: FloatArray,
                inputPreviousCmd: Char,
                cmd: Char,
                floats: FloatArray
            ) {
                var previousCmd = inputPreviousCmd
                var incr = 2
                var currentX = current[0]
                var currentY = current[1]
                var ctrlPointX = current[2]
                var ctrlPointY = current[3]
                var reflectiveCtrlPointX: Float
                var reflectiveCtrlPointY: Float
                when (cmd) {
                    'z', 'Z' -> {
                        path.close()
                        return
                    }
                    'm', 'M', 'l', 'L', 't', 'T' -> incr = 2
                    'h', 'H', 'v', 'V' -> incr = 1
                    'c', 'C' -> incr = 6
                    's', 'S', 'q', 'Q' -> incr = 4
                    'a', 'A' -> incr = 7
                }
                var k = 0
                while (k < floats.size) {
                    when (cmd) {
                        'm' -> {
                            path.rMoveTo(floats[k], floats[k + 1])
                            currentX += floats[k]
                            currentY += floats[k + 1]
                        }
                        'M' -> {
                            path.moveTo(floats[k], floats[k + 1])
                            currentX = floats[k]
                            currentY = floats[k + 1]
                        }
                        'l' -> {
                            path.rLineTo(floats[k], floats[k + 1])
                            currentX += floats[k]
                            currentY += floats[k + 1]
                        }
                        'L' -> {
                            path.lineTo(floats[k], floats[k + 1])
                            currentX = floats[k]
                            currentY = floats[k + 1]
                        }
                        'h' -> {
                            path.rLineTo(floats[k], 0f)
                            currentX += floats[k]
                        }
                        'H' -> {
                            path.lineTo(floats[k], currentY)
                            currentX = floats[k]
                        }
                        'v' -> {
                            path.rLineTo(0f, floats[k])
                            currentY += floats[k]
                        }
                        'V' -> {
                            path.lineTo(currentX, floats[k])
                            currentY = floats[k]
                        }
                        'c' -> {
                            path.rCubicTo(
                                floats[k],
                                floats[k + 1],
                                floats[k + 2],
                                floats[k + 3],
                                floats[k + 4],
                                floats[k + 5]
                            )
                            ctrlPointX = currentX + floats[k + 2]
                            ctrlPointY = currentY + floats[k + 3]
                            currentX += floats[k + 4]
                            currentY += floats[k + 5]
                        }
                        'C' -> {
                            path.cubicTo(
                                floats[k], floats[k + 1], floats[k + 2], floats[k + 3],
                                floats[k + 4], floats[k + 5]
                            )
                            currentX = floats[k + 4]
                            currentY = floats[k + 5]
                            ctrlPointX = floats[k + 2]
                            ctrlPointY = floats[k + 3]
                        }
                        's' -> {
                            reflectiveCtrlPointX = 0f
                            reflectiveCtrlPointY = 0f
                            if (previousCmd == 'c' || previousCmd == 's' || previousCmd == 'C' || previousCmd == 'S') {
                                reflectiveCtrlPointX = currentX - ctrlPointX
                                reflectiveCtrlPointY = currentY - ctrlPointY
                            }
                            path.rCubicTo(
                                reflectiveCtrlPointX,
                                reflectiveCtrlPointY,
                                floats[k],
                                floats[k + 1],
                                floats[k + 2],
                                floats[k + 3]
                            )
                            ctrlPointX = currentX + floats[k]
                            ctrlPointY = currentY + floats[k + 1]
                            currentX += floats[k + 2]
                            currentY += floats[k + 3]
                        }
                        'S' -> {
                            reflectiveCtrlPointX = currentX
                            reflectiveCtrlPointY = currentY
                            if (previousCmd == 'c' || previousCmd == 's' || previousCmd == 'C' || previousCmd == 'S') {
                                reflectiveCtrlPointX = 2 * currentX - ctrlPointX
                                reflectiveCtrlPointY = 2 * currentY - ctrlPointY
                            }
                            path.cubicTo(
                                reflectiveCtrlPointX,
                                reflectiveCtrlPointY,
                                floats[k],
                                floats[k + 1],
                                floats[k + 2],
                                floats[k + 3]
                            )
                            ctrlPointX = floats[k]
                            ctrlPointY = floats[k + 1]
                            currentX = floats[k + 2]
                            currentY = floats[k + 3]
                        }
                        'q' -> {
                            path.rQuadTo(floats[k], floats[k + 1], floats[k + 2], floats[k + 3])
                            ctrlPointX = currentX + floats[k]
                            ctrlPointY = currentY + floats[k + 1]
                            currentX += floats[k + 2]
                            currentY += floats[k + 3]
                        }
                        'Q' -> {
                            path.quadTo(floats[k], floats[k + 1], floats[k + 2], floats[k + 3])
                            ctrlPointX = floats[k]
                            ctrlPointY = floats[k + 1]
                            currentX = floats[k + 2]
                            currentY = floats[k + 3]
                        }
                        't' -> {
                            reflectiveCtrlPointX = 0f
                            reflectiveCtrlPointY = 0f
                            if (previousCmd == 'q' || previousCmd == 't' || previousCmd == 'Q' || previousCmd == 'T') {
                                reflectiveCtrlPointX = currentX - ctrlPointX
                                reflectiveCtrlPointY = currentY - ctrlPointY
                            }
                            path.rQuadTo(
                                reflectiveCtrlPointX, reflectiveCtrlPointY,
                                floats[k], floats[k + 1]
                            )
                            ctrlPointX = currentX + reflectiveCtrlPointX
                            ctrlPointY = currentY + reflectiveCtrlPointY
                            currentX += floats[k]
                            currentY += floats[k + 1]
                        }
                        'T' -> {
                            reflectiveCtrlPointX = currentX
                            reflectiveCtrlPointY = currentY
                            if (previousCmd == 'q' || previousCmd == 't' || previousCmd == 'Q' || previousCmd == 'T') {
                                reflectiveCtrlPointX = 2 * currentX - ctrlPointX
                                reflectiveCtrlPointY = 2 * currentY - ctrlPointY
                            }
                            path.quadTo(
                                reflectiveCtrlPointX,
                                reflectiveCtrlPointY,
                                floats[k],
                                floats[k + 1]
                            )
                            ctrlPointX = reflectiveCtrlPointX
                            ctrlPointY = reflectiveCtrlPointY
                            currentX = floats[k]
                            currentY = floats[k + 1]
                        }
                        'a' -> {
                            // (rx ry x-axis-rotation large-arc-flag sweep-flag x y)
                            drawArc(
                                path,
                                currentX,
                                currentY,
                                floats[k + 5] + currentX,
                                floats[k + 6] + currentY,
                                floats[k],
                                floats[k + 1],
                                floats[k + 2],
                                floats[k + 3] != 0f,
                                floats[k + 4] != 0f
                            )
                            currentX += floats[k + 5]
                            currentY += floats[k + 6]
                            ctrlPointX = currentX
                            ctrlPointY = currentY
                        }
                        'A' -> {
                            drawArc(
                                path,
                                currentX,
                                currentY,
                                floats[k + 5],
                                floats[k + 6],
                                floats[k],
                                floats[k + 1],
                                floats[k + 2],
                                floats[k + 3] != 0f,
                                floats[k + 4] != 0f
                            )
                            currentX = floats[k + 5]
                            currentY = floats[k + 6]
                            ctrlPointX = currentX
                            ctrlPointY = currentY
                        }
                    }
                    previousCmd = cmd
                    k += incr
                }
                current[0] = currentX
                current[1] = currentY
                current[2] = ctrlPointX
                current[3] = ctrlPointY
            }

            private fun drawArc(
                p: Path,
                x0: Float,
                y0: Float,
                x1: Float,
                y1: Float,
                a: Float,
                b: Float,
                theta: Float,
                isMoreThanHalf: Boolean,
                isPositiveArc: Boolean
            ) {
                /* Convert rotation angle from degrees to radians */
                val thetaD = Math.toRadians(theta.toDouble())
                /* Pre-compute rotation matrix entries */
                val cosTheta = cos(thetaD)
                val sinTheta = sin(thetaD)
                /* Transform (x0, y0) and (x1, y1) into unit space */
                /* using (inverse) rotation, followed by (inverse) scale */
                val x0p = (x0 * cosTheta + y0 * sinTheta) / a
                val y0p = (-x0 * sinTheta + y0 * cosTheta) / b
                val x1p = (x1 * cosTheta + y1 * sinTheta) / a
                val y1p = (-x1 * sinTheta + y1 * cosTheta) / b
                /* Compute differences and averages */
                val dx = x0p - x1p
                val dy = y0p - y1p
                val xm = (x0p + x1p) / 2
                val ym = (y0p + y1p) / 2
                /* Solve for intersecting unit circles */
                val dsq = dx * dx + dy * dy
                if (dsq == 0.0) return  /* Points are coincident */
                val disc = 1.0 / dsq - 1.0 / 4.0
                if (disc < 0.0) {
                    val adjust = (sqrt(dsq) / 1.99999).toFloat()
                    drawArc(
                        p,
                        x0,
                        y0,
                        x1,
                        y1,
                        a * adjust,
                        b * adjust,
                        theta,
                        isMoreThanHalf,
                        isPositiveArc
                    )
                    return  /* Points are too far apart */
                }
                val s = sqrt(disc)
                val sdx = s * dx
                val sdy = s * dy
                var cx: Double
                var cy: Double
                if (isMoreThanHalf == isPositiveArc) {
                    cx = xm - sdy
                    cy = ym + sdx
                } else {
                    cx = xm + sdy
                    cy = ym - sdx
                }
                val eta0 = atan2(y0p - cy, x0p - cx)
                val eta1 = atan2(y1p - cy, x1p - cx)
                var sweep = eta1 - eta0
                if (isPositiveArc != sweep >= 0) {
                    if (sweep > 0) {
                        sweep -= 2 * Math.PI
                    } else {
                        sweep += 2 * Math.PI
                    }
                }
                cx *= a.toDouble()
                cy *= b.toDouble()
                val tcx = cx
                cx = cx * cosTheta - cy * sinTheta
                cy = tcx * sinTheta + cy * cosTheta
                arcToBezier(
                    p,
                    cx,
                    cy,
                    a.toDouble(),
                    b.toDouble(),
                    x0.toDouble(),
                    y0.toDouble(),
                    thetaD,
                    eta0,
                    sweep
                )
            }

            /**
             * Converts an arc to cubic Bezier segments and records them in p.
             *
             * @param p     The target for the cubic Bezier segments
             * @param cx    The x coordinate center of the ellipse
             * @param cy    The y coordinate center of the ellipse
             * @param a     The radius of the ellipse in the horizontal direction
             * @param b     The radius of the ellipse in the vertical direction
             * @param inputE1x   E(eta1) x coordinate of the starting point of the arc
             * @param inputE1y   E(eta2) y coordinate of the starting point of the arc
             * @param theta The angle that the ellipse bounding rectangle makes with horizontal plane
             * @param start The start angle of the arc on the ellipse
             * @param sweep The angle (positive or negative) of the sweep of the arc on the ellipse
             */
            private fun arcToBezier(
                p: Path,
                cx: Double,
                cy: Double,
                a: Double,
                b: Double,
                inputE1x: Double,
                inputE1y: Double,
                theta: Double,
                start: Double,
                sweep: Double
            ) {
                // Taken from equations at: http://spaceroots.org/documents/ellipse/node8.html
                // and http://www.spaceroots.org/documents/ellipse/node22.html
                // Maximum of 45 degrees per cubic Bezier segment
                var e1x = inputE1x
                var e1y = inputE1y
                val numSegments = abs(ceil(sweep * 4 / Math.PI).toInt())
                var eta1 = start
                val cosTheta = cos(theta)
                val sinTheta = sin(theta)
                val cosEta1 = cos(eta1)
                val sinEta1 = sin(eta1)
                var ep1x = -a * cosTheta * sinEta1 - b * sinTheta * cosEta1
                var ep1y = -a * sinTheta * sinEta1 + b * cosTheta * cosEta1
                val anglePerSegment = sweep / numSegments
                for (i in 0 until numSegments) {
                    val eta2 = eta1 + anglePerSegment
                    val sinEta2 = sin(eta2)
                    val cosEta2 = cos(eta2)
                    val e2x = cx + a * cosTheta * cosEta2 - b * sinTheta * sinEta2
                    val e2y = cy + a * sinTheta * cosEta2 + b * cosTheta * sinEta2
                    val ep2x = -a * cosTheta * sinEta2 - b * sinTheta * cosEta2
                    val ep2y = -a * sinTheta * sinEta2 + b * cosTheta * cosEta2
                    val tanDiff2 = tan((eta2 - eta1) / 2)
                    val alpha = sin(eta2 - eta1) * (sqrt(4 + 3 * tanDiff2 * tanDiff2) - 1) / 3
                    val q1x = e1x + alpha * ep1x
                    val q1y = e1y + alpha * ep1y
                    val q2x = e2x - alpha * ep2x
                    val q2y = e2y - alpha * ep2y
                    p.cubicTo(
                        q1x.toFloat(),
                        q1y.toFloat(),
                        q2x.toFloat(),
                        q2y.toFloat(),
                        e2x.toFloat(),
                        e2y.toFloat()
                    )
                    eta1 = eta2
                    e1x = e2x
                    e1y = e2y
                    ep1x = ep2x
                    ep1y = ep2y
                }
            }
        }
    }
}