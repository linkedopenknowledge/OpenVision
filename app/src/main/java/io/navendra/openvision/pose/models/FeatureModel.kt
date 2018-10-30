package io.navendra.openvision.pose.models

import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.MatOfPoint3f

data class FeatureModel(
    val mat : Mat,
    val keyPoints: MatOfKeyPoint,
    val imagePoints : MatOfPoint2f,
    val objectPoints: MatOfPoint3f,
    val descriptors : Mat,
    val width : Int,
    val height : Int
)