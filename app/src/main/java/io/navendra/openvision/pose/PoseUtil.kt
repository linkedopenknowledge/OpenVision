package io.navendra.openvision.pose

import android.graphics.Bitmap
import android.util.Log
import io.navendra.openvision.pose.models.FeatureModel
import org.opencv.features2d.DescriptorMatcher
import java.util.*
import org.opencv.calib3d.Calib3d
import org.opencv.calib3d.Calib3d.SOLVEPNP_ITERATIVE
import org.opencv.core.*


class PoseUtil constructor(private val bitmap1: Bitmap, private val bitmap2 : Bitmap){

    lateinit var homography : Mat
    lateinit var rVec : Mat
    lateinit var tVec : Mat


    private lateinit var referenceFeatureModel: FeatureModel
    private lateinit var targetFeatureModel: FeatureModel
    private var maxDist = 0.0
    private var minDist = 100.0
    private var matches = MatOfDMatch()
    var matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED)

    fun build(){
        init()
        computeHomography()
        resolveMotion()
    }

    private fun init(){
        referenceFeatureModel = FeatureExtractor(bitmap1)
            .build()
            .featureModel

        targetFeatureModel = FeatureExtractor(bitmap2)
            .build()
            .featureModel

    }

    private fun computeHomography(){
        matcher.match(referenceFeatureModel.descriptors, targetFeatureModel.descriptors, matches)

        // Calculate min and max distance between the keypoints in the two images.

        val listMatches = matches.toList()

        for (i in listMatches.indices) {
            val dist = listMatches[i].distance.toDouble()
            if (dist < minDist) minDist = dist
            if (dist > maxDist) maxDist = dist
        }

        Log.d(this.javaClass.simpleName, "Min: $minDist")
        Log.d(this.javaClass.simpleName, "Max: $maxDist")

        val goodListMatches = LinkedList<DMatch>()
        val goodMatches = MatOfDMatch()
        for (i in 0 until listMatches.size) {
            if (listMatches[i].distance < 2 * minDist) {
                goodListMatches.addLast(listMatches[i])
            }
        }

        goodMatches.fromList(goodListMatches)

        Log.d(this.javaClass.simpleName, "Number of matches: ${listMatches.size}")
        Log.d(this.javaClass.simpleName, "Number of good matches: ${goodListMatches.size}")

        // Calculate the homograohy between the two images...
        val imgPoints1List = LinkedList<Point>()
        val imgPoints2List = LinkedList<Point>()
        val keypoints1List = referenceFeatureModel.keyPoints.toList()
        val keypoints2List = targetFeatureModel.keyPoints.toList()

        for (i in 0 until goodListMatches.size) {
            imgPoints1List.addLast(keypoints1List[goodListMatches[i].queryIdx].pt)
            imgPoints2List.addLast(keypoints2List[goodListMatches[i].trainIdx].pt)
        }

        val obj = MatOfPoint2f()
        obj.fromList(imgPoints1List)
        val scene = MatOfPoint2f()
        scene.fromList(imgPoints2List)

        homography = Calib3d.findHomography(obj, scene, Calib3d.RANSAC, 3.0)

    }

    private fun resolveMotion(){
        val img1 = referenceFeatureModel

        val cameraMat = buildCameraMatrix(img1)
        tVec = Mat()
        rVec = Mat()
        val sol = Calib3d.solvePnPRansac(img1.objectPoints,img1.imagePoints,homography,cameraMat,
            rVec, tVec, true, SOLVEPNP_ITERATIVE)
        val i = 0
    }

    private fun buildCameraMatrix(model: FeatureModel) : MatOfDouble{
        val img = referenceFeatureModel
        val cameraMatrix = Mat.zeros(3,3,0)

        val fx = img.width as Double
        val fy = img.height as Double
        val cx = img.width/2 as Double
        val cy =  img.height/-2 as Double
        val one = 1.0

        cameraMatrix.put(0,0, fx)
        cameraMatrix.put(1,1, fy)
        cameraMatrix.put(2,2, one)
        cameraMatrix.put(0,2, cx)
        cameraMatrix.put(1,2, cy)

        return cameraMatrix as MatOfDouble
    }









}