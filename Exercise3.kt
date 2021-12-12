package dsl.swing

import java.awt.BorderLayout
import java.awt.Container
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.*
import kotlin.system.exitProcess


fun frame(title: String, x: Int, y: Int, init: JFrameExt.() -> Unit): JFrameExt {
    val frame = JFrameExt(title)
    frame.setLocation(x, y)
    frame.init()
    return frame
}

class JFrameExt(title: String) : JFrame(title) {

    fun exitOnClose() {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    }

    fun open() {
        pack()
        isVisible = true
    }

    fun close() {
        exitProcess(0)
    }

    fun textField(width: Int, init: JTextField.() -> Unit = {}): JTextField {
        val textField =  JTextField(width)
        textField.init()
        return textField
    }

    fun label(text: String) = JLabel(text)

    fun menuBar(init: JMenuBar.() -> Unit) {
        jMenuBar = JMenuBar()
        jMenuBar.init()
    }

    fun JMenuBar.menu(name: String, init: JMenu.() -> Unit) {
        val menu = JMenu(name)
        this.add(menu)
        menu.init()
    }

    fun JMenu.menu(name: String, init: JMenu.() -> Unit) {
        val menu = JMenu(name)
        this.add(menu)
        menu.init()
    }

    fun JMenu.item(name: String): JMenuItem {
        val item = JMenuItem(name)
        this.add(item)
        return item
    }

    infix fun JMenuItem.onEvent(handler: () -> Unit) {
        addActionListener { handler() }
    }

    infix fun JButton.onEvent(handler: () -> Unit) {
        addActionListener { handler() }
    }

    infix fun JTextField.onEvent(handler: () -> Unit) {
        addActionListener { handler() }
    }

    operator fun JMenuItem.plus(handler: () -> Unit): JMenuItem {
        addActionListener { handler() }
        return this
    }

    fun content(init: Container.() -> Unit): Container {
        contentPane.init()
        return contentPane
    }

    fun panel(init: JPanel.() -> Unit): JPanel {
        val panel = JPanel()
        panel.init()
        return panel
    }

    fun button(label: String, init: JButton.() -> Unit = {}): JButton {
        val button = JButton(label)
        button.init()
        return button
    }

    open class LayoutBuilder : JPanel() {
        open fun <C : JComponent> comp(jComp: C, init: C.() -> Unit = {}): C {
            add(jComp)
            jComp.init()
            return jComp
        }

        fun etchedBorder() {
            this.border = BorderFactory.createEtchedBorder()
        }
    }

    fun Container.borderLayout(init: BorderLayoutBuilder.() -> Unit): JPanel {
        val borderLayoutBuilder = BorderLayoutBuilder()
        borderLayoutBuilder.layout = BorderLayout()
        add(borderLayoutBuilder)
        borderLayoutBuilder.init()
        return borderLayoutBuilder
    }

    class BorderLayoutBuilder : LayoutBuilder() {
        private fun <C : JComponent> comp(pos: String, jComp: C, init: C.() -> Unit): C {
            add(jComp, pos)
            jComp.init()
            return jComp
        }

        fun <C : JComponent> north(jComp: C, init: C.() -> Unit = {}): C {
            return comp(BorderLayout.NORTH, jComp, init)
        }

        fun <C : JComponent> center(jComp: C, init: C.() -> Unit = {}): C {
            return comp(BorderLayout.CENTER, jComp, init)
        }

        fun <C : JComponent> south(jComp: C, init: C.() -> Unit = {}): C {
            return comp(BorderLayout.SOUTH, jComp, init)
        }
        fun <C : JComponent> east(jComp: C, init: C.() -> Unit = {}): C {
            return comp(BorderLayout.EAST, jComp, init)
        }

        fun <C : JComponent> west(jComp: C, init: C.() -> Unit = {}): C {
            return comp(BorderLayout.WEST, jComp, init)
        }
    }

    fun Container.flowLayout(init: FlowLayoutBuilder.() -> Unit): JPanel {
        val flowLayoutBuilder = FlowLayoutBuilder()
        flowLayoutBuilder.layout = FlowLayout()
        add(flowLayoutBuilder)
        flowLayoutBuilder.init()
        return flowLayoutBuilder
    }

    class FlowLayoutBuilder : LayoutBuilder() {
    }

    fun Container.gridLayout(rows: Int, cols: Int, init: GridLayoutBuilder.() -> Unit): JPanel {
        val gridLayoutBuilder = GridLayoutBuilder(rows, cols)
        gridLayoutBuilder.layout = GridLayout(rows, cols)
        add(gridLayoutBuilder)
        gridLayoutBuilder.init()
        return gridLayoutBuilder
    }

    class GridLayoutBuilder(val rows: Int, val cols: Int) : JPanel() {
        fun <C : JComponent> comp(jComp: C, rows: Int, cols: Int, init: C.() -> Unit = {}): C {
            add(jComp)
            jComp.init()
            return jComp
        }
    }
}

fun main() {
    val frame =
        frame("Temperature converter", 200, 200) {

            exitOnClose()

            val celsius = textField(6)
            val fahrenh = textField(6)
            val message = label(" ")

            val refreshMessage: () -> Unit = {
                message.text = "${celsius.text} C = ${fahrenh.text} F"
            }

            val reset: () -> Unit = {
                celsius.text = "0"
                fahrenh.text = "32"
                refreshMessage()
            }

            menuBar {
                menu("File") {
                    item("Exit") onEvent { close() }
                }
                menu("Edit") {
                    item("Reset") onEvent {
                        reset()
                    }
                }
                menu("Settings") {
                    menu("Theme") {
                        UIManager.getInstalledLookAndFeels().forEach {
                            item(it.name) onEvent {
                                UIManager.setLookAndFeel(it.className)
                                SwingUtilities.updateComponentTreeUI(this@frame)
                                pack()
                            }
                        }
                    }
                }
            }
            content {
                borderLayout {
                    north(flowLayout {
                        etchedBorder()
                        comp(button("Reset")) onEvent {
                            reset()
                        }
                    })
                    center(flowLayout {
                        etchedBorder()
                        comp(celsius) { text = "0" } onEvent {
                            val c = celsius.text.filter { it.isDigit() || it == '-' }.toInt()
                            val f = c * 9 / 5 + 32
                            fahrenh.text = f.toString()
                            refreshMessage()
                        }
                        comp(JLabel("Celsius = "))
                        comp(fahrenh) { text = "32" } onEvent {
                            val f = fahrenh.text.filter { it.isDigit() || it == '-' }.toInt()
                            val c = (f - 32) * 5 / 9
                            celsius.text = c.toString()
                            refreshMessage()
                        }
                        comp(label("Fahrenheit"))
                    })
                    south(message) {
                        etchedBorder()
                        refreshMessage()
                    }
                }
            }
        }

    frame.open()
}