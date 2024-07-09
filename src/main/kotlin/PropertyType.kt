import org.apache.commons.lang3.StringUtils

enum class PropertyType() {

    BOOLEAN {
        override fun isApplicable(type: String?, defaultValue: String?): Boolean {
            return type != null && type.lowercase().contains("bool")
                    || defaultValue != null && validate(defaultValue)
        }

        override fun validate(value: String) = value.equals("true", true) || value.equals("false", true)
    },

    INTEGER {
        override fun isApplicable(type: String?, defaultValue: String?): Boolean {
            return type != null && type.lowercase().contains("int")
                    || defaultValue != null && validate(defaultValue)
        }

        override fun validate(value: String) = StringUtils.isNumeric(value)
    },

    DOUBLE {
        override fun isApplicable(type: String?, defaultValue: String?): Boolean {
            return type != null
                    && (type.contains("float", true) || type.contains("double", true) || type.contains("real", true))
                    || defaultValue != null && validate(defaultValue)
        }

        override fun validate(value: String) = value.matches("[0-9.]+".toRegex())
    },

    STRING {
        override fun isApplicable(type: String?, defaultValue: String?) = true

        override fun validate(value: String) = true
    };

    companion object {
        fun get(dataType: String?, defaultValue: String?): PropertyType {
            return entries.find { it.isApplicable(dataType, defaultValue) } ?: STRING
        }
    }

    abstract fun isApplicable(type: String?, defaultValue: String?): Boolean

    abstract fun validate(value: String): Boolean

    override fun toString(): String {
        return StringUtils.capitalize(super.toString().lowercase())
    }
}
