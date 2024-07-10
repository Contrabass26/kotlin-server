import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*
import javax.swing.table.DefaultTableModel

private val operatorListType = object : TypeReference<List<OpsTab.Operator>>() {}

fun <T> DefaultListModel<T>.add(value: T) {
    add(size, value)
}

class OpsTab(private val server: Server) : ServerConfigTab() {

    private val mainScope = ServerConfigTabType.OPERATORS.mainScope
    private val tableModel: DefaultTableModel

    init {
        layout = GridBagLayout()
        // Main list box
        val headings = arrayOf("Name", "UUID", "Permission level", "Bypass player limit")
        tableModel = object : DefaultTableModel(0, headings.size) {
            override fun getColumnName(column: Int) = headings[column]

            override fun isCellEditable(row: Int, column: Int) = column >= 2

            override fun getColumnClass(columnIndex: Int): Class<*> = when (columnIndex) {
                0 -> String::class
                1 -> String::class
                2 -> Int::class
                3 -> Boolean::class
                else -> { throw IllegalArgumentException("Column $columnIndex should not exist") }
            }.javaObjectType
        }
        val table = JTable(tableModel)
        table.preferredScrollableViewportSize = Dimension(500, 300)
        table.fillsViewportHeight = true
        val tableScrollPane = JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        add(tableScrollPane, getConstraints(gridy = 2, fill = GridBagConstraints.BOTH))
        // Add button
        val addBtn = JButton("Add")
        addBtn.addActionListener {
            val name = JOptionPane.showInputDialog(MAIN_SCREEN!!, "Enter player name:", "Add operator", JOptionPane.QUESTION_MESSAGE)

        }
        add(addBtn, getConstraints(weightx = 1.0, insets = getInsets(bottom = 5)))
        // Load operators from file
        loadOperators()
    }

    override fun onCloseTab() {
        mainScope.cancel()
        // Save file
        val operators: List<Operator> = tableModel.dataVector
            .map { Operator(
                it[0] as String,
                it[1] as String,
                it[2] as Int,
                it[3] as Boolean
            ) }
        JSON_MAPPER.writeValue(server.relativeFile("ops.json"), operators)
    }

    override fun onFileUpdate() {
        loadOperators()
    }

    private fun loadOperators() {
        SwingUtilities.invokeLater {
            for (i in 0..<tableModel.rowCount) {
                tableModel.removeRow(0)
            }
        }
        mainScope.launch {
            val operators = JSON_MAPPER.readValue(server.relativeFile("ops.json"), operatorListType)
            SwingUtilities.invokeLater {
                operators.forEach { it.run {
                    tableModel.addRow(arrayOf(name, uuid, level, bypassLimit))
                } }
            }
        }
    }

    data class Operator @JsonCreator constructor(
        @JsonProperty("name") val name: String,
        @JsonProperty("uuid") val uuid: String,
        @JsonProperty("level") val level: Int,
        @JsonProperty("bypassesPlayerLimit") val bypassLimit: Boolean
    ) {

        override fun toString(): String {
            return "Operator(name='$name', uuid='$uuid', level=$level, bypassesPlayerLimit=$bypassLimit)"
        }
    }
}