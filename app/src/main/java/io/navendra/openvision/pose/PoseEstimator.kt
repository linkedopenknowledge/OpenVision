package io.navendra.openvision.pose

import android.media.Image
import android.view.Surface

class PoseEstimator constructor( val listener: PoseListener,
                                 val referenceImage : Image,
                                 val cameraSurface : Surface){

    fun startEstimation(){
        listener.start()

    }

    fun onImageAvailable(){

    }

    fun endEstimation(){
        listener.stop()
    }

}