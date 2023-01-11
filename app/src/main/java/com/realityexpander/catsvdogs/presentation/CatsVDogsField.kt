package com.realityexpander.catsvdogs.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.realityexpander.catsvdogs.data.GameState

@Composable
fun CatsVDogsField(
    state: GameState,
    modifier: Modifier = Modifier,
    playerXColor: Color = Color.Blue,
    playerOColor: Color = Color.Red,
    onTapInField: (x: Int, y: Int) -> Unit
) {

    Canvas(
        modifier = modifier
            .pointerInput(true) {
                detectTapGestures {
                    val x = (5 * it.x.toInt() / size.width)
                    val y = (5 * it.y.toInt() / size.height)
                    onTapInField(x, y)
                }
            }
    ) {
        drawField()

        state.field.forEachIndexed { y, _ ->
            state.field[y].forEachIndexed { x, player ->
                val offset = Offset(
                    x = x * size.width * (1 / 5f) + size.width / 10f,
                    y = y * size.height * (1 / 5f) + size.height / 7f,
                )
                if(player == 'X') {
                    drawX(
                        color = playerXColor,
                        center = offset
                    )
                } else if(player == 'O') {
                    drawO(
                        color = playerOColor,
                        center = offset
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawO(
    color: Color,
    center: Offset,
    size: Size = Size(50.dp.toPx(), 50.dp.toPx())
) {
//    drawCircle(
//        color = color,
//        center = center,
//        radius = size.width / 2f,
//        style = Stroke(
//            width = 3.dp.toPx()
//        )
//    )

    drawContext.canvas.nativeCanvas.drawText(
        "🐕", // O
        center.x,
        center.y,
        android.graphics.Paint().apply {
            this.color = color.toArgb()
            textSize = 50.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
        }
    )
}

private fun DrawScope.drawX(
    color: Color,
    center: Offset,
    size: Size = Size(50.dp.toPx(), 50.dp.toPx())
) {
//    drawLine(
//        color = color,
//        start = Offset(
//            x = center.x - size.width / 2f,
//            y = center.y - size.height / 2f
//        ),
//        end = Offset(
//            x = center.x + size.width / 2f,
//            y = center.y + size.height / 2f
//        ),
//        strokeWidth = 3.dp.toPx(),
//        cap = StrokeCap.Round
//    )
//    drawLine(
//        color = color,
//        start = Offset(
//            x = center.x - size.width / 2f,
//            y = center.y + size.height / 2f
//        ),
//        end = Offset(
//            x = center.x + size.width / 2f,
//            y = center.y - size.height / 2f
//        ),
//        strokeWidth = 3.dp.toPx(),
//        cap = StrokeCap.Round
//    )

    drawContext.canvas.nativeCanvas.drawText(
        "🐈", // X
        center.x,
        center.y,
        android.graphics.Paint().apply {
            this.color = color.toArgb()
            textSize = 50.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
        }
    )
}

private fun DrawScope.drawField() {
    // 1st vertical line
    drawLine(
        color = Color.LightGray,
        start = Offset(
            x = size.width * (1 / 5f),
            y = 0f
        ),
        end = Offset(
            x = size.width * (1 / 5f),
            y = size.height
        ),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // 2nd vertical line
    drawLine(
        color = Color.LightGray,
        start = Offset(
            x = size.width * (2 / 5f),
            y = 0f
        ),
        end = Offset(
            x = size.width * (2 / 5f),
            y = size.height
        ),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // 3rd vertical line
    drawLine(
        color = Color.LightGray,
        start = Offset(
            x = size.width * (3 / 5f),
            y = 0f
        ),
        end = Offset(
            x = size.width * (3 / 5f),
            y = size.height
        ),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // 4th vertical line
    drawLine(
        color = Color.LightGray,
        start = Offset(
            x = size.width * (4 / 5f),
            y = 0f
        ),
        end = Offset(
            x = size.width * (4 / 5f),
            y = size.height
        ),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // 1st horizontal line
    drawLine(
        color = Color.LightGray,
        start = Offset(
            x = 0f,
            y = size.height * (1 / 5f)
        ),
        end = Offset(
            x = size.width,
            y = size.height * (1 / 5f)
        ),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // 2nd horizontal line
    drawLine(
        color = Color.LightGray,
        start = Offset(
            x = 0f,
            y = size.height * (2 / 5f)
        ),
        end = Offset(
            x = size.width,
            y = size.height * (2 / 5f)
        ),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // 3rd horizontal line
    drawLine(
        color = Color.LightGray,
        start = Offset(
            x = 0f,
            y = size.height * (3 / 5f)
        ),
        end = Offset(
            x = size.width,
            y = size.height * (3 / 5f)
        ),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // 4th horizontal line
    drawLine(
        color = Color.LightGray,
        start = Offset(
            x = 0f,
            y = size.height * (4 / 5f)
        ),
        end = Offset(
            x = size.width,
            y = size.height * (4 / 5f)
        ),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
}

@Preview(showBackground = true)
@Composable
fun CatsVDogsPreview() {
    CatsVDogsField(
        state = GameState(
            field = arrayOf(
                arrayOf('X', null, null),
                arrayOf(null, 'O', 'O'),
                arrayOf(null, 'X', null),
            ),
        ),
        onTapInField = { _, _ ->},
        modifier = Modifier.size(300.dp)
    )
}