package com.cowinclub.dingdong.mediademo.openGlES

class Plane constructor(private var width: Float
                        , private var height: Float
                        , private var widthSegments: Int
                        , private var heightSegments: Int) : Mesh() {

    constructor(width: Float, height: Float) : this(width, height, 1, 1)
    constructor() : this(1f, 1f, 1, 1)



    private var textureCoordinates= floatArrayOf(
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    )
    init {
//        initData()

        origin()
    }

    private fun origin(){
        val vertices = FloatArray((widthSegments + 1) * (heightSegments + 1)
                * 3)
        val indices = ShortArray((widthSegments + 1) * (heightSegments + 1)
                * 6)

        val xOffset = width / -2
        val yOffset = height / -2
        val xWidth = width / widthSegments
        val yHeight = height / heightSegments
        var currentVertex = 0
        var currentIndex = 0
        val w = (widthSegments + 1).toShort()
        for (y in 0 until heightSegments + 1) {
            for (x in 0 until widthSegments + 1) {
                vertices[currentVertex] = xOffset + x * xWidth
                vertices[currentVertex + 1] = yOffset + y * yHeight
                vertices[currentVertex + 2] = 0f
                currentVertex += 3

                val n = y * (widthSegments + 1) + x

                if (y < heightSegments && x < widthSegments) {
                    // Face one
                    indices[currentIndex] = n.toShort()
                    indices[currentIndex + 1] = (n + 1).toShort()
                    indices[currentIndex + 2] = (n + w).toShort()
                    // Face two
                    indices[currentIndex + 3] = (n + 1).toShort()
                    indices[currentIndex + 4] = (n + 1 + w.toInt()).toShort()
                    indices[currentIndex + 5] = (n + 1 + w.toInt() - 1).toShort()

                    currentIndex += 6
                }
            }
        }

        setIndices(indices)
        setVertices(vertices)
        setTexTure(textureCoordinates)
    }


}