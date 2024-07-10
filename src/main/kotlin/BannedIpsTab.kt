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

private val ipBanListType = object : TypeReference<List<BannedIpsTab.IpBan>>() {}

class BannedIpsTab(private val server: Server) : ServerConfigTab() {

    private val mainScope = ServerConfigTabType.IP_BAN_LIST.mainScope
    private val tableModel: DefaultTableModel

    init {
        layout = GridBagLayout()
        // Main list box
        val headings = arrayOf("IP", "Reason", "Date", "Source", "Duration")
        tableModel = object : DefaultTableModel(0, headings.size) {
            override fun getColumnName(column: Int) = headings[column]

            override fun isCellEditable(row: Int, column: Int) = column >= 1
        }
        val table = JTable(tableModel)
        table.preferredScrollableViewportSize = Dimension(500, 300)
        table.fillsViewportHeight = true
        val tableScrollPane = JScrollPane(
            table,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        add(tableScrollPane, getConstraints(gridy = 2, gridwidth = 2, fill = GridBagConstraints.BOTH))
        // Add button
        val addBtn = JButton("Add")
        addBtn.addActionListener {
            val name = JOptionPane.showInputDialog(
                MAIN_SCREEN!!,
                "Enter IP address:",
                "Ban IP",
                JOptionPane.QUESTION_MESSAGE
            )
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
        // Load ban list from file
        loadBanList()
    }

    override fun onCloseTab() {
        mainScope.cancel()
        // Save file
        val players: List<IpBan> = tableModel.dataVector
            .map { IpBan(
                it[0] as String,
                it[1] as String,
                it[2] as String,
                it[3] as String,
                it[4] as String
            ) }
        JSON_MAPPER.writeValue(server.relativeFile("banned-ips.json"), players)
    }

    override fun onFileUpdate() {
        loadBanList()
    }

    private fun loadBanList() {
        SwingUtilities.invokeLater {
            for (i in 0..<tableModel.rowCount) {
                tableModel.removeRow(0)
            }
        }
        mainScope.launch {
            val banList = JSON_MAPPER.readValue(server.relativeFile("banned-ips.json"), ipBanListType)
            SwingUtilities.invokeLater {
                banList.forEach {
                    it.run {
                        tableModel.addRow(ip, reason, date, source, expires)
                    }
                }
            }
        }
    }

    data class IpBan @JsonCreator constructor(
        @JsonProperty("ip") val ip: String,
        @JsonProperty("reason") val reason: String,
        @JsonProperty("created") val date: String,
        @JsonProperty("source") val source: String,
        @JsonProperty("expires") val expires: String
    ) {

        override fun toString() = ip
    }
}