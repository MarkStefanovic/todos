package src.presentation

import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val mainColor = Color.rgb(20, 20, 20)
        val lightTextColor = mainColor.derive(0.9)
        val highlightColor = Color.DARKCYAN
        val centerAlignedCell by cssclass()
        val flat = mixin {
            backgroundInsets += box(0.px)
            borderColor += box(mainColor)
        }

        fun CssSelectionBlock.resetBorder() {
            borderRadius += box(0.px)
            borderStyle += BorderStrokeStyle.NONE
            borderColor += box(Color.TRANSPARENT)
            borderWidth += box(0.px)
        }
    }

    init {
        root {
            baseColor = mainColor
            backgroundColor += mainColor
            focusColor = Color.TRANSPARENT
            resetBorder()
        }

        text {
            fill = lightTextColor
            textFill = mainColor.derive(-0.3)
            effect = DropShadow(0.0, Color.TRANSPARENT)
        }

        label and heading {
            textFill = Color.TRANSPARENT
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
        }

        s(button, textInput) {
            +flat
        }

        tabPane {
            tab {
                text {
                    fontSize = 12.px
                    fontWeight = FontWeight.THIN
                    fill = mainColor.derive(0.5)
                }
                and(selected) {
                    text {
                        fontSize = 13.px
                        fontWeight = FontWeight.BOLD
                        fill = mainColor.derive(0.8)
                    }
                }
                and(hover) {
                    text {
                        fill = mainColor.derive(0.9)
                    }
                }
                backgroundColor += Color.TRANSPARENT
                padding = box(7.px)
            }
        }

        checkBox {
            box {
                backgroundColor += mainColor.derive(0.7)
            }
            and(selected) {
                mark {
                    backgroundColor += mainColor.derive(-0.9)
                }
            }
        }

        textInput {
            backgroundColor += lightTextColor
            textFill = mainColor.derive(-0.3)
            and(focused) {
                backgroundColor += mainColor.derive(0.97)
                textFill = mainColor.derive(-0.5)
            }
        }

        button {
            fontWeight = FontWeight.BOLD
            backgroundColor += mainColor.derive(0.6)
            text {
                fill = mainColor.derive(-0.7)
            }
            and(disabled) {
                backgroundColor += mainColor.derive(0.5)
            }
            and(hover) {
                backgroundColor += mainColor.derive(0.8)
            }
        }

        tableView {
            backgroundColor += mainColor.derive(0.1)

            label {
                backgroundColor += mainColor.derive(-0.5)
            }

            tableCell {
                resetBorder()
            }

            tableRowCell {
                borderColor += box(mainColor.derive(-0.1))
                and(even) {
                    backgroundColor += mainColor.derive(0.05)
                }
                and(odd) {
                    backgroundColor += mainColor.derive(-0.05)
                }
                and(selected) {
                    borderStyle += BorderStrokeStyle.SOLID
                    borderWidth += box(2.px)
                    borderColor += box(highlightColor)
                }
                text {
                    fontWeight = FontWeight.NORMAL
                }
            }

            tableColumn {
                label {
                    text {
                        fontWeight = FontWeight.BOLD
                    }
                    backgroundColor += mainColor.derive(-0.3)
                }
            }
        }

        centerAlignedCell {
            and(tableCell) {
                alignment = Pos.CENTER
            }
        }
    }
}