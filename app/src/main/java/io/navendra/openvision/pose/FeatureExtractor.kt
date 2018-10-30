package io.navendra.openvision.pose

import android.graphics.Bitmap
import io.navendra.openvision.pose.models.FeatureModel
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.KAZE

class FeatureExtractor constructor(val bitmap: Bitmap){

    lateinit var featureModel : FeatureModel

    private lateinit var keyPoints : MatOfKeyPoint
    private lateinit var imagePoints : MatOfPoint2f
    private lateinit var objectPoints : MatOfPoint3f
    private lateinit var descriptors : Mat
    private lateinit var kaze : KAZE

    fun build() : FeatureExtractor{
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        keyPoints = MatOfKeyPoint()
        kaze = KAZE.create()
        kaze.detect(mat,keyPoints)
        descriptors = Mat()
        kaze.compute(mat,keyPoints,descriptors)
        imagePoints = buildImagePoints()
        objectPoints = buildWorldPoints()
        featureModel = FeatureModel(
            mat,
            keyPoints,
            imagePoints,
            objectPoints,
            descriptors,
            bitmap.width,
            bitmap.height
        )
        return this
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildImagePoints() : MatOfPoint2f{
        val points = MatOfPoint2f()
        val p = keyPoints.toList() as List<Point>
        points.fromList(p)
        return points
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildWorldPoints() : MatOfPoint3f{
        val points = MatOfPoint3f()
        val pointsList = mutableListOf<Point3>()
        val p = keyPoints.toList() as List<Point>
        for(i in p){
            val curr = Point3(i.x,i.y,1.0)
            pointsList.add(curr)
        }
        points.fromList(pointsList)
        return points
    }

}