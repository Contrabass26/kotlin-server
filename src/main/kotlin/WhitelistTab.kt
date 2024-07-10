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

private val whitelistType = object : TypeReference<List<WhitelistTab.Whitelisted>>() {}

class WhitelistTab(private val server: Server) : ServerConfigTab() {

    private val mainScope = ServerConfigTabType.WHITELIST.mainScope
    private val tableModel: DefaultTableModel

    init {
        layout = GridBagLayout()
        // Main list box
        val headings = arrayOf("Name", "UUID")
        tableModel = object : DefaultTableModel(0, headings.size) {
            override fun getColumnName(column: Int) = headings[column]

            override fun isCellEditable(row: Int, column: Int) = false
        }
        val table = JTable(tableModel)
        table.preferredScrollableViewportSize = Dimension(500, 300)
        table.fillsViewportHeight = true
        val tableScrollPane = JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        add(tableScrollPane, getConstraints(gridy = 2, gridwidth = 2, fill = GridBagConstraints.BOTH))
        // Add button
        val addBtn = JButton("Add")
        addBtn.addActionListener {
            val name = JOptionPane.showInputDialog(MAIN_SCREEN!!, "Enter player name:", "Add to whitelist", JOptionPane.QUESTION_MESSAGE)
            mainScope.launch {
                val uuid = getPlayerUuid(name)
                SwingUtilities.invokeLater {
                    tableModel.addRow(name, uuid)
                }
            }
        }
        add(addBtn, getConstraints(weightx = 1.0, insets = getInsets(bottom = 5, top = 5)))
        // Remove button
        val removeBtn = JButton("Remove")
        removeBtn.addActionListener {
            val selected = table.selectedRow
            if (selected != -1) {
                tableModel.removeRow(selected)
            }
        }
        add(removeBtn, getConstraints(gridx = 2, weightx = 1.0, insets = getInsets(top = 5, bottom = 5, left = 5)))
        // Load whitelist from file
        loadWhitelist()
    }

    override fun onCloseTab() {
        mainScope.cancel()
        // Save file
        val players: List<Whitelisted> = tableModel.dataVector
            .map { Whitelisted(
                it[0] as String,
                it[1] as String
            ) }
        JSON_MAPPER.writeValue(server.relativeFile("whitelist.json"), players)
    }

    override fun onFileUpdate() {
        loadWhitelist()
    }

    private fun loadWhitelist() {
        SwingUtilities.invokeLater {
            for (i in 0..<tableModel.rowCount) {
                tableModel.removeRow(0)
            }
        }
        mainScope.launch {
            val whitelist = JSON_MAPPER.readValue(server.relativeFile("whitelist.json"), whitelistType)
            SwingUtilities.invokeLater {
                whitelist.forEach { it.run {
                    tableModel.addRow(name, uuid)
                } }
            }
        }
    }

    data class Whitelisted @JsonCreator constructor(
        @JsonProperty("name") val name: String,
        @JsonProperty("uuid") val uuid: String
    ) {

        override fun toString() = name
    }
}