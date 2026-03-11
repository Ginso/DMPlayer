import com.example.danceplayer.model.Tag
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object DateTimeUtil {
    fun formatDuration(duration: Long): String {
        val hours = duration / 3600000
        val minutes = (duration % 3600000) / 60000
        val seconds = (duration % 60000) / 1000
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {             
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun parse(text: String, type: Tag.Type): Long? {
        return try {
            when (type) {
                Tag.Type.DATETIME, Tag.Type.DATE, Tag.Type.TIME -> {
                    val formatter = when (type) {
                        Tag.Type.DATE -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        Tag.Type.TIME -> {
                            when (text.count { it == ':' }) {
                                1 -> DateTimeFormatter.ofPattern("mm:ss")
                                2 -> DateTimeFormatter.ofPattern("HH:mm:ss")
                                else -> return null
                            }
                        }
                        else -> DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                    }.localizedBy(Locale.getDefault()).withZone(ZoneId.systemDefault())
                    formatter.parse(text, Instant::from).toEpochMilli()
                }
                else -> null
            }
        } catch (e: Exception) {
             null
         }
    }

    fun getPattern(type: Tag.Type): String {
        return when(type) {
            Tag.Type.DATE -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(Locale.getDefault()).withZone(ZoneId.systemDefault()).toString()
            Tag.Type.TIME -> "mm:ss or hh:mm:ss"
            Tag.Type.DATETIME -> DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).localizedBy(Locale.getDefault()).withZone(ZoneId.systemDefault()).toString()
            else -> ""
        }
    }
}