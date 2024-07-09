import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.awt.*
import java.io.File
import java.io.FileReader
import java.net.URI
import java.util.*
import javax.swing.*
import javax.swing.border.LineBorder
import javax.swing.event.HyperlinkEvent
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
                    Pair(cells[0].text(), cells[3].html())
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
            private val selectedBorder = Color(21, 65, 106)

            init {
                contentType = "text/html"
                isEditable = false
                isOpaque = true
                addHyperlinkListener {
                    println("Url clicked")
                }
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
                background = if (isSelected) table.selectionBackground else table.background
                foreground = if (isSelected) table.selectionForeground else table.foreground
                border = if (hasFocus) LineBorder(selectedBorder, 1) else null
                val color = if (isSelected) "#ffffff" else "#236db2"
                HTMLEditorKit().styleSheet.addRule("a {color:$color}")
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