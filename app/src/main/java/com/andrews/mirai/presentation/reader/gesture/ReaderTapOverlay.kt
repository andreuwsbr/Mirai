package com.andrews.mirai.presentation.reader.gesture

import android.view.ViewConfiguration
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

fun Modifier.readerTapOverlay(
    enabled: Boolean,
    edgeFraction: Float,
    vertical: Boolean,
    reverseReadingDirection: Boolean,
    onAction: (ReaderTapAction) -> Unit
): Modifier {
    if (!enabled) {
        return this
    }

    return pointerInput(
        enabled,
        edgeFraction,
        vertical,
        reverseReadingDirection
    ) {
        coroutineScope readerScope@{
            var pendingTapJob: Job? =
                null

            var previousTapPosition: Offset? =
                null

            awaitEachGesture {
                val down =
                    awaitFirstDown(
                        requireUnconsumed = false,
                        pass =
                            PointerEventPass.Initial
                    )

                val initialPosition =
                    down.position

                var movedBeyondTapDistance =
                    false

                var multiplePointersDetected =
                    false

                var gestureFinished =
                    false

                while (!gestureFinished) {
                    val event =
                        awaitPointerEvent(
                            pass =
                                PointerEventPass.Initial
                        )

                    val pressedPointers =
                        event.changes.count { change ->
                            change.pressed
                        }

                    if (
                        event.changes.size > 1 ||
                        pressedPointers > 1
                    ) {
                        multiplePointersDetected =
                            true
                    }

                    val currentChange =
                        event.changes
                            .firstOrNull { change ->
                                change.id == down.id
                            }

                    if (currentChange == null) {
                        gestureFinished =
                            true

                        continue
                    }

                    val movementDistance =
                        distanceBetween(
                            first =
                                initialPosition,
                            second =
                                currentChange.position
                        )

                    if (
                        movementDistance >
                        viewConfiguration.touchSlop
                    ) {
                        movedBeyondTapDistance =
                            true
                    }

                    if (!currentChange.pressed) {
                        val gestureDuration =
                            currentChange.uptimeMillis -
                                    down.uptimeMillis

                        val validSingleTap =
                            !movedBeyondTapDistance &&
                                    !multiplePointersDetected &&
                                    gestureDuration <=
                                    MAXIMUM_TAP_DURATION_MILLIS

                        if (validSingleTap) {
                            val currentTapPosition =
                                currentChange.position

                            val previousPosition =
                                previousTapPosition

                            val isPossibleDoubleTap =
                                pendingTapJob
                                    ?.isActive == true &&
                                        previousPosition != null &&
                                        distanceBetween(
                                            first =
                                                previousPosition,
                                            second =
                                                currentTapPosition
                                        ) <=
                                        viewConfiguration
                                            .touchSlop *
                                        DOUBLE_TAP_DISTANCE_MULTIPLIER

                            if (isPossibleDoubleTap) {
                                /*
                                 * Cancela a navegação do primeiro
                                 * toque para deixar o duplo toque
                                 * disponível ao zoom da imagem.
                                 */
                                pendingTapJob
                                    ?.cancel()

                                pendingTapJob =
                                    null

                                previousTapPosition =
                                    null
                            } else {
                                pendingTapJob
                                    ?.cancel()

                                previousTapPosition =
                                    currentTapPosition

                                pendingTapJob =
                                    this@readerScope.launch {
                                        delay(
                                            ViewConfiguration
                                                .getDoubleTapTimeout()
                                                .toLong()
                                        )

                                        val action =
                                            if (vertical) {
                                                ReaderTapResolver
                                                    .resolveVertical(
                                                        positionY =
                                                            currentTapPosition.y,
                                                        height =
                                                            size.height
                                                                .toFloat(),
                                                        edgeFraction =
                                                            edgeFraction
                                                    )
                                            } else {
                                                ReaderTapResolver
                                                    .resolveHorizontal(
                                                        positionX =
                                                            currentTapPosition.x,
                                                        width =
                                                            size.width
                                                                .toFloat(),
                                                        edgeFraction =
                                                            edgeFraction,
                                                        reverseReadingDirection =
                                                            reverseReadingDirection
                                                    )
                                            }

                                        onAction(
                                            action
                                        )

                                        previousTapPosition =
                                            null

                                        pendingTapJob =
                                            null
                                    }
                            }
                        }

                        gestureFinished =
                            true
                    }

                    if (
                        event.changes.none { change ->
                            change.pressed
                        }
                    ) {
                        gestureFinished =
                            true
                    }
                }
            }
        }
    }
}

private fun distanceBetween(
    first: Offset,
    second: Offset
): Float {
    val differenceX =
        second.x -
                first.x

    val differenceY =
        second.y -
                first.y

    return sqrt(
        differenceX *
                differenceX +
                differenceY *
                differenceY
    )
}

private const val MAXIMUM_TAP_DURATION_MILLIS =
    500L

private const val DOUBLE_TAP_DISTANCE_MULTIPLIER =
    2f