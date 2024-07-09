import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.awt.*
import java.io.File
import java.io.FileReader
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import javax.swing.text.html.HTMLEditorKit

private fun Properties.load(file: File) {
    load(FileReader(file))
}

class ServerPropertiesTab(server: Server) : ServerConfigTab() {
    
    companion object {
        private val mainScope = ServerConfigTabType.SERVER_PROPERTIES.mainScope
        private val descriptions: Deferred<Map<String, String>>
        private val dataTypes: Deferred<Map<String, String>>
        private val defaults: Deferred<Map<String, String>>

        init {
            val rows = mainScope.async {
                val document = getJsoup("https://minecraft.wiki/w/Server.properties")
                val table = document.select("table[data-description=Server properties]").first()
                table!!.select("tr")
            }
            descriptions = mainScope.async {
                rows.await().drop(1).associate {
                    val cells = it.select("td")
                    Pair(cells[0].text(), cells[3].html().trim())
                }
            }
            dataTypes = mainScope.async {
                rows.await().drop(1).associate {
                    val cells = it.select("td")
                    Pair(cells[0].text(), cells[1].text())
                }
            }
            defaults = mainScope.async {
                rows.await().drop(1).associate {
                    val cells = it.select("td")
                    Pair(cells[0].text(), cells[2].text())
                }
            }
        }

        suspend fun getDescription(key: String): String {
            return descriptions.await()[key] ?: "Not found"
        }

        suspend fun getDataType(key: String): PropertyType {
            val dataType = dataTypes.await()[key]
            val defaultValue = defaults.await()[key]
            return PropertyType.get(dataType, defaultValue)
        }

        suspend fun getDefaultValue(key: String): String {
            return defaults.await()[key] ?: "Not found"
        }
    }

    private val properties: Properties

    init {
        layout = GridBagLayout()
        properties = Properties()
        properties.load(server.relativeFile("server.properties"))
        // Table
        val headings = arrayOf("Name", "Description", "Default value", "Data type", "Value")
        val tableModel = object : DefaultTableModel(0, headings.size) {
            override fun getColumnName(column: Int) = headings[column]

            override fun isCellEditable(row: Int, column: Int) = column == 4
        }
        val descriptionRenderer = object : JTextPane(), TableCellRenderer {
            private val focusBorder = BorderFactory.createLineBorder(Color(21, 65, 106))
            private val gridBorder = BorderFactory.createMatteBorder(0, 0, 1, 1, Color(235, 235, 235))

            init {
                contentType = "text/html"
                isEditable = false
                isOpaque = true
                HTMLEditorKit().styleSheet.addRule("a {text-decoration:none}")
                HTMLEditorKit().styleSheet.addRule("code {font-size:normal}")
            }

            override fun getTableCellRendererComponent(
                table: JTable?,
                value: Any?,
                isSelected: Boolean,
                hasFocus: Boolean,
                row: Int,
                column: Int
            ): Component {
                text = "<html>${value as String}</html>"
                if (table == null) throw IllegalArgumentException("No table provided")
                val textColor = if (isSelected) table.selectionForeground else table.foreground
                background = if (isSelected) table.selectionBackground else table.background
                foreground = textColor
                val outerBorder = if (hasFocus) focusBorder else if (isSelected) null else gridBorder
                val innerBorder = if (outerBorder == null) BorderFactory.createEmptyBorder(3, 3, 3, 3) else BorderFactory.createEmptyBorder(2, 2, 2, 3)
                border = BorderFactory.createCompoundBorder(outerBorder, innerBorder)
                val textColorHex = textColor.run { String.format("#%02x%02x%02x", red, green, blue) }
                HTMLEditorKit().styleSheet.addRule("a {color:$textColorHex}")
                val width = table.columnModel.getColumn(column).width
                size = Dimension(width, 20)
                table.setRowHeight(row, preferredSize.height)
                return this
            }
        }
        val table = object : JTable(tableModel) {
            override fun getCellRenderer(row: Int, column: Int): TableCellRenderer {
                if (column == 1) {
                    return descriptionRenderer
                }
                return super.getCellRenderer(row, column)
            }
        }
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.preferredScrollableViewportSize = Dimension(500, 300)
        table.fillsViewportHeight = true
        table.showHorizontalLines = true
        table.showVerticalLines = true
        mainScope.launch {
            properties.forEach {
                val key = it.key as String
                val description = getDescription(key)
                val defaultValue = getDefaultValue(key)
                val dataType = getDataType(key)
                SwingUtilities.invokeLater {
                    tableModel.addRow(arrayOf(key, description, defaultValue, dataType, it.value))
                }
            }
        }
        val tableScrollPane = JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        add(tableScrollPane, getConstraints(fill = GridBagConstraints.BOTH))
    }

    override fun onCloseTab() {

    }
}