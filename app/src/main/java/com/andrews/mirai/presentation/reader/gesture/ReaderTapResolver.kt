package com.andrews.mirai.presentation.reader.gesture

object ReaderTapResolver {

    fun resolveHorizontal(
        positionX: Float,
        width: Float,
        edgeFraction: Float,
        reverseReadingDirection: Boolean
    ): ReaderTapAction {
        if (width <= 0f) {
            return ReaderTapAction.TOGGLE_CONTROLS
        }

        val safeEdgeFraction =
            edgeFraction.coerceIn(
                minimumValue = MINIMUM_EDGE_FRACTION,
                maximumValue = MAXIMUM_EDGE_FRACTION
            )

        val leftLimit =
            width * safeEdgeFraction

        val rightLimit =
            width * (
                    1f -
                            safeEdgeFraction
                    )

        val zone =
            when {
                positionX <= leftLimit -> {
                    ReaderTapZone.PREVIOUS
                }

                positionX >= rightLimit -> {
                    ReaderTapZone.NEXT
                }

                else -> {
                    ReaderTapZone.CENTER
                }
            }

        return when (zone) {
            ReaderTapZone.CENTER -> {
                ReaderTapAction.TOGGLE_CONTROLS
            }

            ReaderTapZone.PREVIOUS -> {
                if (reverseReadingDirection) {
                    ReaderTapAction.NEXT_PAGE
                } else {
                    ReaderTapAction.PREVIOUS_PAGE
                }
            }

            ReaderTapZone.NEXT -> {
                if (reverseReadingDirection) {
                    ReaderTapAction.PREVIOUS_PAGE
                } else {
                    ReaderTapAction.NEXT_PAGE
                }
            }
        }
    }

    fun resolveVertical(
        positionY: Float,
        height: Float,
        edgeFraction: Float
    ): ReaderTapAction {
        if (height <= 0f) {
            return ReaderTapAction.TOGGLE_CONTROLS
        }

        val safeEdgeFraction =
            edgeFraction.coerceIn(
                minimumValue = MINIMUM_EDGE_FRACTION,
                maximumValue = MAXIMUM_EDGE_FRACTION
            )

        val topLimit =
            height * safeEdgeFraction

        val bottomLimit =
            height * (
                    1f -
                            safeEdgeFraction
                    )

        return when {
            positionY <= topLimit -> {
                ReaderTapAction.PREVIOUS_PAGE
            }

            positionY >= bottomLimit -> {
                ReaderTapAction.NEXT_PAGE
            }

            else -> {
                ReaderTapAction.TOGGLE_CONTROLS
            }
        }
    }

    private const val MINIMUM_EDGE_FRACTION =
        0.15f

    private const val MAXIMUM_EDGE_FRACTION =
        0.42f
}