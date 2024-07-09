import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import java.io.FileReader
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableModel

private fun Properties.load(file: File) {
    load(FileReader(file))
}

class ServerPropertiesTab(server: Server) : ServerConfigTab() {

    private val properties: Properties

    init {
        layout = GridBagLayout()
        properties = Properties()
        properties.load(server.relativeFile("server.properties"))
        // Table
        val tableModel = object : DefaultTableModel(0, 3) {
            private val headings = arrayOf("Name", "Description", "Value")

            override fun getColumnName(column: Int) = headings[column]

            override fun isCellEditable(row: Int, column: Int) = column == 2
        }
        val table = JTable(tableModel)
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.preferredScrollableViewportSize = Dimension(500, 300)
        table.fillsViewportHeight = true
        table.inputVerifier = object : InputVerifier() {
            override fun verify(input: JComponent?): Boolean {
                return true
            }

            override fun shouldYieldFocus(source: JComponent?, target: JComponent?): Boolean {
                if (source !is JTable) throw IllegalArgumentException("Source is not JTable")
                val editingKey = source.getValueAt(source.editingRow, 0)
            }
        }
        properties.forEach {
            tableModel.addRow(arrayOf(it.key, "Not found", it.value))
        }
        val tableScrollPane = JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        add(tableScrollPane, getConstraints(fill = GridBagConstraints.BOTH))
    }

    override fun onCloseTab() {

    }
}