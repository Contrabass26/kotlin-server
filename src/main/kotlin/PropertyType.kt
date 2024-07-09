import org.apache.commons.lang3.StringUtils

enum class PropertyType() {

    BOOLEAN {
        override fun isApplicable(type: String?, defaultValue: String?): Boolean {
            return type != null
                    && type.lowercase().contains("bool")
                    || defaultValue != null
                    && (defaultValue.equals("true", true)
                    || defaultValue.equals("false", true))
        }
    },

    INTEGER {
        override fun isApplicable(type: String?, defaultValue: String?): Boolean {
            return type != null && type.lowercase().contains("int") || StringUtils.isNumeric(defaultValue)
        }
    },

    DOUBLE {
        override fun isApplicable(type: String?, defaultValue: String?): Boolean {
            if (type != null) {
                val lowerType = type.lowercase()
                if (lowerType.contains("float") || lowerType.contains("double") || lowerType.contains("real")) return true
            }
            return defaultValue != null && defaultValue.matches("[0-9.]+".toRegex())
        }
    },

    STRING {
        override fun isApplicable(type: String?, defaultValue: String?): Boolean {
            return true
        }
    };

    companion object {
        fun get(dataType: String?, defaultValue: String?): PropertyType {
            return entries.find { it.isApplicable(dataType, defaultValue) } ?: STRING
        }
    }

    abstract fun isApplicable(type: String?, defaultValue: String?): Boolean
}
