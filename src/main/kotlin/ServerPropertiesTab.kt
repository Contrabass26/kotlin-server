import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import java.io.FileReader
import java.util.*
import javax.swing.CellEditor
import javax.swing.DefaultCellEditor
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.ScrollPaneConstants
import javax.swing.event.CellEditorListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellEditor

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
        table.cellEditor = object : DefaultCellEditor() {
            override fun getCellEditorValue(): Any {
                TODO("Not yet implemented")
            }

            override fun isCellEditable(anEvent: EventObject?): Boolean {
                TODO("Not yet implemented")
            }

            override fun shouldSelectCell(anEvent: EventObject?): Boolean {
                TODO("Not yet implemented")
            }

            override fun stopCellEditing(): Boolean {
                TODO("Not yet implemented")
            }

            override fun cancelCellEditing() {
                TODO("Not yet implemented")
            }

            override fun addCellEditorListener(l: CellEditorListener?) {
                TODO("Not yet implemented")
            }

            override fun removeCellEditorListener(l: CellEditorListener?) {
                TODO("Not yet implemented")
            }

            override fun getTableCellEditorComponent(
                table: JTable?,
                value: Any?,
                isSelected: Boolean,
                row: Int,
                column: Int
            ): Component {
                TODO("Not yet implemented")
            }

        }
        properties.forEach {
            tableModel.addRow(arrayOf(it.key, "Not found", it.value))
        }
        val tableScrollPane = JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        add(tableScrollPane, getConstraints(fill = GridBagConstraints.BOTH))
    }

    override fun onCloseTab() {
        println("Closing server properties tab")
    }
}