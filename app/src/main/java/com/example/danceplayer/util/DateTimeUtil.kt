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
}